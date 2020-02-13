#!/bin/bash

USAGE(){
    echo "Usage: ./`basename $0` <application|grant> <DateRange>"
    echo "Usage: ./`basename $0` grant 20180101-20190101"
}

if [ "$#" -ne "2" ]; then
    USAGE
    exit 1
fi

## ProjectPath is one directory up from script bin directory.
PROJECTPATH=$( cd $(dirname $0)/.. ; pwd -P )

#----------------------
# Config
#----------------------
## Type (application , grant )
TYPE=$1
DATERANGE=$2

#----------------------
# Exec
#----------------------

CLASSPATH="${PROJECTPATH}/lib/*:${PROJECTPATH}/lib/dependency-jars/*"

JAVA="java -Xms1G -Xmx1G -cp ${CLASSPATH} -Dlog4j.configuration=file:${PROJECTPATH}/conf/log4j.properties"

${JAVA} gov.uspto.bulkdata.cli.Fetch -f="." --fetch-type="$TYPE" --outDir="${PROJECTPATH}/download" --fetch-date="$DATERANGE"
