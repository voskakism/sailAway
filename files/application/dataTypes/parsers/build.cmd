@echo off
cd classes
del /F /Q *.*
cd ../src
javac -cp ../../configuration/classes;../../ais/classes;../../lowLevelTypes/classes;../../userInput/classes;../../exceptions/classes -d ../classes *.java
pause