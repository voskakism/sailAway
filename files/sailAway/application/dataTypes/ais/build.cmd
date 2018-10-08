@echo off
cd classes/
del /F /Q *.*
cd ../src/
javac -cp ../../lowLevelTypes/classes/;../../userInput/classes/ -d ../classes/ *.java
pause