@echo off
cd classes/
del /F /Q *.*
cd ../src/
javac -cp ../../configuration/classes/;../../lowLevelTypes/classes/;../../userInput/classes/;../../ais/classes/ -d ../classes/ *.java
pause