#!/bin/bash

####### Install Java 8 or above.
##
#### UBUNTU
## apt-cache search java | grep openjdk
## sudo apt install default-jre
## sudo apt install openjdk-12-jre-headless
## sudo apt install openjdk-11-jre-headless
## sudo apt install openjdk-10-jre-headless
## sudo apt install openjdk-9-jre-headless
## sudo apt install openjdk-8-jre-headless
## JAVA_HOME=/usr/java/default
## JAVA_HOME=/usr/lib/jvm/java-12-openjdk-amd64
## JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
## JAVA_HOME=/usr/lib/jvm/java-10-openjdk-amd64
## JAVA_HOME=/usr/lib/jvm/java-9-openjdk-amd64
## JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
## JAVA_HOME=/usr/lib/jvm/java-8-oracle
## JAVA_HOME=/usr/lib/jvm/java-12-oracle
##
##### REDHAT / FEDORA
## yum search java | grep openjdk
## sudo yum install java-1.8.0-openjdk-headless
## sudo yum install java-11-openjdk-headless.x86_64
## JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
## sudo rpm -ivh jre-8u73-linux-x64.rpm
## JAVA_HOME=/usr/java

####### Set JAVA_HOME to match installed java
JAVA_HOME=/usr

USAGE(){
    echo "Usage: ./`basename $0` <FILE|DIRECTORY>"
    echo "Usage: ./`basename $0` <FILE|DIRECTORY> <OUTPUT>"
    echo "Usage: ./`basename $0` ipg180102.zip json"
}

if [ ! "$#" -ge "1" ]; then
    USAGE
    exit 1
fi

if [ -z ${JAVA_HOME} ]; then
	echo "JAVA_HOME not set"
	exit 1
fi

## ProjectPath is one directory up from script bin directory.
PROJECTPATH=$( cd $(dirname $0)/.. ; pwd -P )

#----------------------
# Config
#----------------------

FILE=$1

OUTPUTDIR=${PROJECTPATH}\download\transformed

## OUTPUT (raw, object, json, json_flat, text)
OUTPUT=${2:-json}

#----------------------
# Exec
#----------------------

if [ ! -e "$FILE" ]; then
   if [ -e "${PROJECTPATH}/download/${FILE}" ]; then
	  FILE = "${PROJECTPATH}/download/${FILE}"
   else
      echo "File $FILE not found!" >&2
	  exit 1;
   fi
fi

CLASSPATH="${PROJECTPATH}/lib/*:${PROJECTPATH}/lib/dependency-jars/*"

GC_TUNE=('-XX:NewRatio=3' \
        '-XX:SurvivorRatio=4' \
        '-XX:TargetSurvivorRatio=90' \
        '-XX:MaxTenuringThreshold=8' \
        '-XX:+UseConcMarkSweepGC' \
        '-XX:+UseParNewGC' \
        '-XX:ConcGCThreads=4' '-XX:ParallelGCThreads=4' \
        '-XX:+CMSScavengeBeforeRemark' \
        '-XX:PretenureSizeThreshold=64m' \
        '-XX:+UseCMSInitiatingOccupancyOnly' \
        '-XX:CMSInitiatingOccupancyFraction=50' \
        '-XX:CMSMaxAbortablePrecleanTime=6000' \
        '-XX:+CMSParallelRemarkEnabled' \
        '-XX:+ParallelRefProcEnabled' \
        '-XX:-OmitStackTraceInFastThrow')

JAVA="${JAVA_HOME}/bin/java -server -Xms1G -Xmx2G ${GC_TUNE} -cp ${CLASSPATH} -Dlog4j.configuration=file:${PROJECTPATH}/conf/log4j.properties"

${JAVA} gov.uspto.bulkdata.cli.Transformer -f="${FILE}" --type="${OUTPUT}" --outDir="${OUTPUTDIR}" --outBulk=true --prettyPrint=false

