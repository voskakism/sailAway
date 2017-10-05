@echo off
cd classes
del /F /Q *.*
cd ../src
javac -cp ../../ais/classes/;../../lowLevelTypes/classes/;../../exceptions/classes/;../../configuration/classes/ -d ../classes *.java
pause