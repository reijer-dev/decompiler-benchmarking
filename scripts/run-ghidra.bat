echo generate-c - (c) 2023 Jaap van den Bos
IF "%1" EQU "" (
  echo Geen parameters opgegeven.
  exit /b 1
)

SET CDIR=%~dp0%
::Does string have a trailing slash? if so remove it 
IF %CDIR:~-1%==\ SET CDIR=%CDIR:~0,-1%
echo %CDIR%

SET NAME=%~nx1%
echo %NAME%
SET JAVASCRIPT=Decompile
SET GHIDRA=ghidra_10.3_PUBLIC
SET DELFOLDER=~/.ghidra/.%GHIDRA%
SET RUNFOLDER=C:\Users\reije\Downloads\%GHIDRA%

echo Ik wis eerst alle %JAVASCRIPT%.class-bestanden...
cd %DELFOLDER%
for /R %%f in (%JAVASCRIPT%.class) do del %%f /s/q

echo Ik start Ghidra...
cd "%RUNFOLDER%"
call support\analyzeHeadless.bat %CDIR% tmp_ghidra_project -import %1 -scriptPath %cd%\MyScript -postscript %JAVASCRIPT%.java "%CDIR%\%NAME%-ghidra-decompiled.c" -deleteProject
cd %CDIR%
echo Moving "%1-ghidra-decompiled.c" to "%2"
move "%1-ghidra-decompiled.c" "%2"