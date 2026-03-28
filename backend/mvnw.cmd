@echo off
REM Maven Start Up Batch script
set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

%JAVA_HOME%\bin\java %MAVEN_OPTS% -jar %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
