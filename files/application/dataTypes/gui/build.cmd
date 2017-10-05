@echo off
cd classes
del /F /Q *.*
cd ../src
javac -cp ../../configuration/classes;../../userInput/classes;../../lowLevelTypes/classes;../../ais/classes;. -d ../classes *.java
pause