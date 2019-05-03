#!/bin/bash

USAGE(){
    echo "Usage: ./`basename $0` <FILE|DIRECTORY>"
    echo "Usage: ./`basename $0` <FILE|DIRECTORY> <OUTPUT>"
    echo "Usage: ./`basename $0` ipg180102.zip json"
}

if [ ! "$#" -ge "1" ]; then
    USAGE
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

if [ ! -f "$FILE" ]; then
   if [ -f "${PROJECTPATH}/download/${FILE}" ]; then
	  FILE = "${PROJECTPATH}/download/${FILE}"
   else
      echo "File $FILE not found!" >&2
	  exit 1;
   fi
fi

CLASSPATH="${PROJECTPATH}/lib/*:${PROJECTPATH}/lib/dependency-jars/*"

JAVA="java -Xms1G -Xmx1G -cp ${CLASSPATH} -Dlog4j.configuration=file:${PROJECTPATH}/conf/log4j.properties"

${JAVA} gov.uspto.bulkdata.cli.Transformer -f="${FILE}" --type="${OUTPUT}" --outDir="${OUTPUTDIR}" --outBulk=true --prettyPrint=false

