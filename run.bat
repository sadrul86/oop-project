@echo off
setlocal
if exist out rmdir /s /q out
mkdir out
if exist sources.txt del sources.txt
for /r src %%f in (*.java) do echo "%%f">>sources.txt
javac --add-modules jdk.httpserver -d out @sources.txt
if errorlevel 1 (
  del sources.txt
  exit /b 1
)
del sources.txt
java --add-modules jdk.httpserver -cp out com.university.research.Main
endlocal
