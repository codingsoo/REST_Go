import os
import sys
import json
import subprocess
from os import listdir
from os.path import isfile, join

curdir = os.getcwd()

services = ["features-service", "languagetool", "ncs", "news", "ocvn", "proxyprint", "restcountries", "scout-api",
            "scs", "erc20-rest-service", "genome-nexus", "person-controller", "problem-controller", "rest-study",
            "spring-batch-rest", "spring-boot-sample-app", "user-management", "cwa-verification", "market",
            "project-tracking-system"]
tools = ["evomaster-whitebox_data", "restler_data", "resttestgen_data", "restest_data", "bboxrt_data", "schemathesis_data", "tcases_data", "dredd_data", "evomaster-blackbox_data", "apifuzzer_data"]
class_name = ["app.coronawarn", "com.giassi.microservice", "com.test.sampleapp", "com.github.chrisgleissner", "org.restscs", "se.devscout.scoutapi", "com.in28minutes.rest.webservices.restfulwebservices", "eu.fayder.restcountries", "io.github.proxyprint.kitchen", "com.pfa.pack", "com.sw.project", "com.mongodb.starter", "org.devgateway", "org.tsdes.spring.examples.news", "org.restncs", "market", "org.languagetool", "org.cbioportal.genome_nexus", "org.javiermf.features", "io.blk.erc20"]

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
name = sys.argv[2]
k = 0
for i in range(len(services)):
    if name == services[i]:
        k = i

path = paths[k]

print(services[k] + " is processing....")
subdirs = [x[0] for x in os.walk(path)]
class_files = []
jacoco_command2 = ''

for subdir in subdirs:
    if services[k] in subdir and '/target/classes/' in subdir:
        target_dir = subdir[:subdir.rfind('/target/classes/') + 15]
        if target_dir not in class_files:
            class_files.append(target_dir)
            jacoco_command2 = jacoco_command2 + ' --classfiles ' + target_dir
    if services[k] in subdir and '/build/classes/' in subdir:
        target_dir = subdir[:subdir.rfind('/build/classes/') + 14]
        if target_dir not in class_files:
            class_files.append(target_dir)
            jacoco_command2 = jacoco_command2 + ' --classfiles ' + target_dir

jacoco_command2 = jacoco_command2 + ' --csv '

jacoco_command1 = 'java -jar org.jacoco.cli-0.8.7-nodeps.jar report '

files = [f for f in os.listdir(curdir)]
files.sort()


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
            jacoco_file = services[k] + '_' + str(count) + '.csv'
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
                        if 'ERROR ' in line:
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
                    with open('error.json', 'w', encoding='UTF-8') as f:
                        json.dump(errors, f)
                    with open('time.json', 'w', encoding='UTF-8') as f:
                        json.dump(time, f)
            subprocess.run("rm -rf x*", shell=True)

curdir = os.getcwd()
res = ""

