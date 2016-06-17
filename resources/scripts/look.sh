#!/bin/bash
#
#
# View a Patent Document contained within a Weekly Bulk Download File
#
#
FILE=ipa150101.zip
SKIP=0
LIMIT=1
FIELDS=id,title,description,ciations,claims,classification,family
# FIELDS=xml


# ProjectPath is one directory up from script bin directory.
PROJECTPATH=$( cd $(dirname $0)/.. ; pwd -P )

CLASSPATH="${PROJECTPATH}/lib/*:${PROJECTPATH}/lib/dependency-jars/*"

JAVA="java -cp ${CLASSPATH} -Dlog4j.configuration=file:${PROJECTPATH}/conf/log4j.properties"

#
# source    required; Patent Weekly Dump File to read from.  original Zipfile containing XML, or just the xml file on the file system.
# fields    required; comma seperated list of fields [id, title, abstract, description, citations, claims, classification, family], xml = prints full Patent Document XML.
# num       record number/location in bulk load zip
# id        patent id (slower than num lookup if you know its location)
# limit     max limit of records to read
# skip      number of records to skip
# out       write out file
#
${JAVA} gov.uspto.bulkdata.cli.Look --fields="${FIELDS}" --skip=${SKIP} --limit=${LIMIT} --source="${PROJECTPATH}/download/${FILE}"

