@echo off
javac -d bin -encoding UTF-8 src\up\mi\paa\*.java src\up\mi\paa\model\*.java src\up\mi\paa\exception\*.java src\up\mi\paa\service\*.java src\up\mi\paa\ui\*.java
java -cp bin up.mi.paa.Main
pause
