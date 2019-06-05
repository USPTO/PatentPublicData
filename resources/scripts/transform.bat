@ECHO off
SETLOCAL ENABLEEXTENSIONS

IF [%1]==[] GOTO NO_ARGUMENT

REM ProjectPath is one directory up from script bin directory.
SET PROJECTPATH=%~dp0..

REM SET JAVA_HOME=C:\Progra~1\Java\jdk1.8.0_72\jre\bin\java

SET FILE=%1
SET OUTPUTDIR=%PROJECTPATH%\download\transformed

REM  OUTPUT (raw, object, json, json_flat, text)
IF [%2]==[] (SET OUTPUT="json") else (SET OUTPUT="%2")

IF "%JAVA_HOME%" == "" GOTO JVM_NOT_FOUND
IF NOT EXIST %FILE% (SET FILE="%PROJECTPATH%/download/%FILE%")
IF NOT EXIST %FILE% GOTO FILE_NOT_FOUND

SET CLASSPATH="%PROJECTPATH%\lib\*;%PROJECTPATH%\lib\dependency-jars\*;"

SET GC_TUNE=-XX:NewRatio=3 ^
   -XX:SurvivorRatio=4 ^
   -XX:TargetSurvivorRatio=90 ^
   -XX:MaxTenuringThreshold=8 ^
   -XX:+UseConcMarkSweepGC ^
   -XX:+UseParNewGC ^
   -XX:ConcGCThreads=4 -XX:ParallelGCThreads=4 ^
   -XX:+CMSScavengeBeforeRemark ^
   -XX:PretenureSizeThreshold=64m ^
   -XX:+UseCMSInitiatingOccupancyOnly ^
   -XX:CMSInitiatingOccupancyFraction=50 ^
   -XX:CMSMaxAbortablePrecleanTime=6000 ^
   -XX:+CMSParallelRemarkEnabled ^
   -XX:+ParallelRefProcEnabled ^
   -XX:-OmitStackTraceInFastThrow

SET JAVA=%JAVA_HOME% -server -Xms1G -Xmx2G %GC_TUNE% -cp %CLASSPATH% -Dlog4j.configuration="file:%PROJECTPATH%\conf\log4j.properties"

IF NOT EXIST %OUTPUTDIR% (md %OUTPUTDIR%)

%JAVA% gov.uspto.bulkdata.cli.Transformer -f="%FILE%" --type="%OUTPUT%" --outDir="%OUTPUTDIR%" --outBulk=true --prettyPrint=false

GOTO DONE

:JVM_NOT_FOUND
echo ERROR: JAVA_HOME not set
exit /B

:FILE_NOT_FOUND
echo ERROR: File Not Found
exit /B

:NO_ARGUMENT
echo No Valid Arguments Provided
echo Example: transform.bat ipa180104.zip
exit /B

:DONE
