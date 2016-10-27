@ECHO off
SETLOCAL ENABLEEXTENSIONS

SET LIMIT=1

REM ProjectPath is one directory up from script bin directory.
SET PROJECTPATH=%~dp0..

SET CLASSPATH="%PROJECTPATH%\lib\*;%PROJECTPATH%\lib\dependency-jars\*;"

SET JAVA=java -cp %CLASSPATH% -Dlog4j.configuration="file:%PROJECTPATH%\conf\log4j.properties"

%JAVA% gov.uspto.bulkdata.cli2.BulkData --type application --years="2016" --limit=%LIMIT% --outdir="%PROJECTPATH%\download"
