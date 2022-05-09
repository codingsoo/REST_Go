# sudo apt-get update && sudo apt-get install -y git && git clone https://github.com/codingsoo/REST_Go.git

# Install Common Utilities
sudo apt-get install -y software-properties-common \
unzip wget gcc git vim libcurl4-nss-dev tmux

# Install Java8
sudo apt-get install -y openjdk-8-jdk
sudo apt-get install -y maven gradle
sudo apt-get install -y openjdk-11-jdk

# Install Python3
sudo apt-get install -y python3-pip python3-virtualenv
virtualenv venv

# Install NodeJS and NPM
sudo apt-get install -y nodejs npm

# Install Dotnet SDK 5.0
wget https://packages.microsoft.com/config/ubuntu/20.04/packages-microsoft-prod.deb -O packages-microsoft-prod.deb \
&& sudo dpkg -i packages-microsoft-prod.deb \
&& rm packages-microsoft-prod.deb \
&& sudo apt-get update \
&& sudo apt-get install -y apt-transport-https \
&& sudo apt-get install -y dotnet-sdk-5.0

######project-tracking-system#####
cd services/jdk11/project-tracking-system && mvn clean install -DskipTests && mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
cd ../../..

######Testing Tools#####
# Install Dredd 14.1.0
sudo npm install -g dredd

# Install EvoMaster 1.3.0
wget https://github.com/EMResearch/EvoMaster/releases/download/v1.3.0/evomaster.jar.zip
unzip evomaster.jar.zip
rm evomaster.jar.zip

# Install Schemathesis 3.11.6
. ./venv/bin/activate && pip install schemathesis

# Install APIFuzzer 0.9.11
. ./venv/bin/activate && cd APIFuzzer && pip install .
cd ..

# Install RESTler 8.3.0
. ./venv/bin/activate \
&& wget https://github.com/microsoft/restler-fuzzer/archive/refs/tags/v8.3.0.tar.gz \
&& tar -xvf v8.3.0.tar.gz \
&& rm v8.3.0.tar.gz \
&& mv restler-fuzzer-8.3.0 restler \
&& cd restler \
&& mkdir restler_bin \
&& python ./build-restler.py --dest_dir ./restler_bin
cd ..

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Install RESTest 1.2.0 (need to install dependency as well)
cd ./RESTest && sh ./scripts/install_dependencies.sh && mvn clean install -DskipTests
cd ..

# Install bBOXRT (Commit e5d329133d51aa75cd39209590cac7046d0640b1)
cd ./bBOXRT && mvn install -DskipTests
cd ..

# Install RestTestGen
cd ./RestTestGen && chmod +x gradlew && ./gradlew jar
cd ..

wget https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.7/org.jacoco.agent-0.8.7-runtime.jar
wget https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.7/org.jacoco.cli-0.8.7-nodeps.jar

