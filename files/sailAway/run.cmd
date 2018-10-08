@echo off
cd application/main/classes/
java -cp ../../dataTypes/configuration/classes/;../../dataTypes/exceptions/classes/;../../dataTypes/lowLevelTypes/classes/;../../dataTypes/userInput/classes/;../../dataTypes/ais/classes/;../../dataTypes/parsers/classes/;../../dataTypes/gui/classes/;../../listeners/classes/;../../../esper/*;../../../esper/deps/*;. Application
pause