import os
import sys
import time
import subprocess


def whitebox(port):
    timeout = time.time() + 60 * 60 * int(time_limit)
    while time.time() < timeout:
        subprocess.run("rm -rf " + service, shell=True)
        subprocess.run("java -jar evomaster.jar --sutControllerPort " + str(port) + " --maxTime " + time_limit + "h --outputFolder " + service, shell=True)


def blackbox(swagger, port):
    timeout = time.time() + 60 * 60 * int(time_limit)
    while time.time() < timeout:
        if tool == "dredd":
            subprocess.run("dredd " + swagger + ' http://localhost:' + str(port), shell=True)
        elif tool == "evomaster-blackbox":
            subprocess.run("rm -rf " + service, shell=True)
            subprocess.run("java -jar evomaster.jar --blackBox true --bbSwaggerUrl " + swagger + " --bbTargetUrl http://localhost:" + str(port) + " --outputFormat JAVA_JUNIT_4 --maxTime " + time_limit + "h --outputFolder " + service, shell=True)
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
    service = sys.argv[2]
    port = sys.argv[3]
    time_limit = "1"

    curdir = os.getcwd()

    if tool == "evomaster-whitebox":
        subprocess.run("python3 run_service.py " + service + " " + str(port) + " whitebox", shell=True)
    else:
        subprocess.run("python3 run_service.py " + service + " " + str(port) + " blackbox", shell=True)

    print("Service started in the background. To check or kill the session, please see README file.")
    time.sleep(60)

    subprocess.run("tmux new -d -s small_cov 'sh get_cov.sh " + str(port) + "'", shell=True)
    print("We are getting coverage now.")
    time.sleep(60)

    if service == "features-service":
        if tool == "evomaster-whitebox":
            whitebox(40101)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/features_swagger.json", 50100)
        elif tool == "bboxrt" or tool == "tcases":
            blackbox(os.path.join(curdir, "doc/features_openapi.yaml"), 50100)
        else:
            blackbox(os.path.join(curdir, "doc/features_swagger.json"), 50100)
    elif service == "languagetool":
        if tool == "evomaster-whitebox":
            whitebox(40100)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/languagetool_swagger.json", 50101)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/languagetool_swagger.yaml"), 50101)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/languagetool_openapi.yaml"), "50101/v2")
        else:
            blackbox(os.path.join(curdir, "doc/languagetool_swagger.json"), 50101)
    elif service == "ncs":
        if tool == "evomaster-whitebox":
            whitebox(40102)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/ncs_swagger.json", 50102)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/ncs_swagger.yaml"), 50102)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/ncs_openapi.yaml"), 50102)
        else:
            blackbox(os.path.join(curdir, "doc/ncs_swagger.json"), 50102)
    elif service == "news":
        if tool == "evomaster-whitebox":
            whitebox(40103)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/news_swagger.json", 50103)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/news_swagger.yaml"), 50103)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/news_openapi.yaml"), 50103)
        else:
            blackbox(os.path.join(curdir, "doc/news_swagger.json"), 50103)
    elif service == "ocvn":
        if tool == "evomaster-whitebox":
            whitebox(40104)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/ocvn_swagger.json", 50104)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/ocvn_swagger.yaml"), 50104)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/ocvn_openapi.yaml"), 50104)
        else:
            blackbox(os.path.join(curdir, "doc/ocvn_swagger.json"), 50104)
    elif service == "proxyprint":
        if tool == "evomaster-whitebox":
            whitebox(40105)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/proxyprint_swagger.json", 50105)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/proxyprint_swagger.yaml"), 50105)
        elif tool == "tcases" or tool == "apifuzzer":
            blackbox(os.path.join(curdir, "doc/proxyprint_openapi.yaml"), 50105)
        else:
            blackbox(os.path.join(curdir, "doc/proxyprint_swagger.json"), 50105)
    elif service == "restcountries":
        if tool == "evomaster-whitebox":
            whitebox(40106)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/restcountries_openapi.yaml", "50106")
        elif tool == "apifuzzer":
            blackbox(os.path.join(curdir, "doc/restcountries_openapi.yaml"), "50106")
        else:
            blackbox(os.path.join(curdir, "doc/restcountries_openapi.yaml"), "50106/rest")
    elif service == "scout-api":
        if tool == "evomaster-whitebox":
            whitebox(40107)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/scout_swagger.json", 50107)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/scout_swagger.yaml"), 50107)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/scout_tcases.yaml"), 50107)
        elif tool == "apifuzzer":
            blackbox(os.path.join(curdir, "doc/scout_apifuzzer.json"), 50107)
        else:
            blackbox(os.path.join(curdir, "doc/scout_swagger.json"), 50107)
    elif service == "scs":
        if tool == "evomaster-whitebox":
            whitebox(40108)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/scs_swagger.json", 50108)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/scs_swagger.yaml"), 50108)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/scs_openapi.yaml"), 50108)
        else:
            blackbox(os.path.join(curdir, "doc/scs_swagger.json"), 50108)
    elif service == "erc20-rest-service":
        if tool == "evomaster-whitebox":
            whitebox(40109)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/erc20_swagger.json", 50109)
        elif tool == "bboxrt" or tool == "tcases":
            blackbox(os.path.join(curdir, "doc/erc20_openapi.yaml"), 50109)
        else:
            blackbox(os.path.join(curdir, "doc/erc20_swagger.json"), 50109)
    elif service == "genome-nexus":
        if tool == "evomaster-whitebox":
            whitebox(40110)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/genome_swagger.json", 50110)
        elif tool == "bboxrt" or tool == "tcases":
            blackbox(os.path.join(curdir, "doc/genome_openapi.yaml"), 50110)
        else:
            blackbox(os.path.join(curdir, "doc/genome_swagger.json"), 50110)
    elif service == "person-controller":
        if tool == "evomaster-whitebox":
            whitebox(40111)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/person_swagger.json", 50111)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/person_swagger.yaml"), 50111)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/person_openapi.yaml"), 50111)
        else:
            blackbox(os.path.join(curdir, "doc/person_swagger.json"), 50111)
    elif service == "problem-controller":
        if tool == "evomaster-whitebox":
            whitebox(40112)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/problem_swagger.json", 50112)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/problem_swagger.yaml"), 50112)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/problem_tcases.yaml"), 50112)
        else:
            blackbox(os.path.join(curdir, "doc/problem_swagger.json"), 50112)
    elif service == "rest-study":
        if tool == "evomaster-whitebox":
            whitebox(40113)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/rest_swagger.json", 50113)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/rest_swagger.yaml"), 50113)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/rest_tcases.yaml"), 50113)
        else:
            blackbox(os.path.join(curdir, "doc/rest_swagger.json"), 50113)
    elif service == "spring-batch-rest":
        if tool == "evomaster-whitebox":
            whitebox(40114)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/springbatch_swagger.json", 50114)
        else:
            blackbox(os.path.join(curdir, "doc/springbatch_openapi.yaml"), 50114)
    elif service == "spring-boot-sample-app":
        if tool == "evomaster-whitebox":
            whitebox(40115)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/springboot_swagger.json", 8080)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/springboot_swagger.yaml"), 8080)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/springboot_openapi.yaml"), 8080)
        else:
            blackbox(os.path.join(curdir, "doc/springboot_swagger.json"), 8080)
    elif service == "user-management":
        if tool == "evomaster-whitebox":
            whitebox(40116)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/user_swagger.json", 50115)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/user_swagger.yaml"), 50115)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/user_openapi.yaml"), 50115)
        else:
            blackbox(os.path.join(curdir, "doc/user_swagger.json"), 50115)
    elif service == "cwa-verification":
        if tool == "evomaster-whitebox":
            whitebox(40117)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/cwa_swagger.json", 50116)
        elif tool == "restler":
            blackbox(os.path.join(curdir, "doc/cwa_restler.json"), 50116)
        else:
            blackbox(os.path.join(curdir, "doc/cwa_openapi.yaml"), 50116)
    elif service == "market":
        if tool == "evomaster-whitebox":
            whitebox(40118)
        elif tool == "evomaster-blackbox":
            blackbox("https://raw.githubusercontent.com/randomqwerqwer/issta/main/market_swagger.json", 50117)
        elif tool == "bboxrt":
            blackbox(os.path.join(curdir, "doc/market_swagger.yaml"), 50117)
        elif tool == "tcases":
            blackbox(os.path.join(curdir, "doc/market_openapi.yaml"), 50117)
        else:
            blackbox(os.path.join(curdir, "doc/market_swagger.json"), 50117)
    elif service == "project-tracking-system":
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

    print(
        "Experiments are done. We are safely closing the service now. If you want to run more, please check if there is unclosed session. You can check it with 'tmux ls' command. To close the session, you can run 'tmux kill-sess -t {session name}'")

    time.sleep(180)
    subprocess.run("tmux kill-sess -t " + service, shell=True)