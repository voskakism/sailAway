@echo off
cd classes
del /F /Q *.*
cd ../src
javac -cp ../../dataTypes/configuration/classes;../../dataTypes/userInput/classes;../../dataTypes/lowLevelTypes/classes;../../dataTypes/ais/classes;../../../esper/*;../../../esper/deps/*;. -d ../classes *.java
pause
:: ../../dataTypes/parsers/classes;../../dataTypes/exceptions/classes;../../dataTypes/gui/classes;