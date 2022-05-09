import os
import sys
import json
import subprocess
from glob import glob

curdir = os.getcwd()

services = ["features-service", "languagetool", "ncs", "news", "ocvn", "proxyprint", "restcountries", "scout-api",
            "scs", "erc20-rest-service", "genome-nexus", "person-controller", "problem-controller", "rest-study",
            "spring-batch-rest", "spring-boot-sample-app", "user-management", "cwa-verification", "market",
            "project-tracking-system"]
paths = [
    "services/evo_jdk8/cs/rest/original/features-service",
    "services/evo_jdk8/cs/rest/original/languagetool",
    "services/evo_jdk8/cs/rest/artificial/ncs",
    "services/evo_jdk8/cs/rest/artificial/news",
    "services/evo_jdk8/cs/rest-gui/ocvn",
    "services/evo_jdk8/cs/rest/original/proxyprint",
    "services/evo_jdk8/cs/rest/original/restcountries",
    "services/evo_jdk8/cs/rest/original/scout-api",
    "services/evo_jdk8/cs/rest/artificial/scs",
    "services/jdk8/erc20-rest-service",
    "services/jdk8/genome-nexus",
    "services/jdk8/person-controller",
    "services/jdk8/problem-controller",
    "services/jdk8/rest-study",
    "services/jdk8/spring-batch-rest",
    "services/jdk8/spring-boot-sample-app",
    "services/jdk8/user-management",
    "services/jdk11/cwa-verification",
    "services/jdk11/market",
    "services/jdk11/project-tracking-system",
]

port = sys.argv[1]
name = services[19]
path = paths[19]

print(services[19] + " is processing....")
subdirs = [x[0] for x in os.walk(path)]
class_files = []
jacoco_command2 = ''

for subdir in subdirs:
    if services[19] in subdir and '/target/classes/' in subdir:
        target_dir = subdir[:subdir.rfind('/target/classes/') + 15]
        if target_dir not in class_files:
            class_files.append(target_dir)
            jacoco_command2 = jacoco_command2 + ' --classfiles ' + target_dir
    if services[19] in subdir and '/build/classes/' in subdir:
        target_dir = subdir[:subdir.rfind('/build/classes/') + 14]
        if target_dir not in class_files:
            class_files.append(target_dir)
            jacoco_command2 = jacoco_command2 + ' --classfiles ' + target_dir

jacoco_command2 = jacoco_command2 + ' --csv '

jacoco_command1 = 'java -jar org.jacoco.cli-0.8.7-nodeps.jar report '

files = [f for f in os.listdir(curdir)]
files.sort()

for k in range(6):
    count = 0
    errors = []
    time = []
    start_time = True
    error_start = False
    error_msg = ''
    error_time = ''
    for f in files:
        if str(port) in f:
            if count == 6:
                count = 0
            if 'jacoco' in f and '.exec' in f:
                count = count + 1
                jacoco_file = services[19] + '_' + str(k) + '_' + str(count) + '.csv'
                subprocess.run(jacoco_command1 + f + jacoco_command2 + jacoco_file, shell=True)
            elif 'log' in f:
                subprocess.call('split -l 10000000 ' + f, shell=True)
                ffs = [ff for ff in os.listdir('.') if os.path.isfile(ff)]
                ffs.sort()

                for ff in ffs:
                    if ff[0] == 'x':
                        with open(ff, 'r') as log_file:
                            data = log_file.readlines()
                        for line in data:
                            if 'ERROR' in line:
                                error_start = True
                                error_time = line[:line.rfind(':')]
                            elif error_start and 'java.lang.Thread.run' in line:
                                error_start = False
                                if error_msg not in errors:
                                    errors.append(error_msg)
                                    time.append(error_time)
                                error_msg = ''
                                error_time = ''
                            elif error_start and 'at ' in line:
                                error_msg = error_msg + line
                        print(len(errors))
                        with open('error_' + str(k) + '.json', 'w', encoding='UTF-8') as f:
                            json.dump(errors, f)
                        with open('time_' + str(k) + '.json', 'w', encoding='UTF-8') as f:
                            json.dump(time, f)
                subprocess.run("rm -rf x*", shell=True)

    subprocess.run("mkdir -p " + name + "_data/" + services[19], shell=True)
    subprocess.call('mv *.csv ' + name + "_data/" + services[19], shell=True)
    subprocess.call('mv error_' + str(k) + '.json ' + name + "_data/" + services[19], shell=True)
    subprocess.call('mv time_' + str(k) + '.json ' + name + "_data/" + services[19], shell=True)
    subprocess.call('mv jacoco*.exec ' + name + "_data/" + services[19], shell=True)
    subprocess.call('mv log* ' + name + "_data/" + services[19], shell=True)
