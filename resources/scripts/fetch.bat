@ECHO off
SETLOCAL ENABLEEXTENSIONS

REM TYPE (application , grant)

SET TYPE="application"

SET DATERANGE="20180101-20190101"


REM ProjectPath is one directory up from script bin directory.
SET PROJECTPATH=%~dp0..

SET CLASSPATH="%PROJECTPATH%\lib\*;%PROJECTPATH%\lib\dependency-jars\*;"

SET JAVA=java -Xms1G -Xmx1G -cp %CLASSPATH% -Dlog4j.configuration="file:%PROJECTPATH%\conf\log4j.properties"

%JAVA% gov.uspto.bulkdata.cli.Fetch -f="." --fetch-type="%TYPE%" --outDir="%PROJECTPATH%\download" --fetch-date="%DATERANGE%"
