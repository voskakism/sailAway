@echo off
cd classes
del /F /Q *.*
cd ../src
javac -cp ../../dataTypes/configuration/classes;../../dataTypes/parsers/classes;../../dataTypes/userInput/classes;../../dataTypes/lowLevelTypes/classes;../../dataTypes/ais/classes;../../dataTypes/exceptions/classes;../../dataTypes/gui/classes;../../listeners/classes;../../../esper/*;../../../esper/deps/*;. -d ../classes *.java
pause