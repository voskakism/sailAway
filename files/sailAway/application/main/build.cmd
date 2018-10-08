@echo off
cd classes/
del /F /Q *.*
cd ../src/
javac -cp ../../dataTypes/configuration/classes/;../../dataTypes/exceptions/classes/;../../dataTypes/lowLevelTypes/classes/;../../dataTypes/userInput/classes/;../../dataTypes/ais/classes/;../../dataTypes/parsers/classes/;../../dataTypes/gui/classes/;../../listeners/classes/;../../../esper/* -d ../classes/ *.java
pause