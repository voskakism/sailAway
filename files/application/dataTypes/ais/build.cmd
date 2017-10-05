@echo off
cd classes
del /F /Q *.*
cd ../src
javac -cp ../../userInput/classes/;../../lowLevelTypes/classes -d ../classes *.java
pause