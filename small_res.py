import os
import sys
import csv
import json
import subprocess
from glob import glob
from os import listdir
from os.path import isfile, join

services = ["features-service", "languagetool", "ncs", "news", "ocvn", "proxyprint", "restcountries", "scout-api",
            "scs", "erc20-rest-service", "genome-nexus", "person-controller", "problem-controller", "rest-study",
            "spring-batch-rest", "spring-boot-sample-app", "user-management", "cwa-verification", "market",
            "project-tracking-system"]
tools = ["evomaster-whitebox_data", "restler_data", "resttestgen_data", "restest_data", "bboxrt_data",
         "schemathesis_data", "tcases_data", "dredd_data", "evomaster-blackbox_data", "apifuzzer_data"]
class_name = ["app.coronawarn", "com.giassi.microservice", "com.test.sampleapp", "com.github.chrisgleissner",
              "org.restscs", "se.devscout.scoutapi", "com.in28minutes.rest.webservices.restfulwebservices",
              "eu.fayder.restcountries", "io.github.proxyprint.kitchen", "com.pfa.pack", "com.sw.project",
              "com.mongodb.starter", "org.devgateway", "org.tsdes.spring.examples.news", "org.restncs", "market",
              "org.languagetool", "org.cbioportal.genome_nexus", "org.javiermf.features", "io.blk.erc20"]

name = sys.argv[1]
curdir = os.getcwd()
res = ""
# for i in range(10):
for j in range(20):
    t_line = [0, 0, 0, 0, 0, 0]
    c_line = [0, 0, 0, 0, 0, 0]
    t_branch = [0, 0, 0, 0, 0, 0]
    c_branch = [0, 0, 0, 0, 0, 0]
    t_method = [0, 0, 0, 0, 0, 0]
    c_method = [0, 0, 0, 0, 0, 0]
    error = 0
    unique_err = 0
    crucial = 0
    mypath = os.path.join(curdir, name + "_data/" + services[j])
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
                        t_branch[0] = t_branch[0] + int(element[5]) + c_branch[0]
                        c_line[0] = c_line[0] + int(element[8])
                        t_line[0] = t_line[0] + int(element[7]) + c_line[0]
                        c_method[0] = c_method[0] + int(element[12])
                        t_method[0] = t_method[0] + c_method[0] + int(element[11])
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
                        t_branch[1] = t_branch[1] + int(element[5]) + c_branch[1]
                        c_line[1] = c_line[1] + int(element[8])
                        t_line[1] = t_line[1] + int(element[7]) + c_line[1]
                        c_method[1] = c_method[1] + int(element[12])
                        t_method[1] = t_method[1] + int(element[11]) + c_method[1]
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
                        t_branch[2] = t_branch[2] + int(element[5]) + c_branch[2]
                        c_line[2] = c_line[2] + int(element[8])
                        t_line[2] = t_line[2] + int(element[7]) + c_line[2]
                        c_method[2] = c_method[2] + int(element[12])
                        t_method[2] = t_method[2] + int(element[11]) + c_method[2]
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
                        t_branch[3] = t_branch[3] + int(element[5]) + c_branch[3]
                        c_line[3] = c_line[3] + int(element[8])
                        t_line[3] = t_line[3] + int(element[7]) + c_line[3]
                        c_method[3] = c_method[3] + int(element[12])
                        t_method[3] = t_method[3] + int(element[11]) + c_method[3]
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
                        t_branch[4] = t_branch[4] + int(element[5]) + c_branch[4]
                        c_line[4] = c_line[4] + int(element[8])
                        t_line[4] = t_line[4] + int(element[7]) + c_line[4]
                        c_method[4] = c_method[4] + int(element[12])
                        t_method[4] = t_method[4] + int(element[11]) + c_method[4]
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
                        t_branch[5] = t_branch[5] + int(element[5]) + c_branch[5]
                        c_line[5] = c_line[5] + int(element[8])
                        t_line[5] = t_line[5] + int(element[7]) + c_line[5]
                        c_method[5] = c_method[5] + int(element[12])
                        t_method[5] = t_method[5] + int(element[11]) + c_method[5]
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
    res = res + "0,0,0,0,0,0\n"

    for k in range(6):
        if t_line[k] != 0:
            line = c_line[k] / t_line[k]
        else:
            line = 0
        if t_branch[k] != 0:
            branch = c_branch[k] / t_branch[k]
        else:
            branch = 0
        if t_method[k] != 0:
            method = c_method[k] / t_method[k]
        else:
            method = 0
        res = res + str(line) + ',' + str(branch) + ',' + str(method) + ',' + str(error / 7) + ',' + str(
            unique_err / 7) + ',' + str(crucial / 7) + '\n'

with open('res.csv', 'w') as f:
    f.write(res)

subprocess.call('mv res.csv ' + name + "_data", shell=True)
