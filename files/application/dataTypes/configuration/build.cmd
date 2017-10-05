@echo off
cd classes
del /F /Q *.*
cd ../src
javac -d ../classes *.java
pause