::@echo off
echo The Perfect Decompiler - (c) 2023 Reijer Klaasse

IF "%1" EQU "" (
  echo Geen binary opgegeven
  exit /b 1
)

IF "%2" EQU "" (
  echo Geen output file opgegeven
  exit /b 1
)

::Try to find source file
set ParentDir=%~p1
echo %ParentDir%
set ParentDir=%ParentDir: =:%
echo %ParentDir%
call :getparentdir %ParentDir%
::set ParentDir=%ParentDir::= %

set SourceFile=%ParentDir%\source.c
if not exist "%SourceFile%" (
  echo %SourceFile% niet gevonden!
  exit /b 1
)

copy "%SourceFile%" "%2"

goto :EOF

:getparentdir
if "%~1" EQU "" goto :EOF
Set ParentDir=%~1
shift
goto :getparentdir
