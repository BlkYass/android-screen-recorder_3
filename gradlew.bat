@echo off
set GRADLE_HOME=%~dp0\gradle
java -jar "%GRADLE_HOME%\wrapper\gradle-wrapper.jar" %*
