@ECHO off
SETLOCAL ENABLEEXTENSIONS

IF [%1]==[] GOTO NO_ARGUMENT
IF [%2]==[] GOTO NO_ARGUMENT

SET FILE=%1
REM  NUM is the iteration number of file within bulk file
SET NUM=%2
REM  OUTPUT (raw, object, json, json_flat, text)
IF [%3]==[] (SET OUTPUT="json") else (SET OUTPUT="%3")

REM ProjectPath is one directory up from script bin directory.
SET PROJECTPATH=%~dp0..

IF NOT EXIST %FILE% (SET FILE="%PROJECTPATH%/download/%FILE%")
IF NOT EXIST %FILE% GOTO FILE_NOT_FOUND
IF %2 LEQ 0 GOTO NO_ARGUMENT

SET CLASSPATH="%PROJECTPATH%\lib\*;%PROJECTPATH%\lib\dependency-jars\*;"

SET JAVA=java -cp %CLASSPATH% -Dlog4j.configuration="file:%PROJECTPATH%\conf\log4j.properties"

%JAVA% gov.uspto.bulkdata.cli.View -f="%FILE%" --num=%NUM% --type="%OUTPUT%"

GOTO DONE

:FILE_NOT_FOUND
echo File Not Found
exit /B

:NO_ARGUMENT
echo No Valid Arguments Provided
echo Example: view.bat ipa180104.zip 100 raw
exit /B

:DONE
