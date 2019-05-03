#!/bin/bash

USAGE(){
    echo "Usage: ./`basename $0` <FILE> <NUM>"
    echo "Usage: ./`basename $0` <FILE> <NUM> <OUTPUT>"
}

if [ ! "$#" -ge "2" ]; then
    USAGE
    exit 1
fi

# ProjectPath is one directory up from script bin directory.
PROJECTPATH=$( cd $(dirname $0)/.. ; pwd -P )

#----------------------
# Config
#----------------------

FILE=$1
NUM=${2:-1}
## OUTPUT (raw, object, json, json_flat, text)
OUTPUT=${3:-json}

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

SET CLASSPATH="%PROJECTPATH%\lib\*;%PROJECTPATH%\lib\dependency-jars\*;"

SET JAVA=java -cp %CLASSPATH% -Dlog4j.configuration="file:%PROJECTPATH%\conf\log4j.properties"

%JAVA% gov.uspto.bulkdata.cli.View -f="$FILE" --num=$NUM --type=$OUTPUT

