::  ==========   Build order:   ==========
:: 1) configuration
:: 2) exceptions
:: 3) lowLevelTypes			|| Dependency   :		configuration
:: 4) userInput				|| Dependency   :		configuration, lowLevelTypes, exceptions, ais
:: 5) ais					|| Dependencies :		lowLevelTypes, userInput
:: 6) parsers				|| Dependencies :		lowLevelTypes, userInput, exceptions, ais, configuration
:: 6) gui					|| Dependencies :		lowLevelTypes, userInput, ais, configuration

:: WARNING: There seems to be a "co-dependency deadlock" that becomes apparent when the .class files of a previous build
:: are removed from the "classes" folders of the above subsystems; in other words, when a clean rebuild is attempted. 
::
:: After building "configuration", "exceptions" and "lowLevelTypes" uneventfully, the script gets to the 4th subsystem ("userInput") that contains the BroadcastSequence datatype.
:: BroadcastSequence references the Broadcast datatype, declared/defined in "ais", but it cannot find it...
:: Note that this is not a classpath-related error (-cp option is up-to-date in the current build script and the package-specific build scripts), rather,
:: the actual Broadcast.class file is missing, as the script has not gotten to that subsystem yet to compile sources, and since this is a clean build, there is no old .class file.
::
:: A workaround:
:: 1) Run the individual build scripts of "configuration", "exceptions" and "lowLevelTypes", in this order.
:: 2) Alter the file extension of BroadcastSequence.java so it will be ignored by "userInput"'s own build script.
:: 3) Run the individual build script of "userInput".
:: 4) Run the individual build script of "ais".
:: 5) Restore the correct extension of BroadcastSequence.java.
:: 6) Run the individual build script of "userInput".
:: 7) Run the individual build script of "parsers".
:: 7) Run the individual build script of "gui".
:: 8) Run this script. (optionally, just to make sure?)

@echo off

cd configuration/classes/
del /F /Q *.*
cd ../src/
javac -d ../classes/ *.java

cd ../../exceptions/classes/
del /F /Q *.*
cd ../src/
javac -d ../classes/ *.java

cd ../../lowLevelTypes/classes/
del /F /Q *.*
cd ../src/
javac -cp ../../configuration/classes/ -d ../classes/ *.java

cd ../../userInput/classes/
del /F /Q *.*
cd ../src/
javac -cp ../../configuration/classes/;../../exceptions/classes/;../../lowLevelTypes/classes/;../../ais/classes/ -d ../classes/ *.java

cd ../../ais/classes/
del /F /Q *.*
cd ../src/
javac -cp ../../lowLevelTypes/classes/;../../userInput/classes/ -d ../classes/ *.java

cd ../../parsers/classes/
del /F /Q *.*
cd ../src/
javac -cp ../../configuration/classes/;../../exceptions/classes/;../../lowLevelTypes/classes/;../../userInput/classes/;../../ais/classes/ -d ../classes/ *.java

cd ../../gui/classes/
del /F /Q *.*
cd ../src/
javac -cp ../../configuration/classes/;../../lowLevelTypes/classes/;../../userInput/classes/;../../ais/classes/ -d ../classes/ *.java

pause