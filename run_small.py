import os
import sys
import time
import subprocess


def whitebox(port):
    timeout = time.time() + 60 * 60 * float(time_limit)
    while time.time() < timeout:
        subprocess.run("rm -rf " + service, shell=True)
        subprocess.run("java -jar evomaster.jar --sutControllerPort " + str(port) + " --maxTime 10m --outputFolder " + service, shell=True)


def blackbox(swagger, port):
    timeout = time.time() + 60 * 60 * float(time_limit)
    while time.time() < timeout:
        if tool == "dredd":
            subprocess.run("dredd " + swagger + ' http://localhost:' + str(port), shell=True)
        elif tool == "evomaster-blackbox":
            subprocess.run("rm -rf " + service, shell=True)
            subprocess.run("java -jar evomaster.jar --blackBox true --bbSwaggerUrl " + swagger + " --bbTargetUrl http://localhost:" + str(port) + " --outputFormat JAVA_JUNIT_4 --maxTime 6m --outputFolder " + service, shell=True)
        elif tool == "restler":
            basedir = os.path.join(curdir, "restler_" + service)
            restler_home = os.path.join(curdir, "restler/restler_bin/restler/Restler.dll")
            com1 = " && dotnet " + restler_home + " compile --api_spec " + swagger
            com2 = " && dotnet " + restler_home + " fuzz --grammar_file ./Compile/grammar.py --dictionary_file ./Compile/dict.json --settings ./Compile/engine_settings.json --no_ssl --time_budget " + time_limit
            subprocess.run("rm -rf " + basedir, shell=True)
            subprocess.run("mkdir " + basedir + " && cd " + basedir + com1 + com2, shell=True)
        elif tool == "resttestgen":
            subprocess.run("rm -rf services/" + service + "/output",shell=True)
            subprocess.run("cd services/" + service + " && java -jar ../../RestTestGen/RestTestGen.jar", shell=True)
        elif tool == "bboxrt":
            run = "java -jar bBOXRT/target/REST_API_Robustness_Tester-1.0.jar"
            api_file = " --api-file bBOXRT/src/main/java/test.java"
            yaml_file = " --api-yaml-file " + swagger
            subprocess.run(run + api_file + yaml_file, shell=True)
        elif tool == "restest":
            run = "cd RESTest && java -jar target/restest-full.jar"
            api_file = " src/test/resources/" + service + "/api.properties"
            subprocess.run(run + api_file, shell=True)
        elif tool == "schemathesis":
            run = "schemathesis run " + swagger
            options = " --stateful=links --request-timeout 5000 --validate-schema False --base-url http://localhost:" + str(port)
            subprocess.run(run + options, shell=True)
        elif tool == "tcases":
            subprocess.run("rm -rf ./tcases_" + service, shell=True)
            subprocess.run("./tcases/bin/tcases-api-test -o tcases_" + service + "/src/test/java/tcases -p tcases -u 5000 -S " + swagger, shell=True)
            subprocess.run("cp pom.xml ./tcases_" + service, shell=True)
            subprocess.run("cd tcases_" + service + " && mvn clean test", shell=True)
        elif tool == "apifuzzer":
            subprocess.run("APIFuzzer -s " + swagger + " -u http://localhost:" + str(port), shell=True)


if __name__ == "__main__":
    tool = sys.argv[1]
    port = sys.argv[2]
    service = "project-tracking-system"
    time_limit = "0.1"
    curdir = os.getcwd()

    if tool == "evomaster-whitebox":
        subprocess.run("python3 run_service.py " + service + " " + str(port) + " whitebox", shell=True)
    else:
        subprocess.run("python3 run_service.py " + service + " " + str(port) + " blackbox", shell=True)

    print("Service started in the background. To check or kill the session, please see README file.")
    time.sleep(10)

    subprocess.run("tmux new -d -s small_cov 'sh small_cov.sh " + str(port) + "'", shell=True)
    print("We are getting coverage now.")
    time.sleep(10)

    if tool == "evomaster-whitebox":
        whitebox(40119)
    elif tool == "evomaster-blackbox":
        blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/project_swagger.json", 50118)
    elif tool == "bboxrt":
        blackbox(os.path.join(curdir, "doc/project_swagger.yaml"), 50118)
    elif tool == "tcases" or tool == "apifuzzer":
        blackbox(os.path.join(curdir, "doc/project_tcases.yaml"), 50118)
    else:
        blackbox(os.path.join(curdir, "doc/project_swagger.json"), 50118)

    print("Experiments are done. We are safely closing the service now. If you want to run more, please check if there is unclosed session. You can check it with 'tmux ls' command. To close the session, you can run 'tmux kill-sess -t {session name}'")
    time.sleep(30)
    subprocess.run("tmux kill-sess -t " + service, shell=True)