t_line = [0, 0, 0, 0, 0, 0]
c_line = [0, 0, 0, 0, 0, 0]
t_branch = [0, 0, 0, 0, 0, 0]
c_branch = [0, 0, 0, 0, 0, 0]
t_method = [0, 0, 0, 0, 0, 0]
c_method = [0, 0, 0, 0, 0, 0]
error = 0
unique_err = 0
crucial = 0
mypath = os.path.join(curdir, "data/" + services[k])
if os.path.isdir(mypath):
    onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
    for dir_file in onlyfiles:
        if '_1.csv' in dir_file:
            with open(os.path.join(mypath, dir_file)) as f:
                lines = f.readlines()
                start = False
                for line in lines:
                    if not start:
                        start = True
                        continue
                    element = line.split(',')
                    if "EMDriver" in element[2] or "EmbeddedControl" in element[2]:
                        continue
                    c_branch[0] = c_branch[0] + int(element[6])
                    t_branch[0] = t_branch[0] + int(element[5]) + int(element[6])
                    c_line[0] = c_line[0] + int(element[8])
                    t_line[0] = t_line[0] + int(element[7]) + int(element[8])
                    c_method[0] = c_method[0] + int(element[12])
                    t_method[0] = t_method[0] + int(element[12]) + int(element[11])
        elif '_2.csv' in dir_file:
            with open(os.path.join(mypath, dir_file)) as f:
                lines = f.readlines()
                start = False
                for line in lines:
                    if not start:
                        start = True
                        continue
                    element = line.split(',')
                    if "EMDriver" in element[2] or "EmbeddedControl" in element[2]:
                        continue
                    c_branch[1] = c_branch[1] + int(element[6])
                    t_branch[1] = t_branch[1] + int(element[5]) + int(element[6])
                    c_line[1] = c_line[1] + int(element[8])
                    t_line[1] = t_line[1] + int(element[7]) + int(element[8])
                    c_method[1] = c_method[1] + int(element[12])
                    t_method[1] = t_method[1] + int(element[11]) + int(element[12])
        elif '_3.csv' in dir_file:
            with open(os.path.join(mypath, dir_file)) as f:
                lines = f.readlines()
                start = False
                for line in lines:
                    if not start:
                        start = True
                        continue
                    element = line.split(',')
                    if "EMDriver" in element[2] or "EmbeddedControl" in element[2]:
                        continue
                    c_branch[2] = c_branch[2] + int(element[6])
                    t_branch[2] = t_branch[2] + int(element[5]) + int(element[6])
                    c_line[2] = c_line[2] + int(element[8])
                    t_line[2] = t_line[2] + int(element[7]) + int(element[8])
                    c_method[2] = c_method[2] + int(element[12])
                    t_method[2] = t_method[2] + int(element[11]) + int(element[12])
        elif '_4.csv' in dir_file:
            with open(os.path.join(mypath, dir_file)) as f:
                lines = f.readlines()
                start = False
                for line in lines:
                    if not start:
                        start = True
                        continue
                    element = line.split(',')
                    if "EMDriver" in element[2] or "EmbeddedControl" in element[2]:
                        continue
                    c_branch[3] = c_branch[3] + int(element[6])
                    t_branch[3] = t_branch[3] + int(element[5]) + int(element[6])
                    c_line[3] = c_line[3] + int(element[8])
                    t_line[3] = t_line[3] + int(element[7]) + int(element[8])
                    c_method[3] = c_method[3] + int(element[12])
                    t_method[3] = t_method[3] + int(element[11]) + int(element[12])
        elif '_5.csv' in dir_file:
            with open(os.path.join(mypath, dir_file)) as f:
                lines = f.readlines()
                start = False
                for line in lines:
                    if not start:
                        start = True
                        continue
                    element = line.split(',')
                    if "EMDriver" in element[2] or "EmbeddedControl" in element[2]:
                        continue
                    c_branch[4] = c_branch[4] + int(element[6])
                    t_branch[4] = t_branch[4] + int(element[5]) + int(element[6])
                    c_line[4] = c_line[4] + int(element[8])
                    t_line[4] = t_line[4] + int(element[7]) + int(element[8])
                    c_method[4] = c_method[4] + int(element[12])
                    t_method[4] = t_method[4] + int(element[11]) + int(element[12])
        elif '_6.csv' in dir_file:
            with open(os.path.join(mypath, dir_file)) as f:
                lines = f.readlines()
                start = False
                for line in lines:
                    if not start:
                        start = True
                        continue
                    element = line.split(',')
                    if "EMDriver" in element[2] or "EmbeddedControl" in element[2]:
                        continue
                    c_branch[5] = c_branch[5] + int(element[6])
                    t_branch[5] = t_branch[5] + int(element[5]) + int(element[6])
                    c_line[5] = c_line[5] + int(element[8])
                    t_line[5] = t_line[5] + int(element[7]) + int(element[8])
                    c_method[5] = c_method[5] + int(element[12])
                    t_method[5] = t_method[5] + int(element[11]) + int(element[12])
        elif 'error' in dir_file:
            with open(os.path.join(mypath, dir_file), 'r') as f:
                data = json.load(f)
            unique = []
            for d in data:
                uniq = d[:d.find('\n')]
                if uniq not in unique:
                    unique.append(uniq)

            error = error + len(data)
            unique_err = unique_err + len(unique)

            for uni in unique:
                flag = True
                for n in class_name:
                    if n in uni:
                        flag = False
                if flag:
                    crucial = crucial + 1
res = ""

for k in range(6):
    if t_line[k] != 0:
        line = c_line[k]/t_line[k]
    else:
        line = 0
    if t_branch[k] != 0:
        branch = c_branch[k]/t_branch[k]
    else:
        branch = 0
    if t_method[k] != 0:
        method = c_method[k]/t_method[k]
    else:
        method = 0
    res = res + str(line*100) + '%,' + str(branch*100) + '%,' + str(method*100) + '%\n'
res = res + str(error) + ',' + str(unique_err) + ',' + str(crucial) + '\n'

with open('res.csv', 'w') as f:
    f.write(res)


subprocess.run("mkdir -p " + "data/" + services[k], shell=True)
subprocess.call('mv res.csv ' + "data/" + services[k], shell=True)
subprocess.call('mv *.csv ' + "data/" + services[k], shell=True)
subprocess.call('mv error.json ' + "data/" + services[k], shell=True)
subprocess.call('mv time.json ' + "data/" + services[k], shell=True)
subprocess.call('mv jacoco*.exec ' + "data/" + services[k], shell=True)
subprocess.call('mv log* ' + "data/" + services[k], shell=True)
