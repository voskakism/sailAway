@echo off
cd classes/
del /F /Q *.*
cd ../src/
javac -cp ../../dataTypes/configuration/classes/;../../dataTypes/lowLevelTypes/classes/;../../dataTypes/userInput/classes/;../../../esper/* -d ../classes/ *.java
pause