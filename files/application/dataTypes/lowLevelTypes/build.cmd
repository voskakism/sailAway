@echo off
cd classes
del /F /Q *.*
cd ../src
javac -cp ../../configuration/classes/ -d ../classes *.java
pause