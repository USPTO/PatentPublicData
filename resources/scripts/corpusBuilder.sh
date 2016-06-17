#!/bin/bash
#
#  Corpus Builder downloads USPTO Weekly Bulk Patent Dumps, 
#  keeping one at a time, extract out Patent Documents which match specified Patent Classifications.
#
#

# ProjectPath is one directory up from script bin directory.
PROJECTPATH=$( cd $(dirname $0)/.. ; pwd -P )

CLASSPATH="${PROJECTPATH}/lib/*:${PROJECTPATH}/lib/dependency-jars/*"

JAVA="java -cp ${CLASSPATH} -Dlog4j.configuration=file:${PROJECTPATH}/conf/log4j.properties"

#
#
# cpc, uspc     each can be a comma seperated list;  best to privide CPC and USPC classifications since CPC will only match recent documents and USPC are no longer provided in recent documents.
# years                   comma for individual years; dash for year range
# type [grant, application]
# eval [ xml, patent ]    defaults to xml,  xml = lookup patent classifications using XPATH on XML, patent = initatiate patent object and perform lookup.
# out [dummy, xml, zip]   defaults to xml,  dummy = outputs nothing, xml = single xml file, zip = single xml file in zip (note can not be interrupted else zip will be invalid)
# delete [true, false]    defaults to true, to delete each bulk dump once read.
#
${JAVA} gov.uspto.bulkdata.corpusbuilder.Corpus --type=grant --outdir="../download" --years=2006,2009 --uspc=725/000 --cpc=H04N21/00 --name=corpus --skip=0 --eval=xml --out=xml
