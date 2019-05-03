@ECHO off
SETLOCAL ENABLEEXTENSIONS

IF [%1]==[] GOTO NO_ARGUMENT

REM ProjectPath is one directory up from script bin directory.
SET PROJECTPATH=%~dp0..

SET FILE=%1
SET OUTPUTDIR=%PROJECTPATH%\download\transformed

REM  OUTPUT (raw, object, json, json_flat, text)
IF [%2]==[] (SET OUTPUT="json") else (SET OUTPUT="%2")

IF NOT EXIST %FILE% (SET FILE="%PROJECTPATH%/download/%FILE%")
IF NOT EXIST %FILE% GOTO FILE_NOT_FOUND

SET CLASSPATH="%PROJECTPATH%\lib\*;%PROJECTPATH%\lib\dependency-jars\*;"

SET JAVA=java -cp %CLASSPATH% -Dlog4j.configuration="file:%PROJECTPATH%\conf\log4j.properties"

IF NOT EXIST %OUTPUTDIR% (md %OUTPUTDIR%)

%JAVA% gov.uspto.bulkdata.cli.Transformer -f="%FILE%" --type="%OUTPUT%" --outDir="%OUTPUTDIR%" --outBulk=true --prettyPrint=false

GOTO DONE

:FILE_NOT_FOUND
echo File Not Found
exit /B

:NO_ARGUMENT
echo No Valid Arguments Provided
echo Example: transform.bat ipa180104.zip
exit /B

:DONE
