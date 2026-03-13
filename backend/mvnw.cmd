@echo off
rem Maven Wrapper startup script for Windows

set MAVEN_OPTS=-Xmx512m

rem Find the project root
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set BASE_DIR=%DIRNAME%

rem Detect MAVEN_CMD_LINE_ARGS
set MAVEN_CMD_LINE_ARGS=%*

rem Find maven
if not exist "%MAVEN_HOME%" goto readMavenHome
set MAVEN_HOME=%MAVEN_HOME%
:readMavenHome
if not exist "%MAVEN_BIN%" set MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd

rem Execute maven
"%MAVEN_BIN%" %MAVEN_CMD_LINE_ARGS%
