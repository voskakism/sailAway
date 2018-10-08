@echo off
cd classes/
del /F /Q *.*
cd ../src/
javac -cp ../../configuration/classes/;../../exceptions/classes/;../../lowLevelTypes/classes/;../../ais/classes/ -d ../classes/ *.java
pause