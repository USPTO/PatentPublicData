#!/bin/bash

LIMIT=1

# ProjectPath is one directory up from script bin directory.
PROJECTPATH=$( cd $(dirname $0)/.. ; pwd -P )

CLASSPATH="${PROJECTPATH}/lib/*:${PROJECTPATH}/lib/dependency-jars/*"

JAVA="java -cp ${CLASSPATH} -Dlog4j.configuration=file:${PROJECTPATH}/conf/log4j.properties"

#
# type: [application, grant]    requited;  patent document type
# years     required; comma for individual years; dash for year range
# limit     download limit
# skip      skip over limit
# filename  specific bulk file name to download
# outdir    directory to download to.
#
${JAVA} gov.uspto.bulkdata.cli2.BulkData --source google --type application --limit=${LIMIT} --outdir="${PROJECTPATH}/download"
