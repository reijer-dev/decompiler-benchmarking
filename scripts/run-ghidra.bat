SET THIS_SCRIPT_DIR=%~dp0%
::Does string have a trailing slash? if so remove it 
IF %THIS_SCRIPT_DIR:~-1%==\ SET THIS_SCRIPT_DIR=%THIS_SCRIPT_DIR:~0,-1%
echo %THIS_SCRIPT_DIR%

SET EXECUTABLE_NAME=%~nx1%
echo %EXECUTABLE_NAME%
SET POSTSCRIPT_DIR=%THIS_SCRIPT_DIR%\resources
SET POSTSCRIPT_NAME=ghidra_decompile.java
SET GHIDRA_NAME=ghidra_11.0_PUBLIC
SET RUNFOLDER=C:\software\%GHIDRA_NAME%

echo Ik start Ghidra...
call "%RUNFOLDER%\support\analyzeHeadless.bat" "%THIS_SCRIPT_DIR%" tmp_ghidra_project -import %1 -scriptPath "%POSTSCRIPT_DIR%" -postscript "%POSTSCRIPT_NAME%" %2 -deleteProject