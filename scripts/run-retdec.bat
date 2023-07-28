echo generate-c - (c) 2023 Jaap van den Bos
IF "%1" EQU "" (
  echo Geen parameters opgegeven.
  exit /b 1
)

C:\Users\reije\Downloads\RetDec-v5.0-Windows-Release\bin\retdec-decompiler.exe %1%
echo CFILE=%~dp0%program.exe.c