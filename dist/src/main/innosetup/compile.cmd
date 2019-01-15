@echo off
REM
REM Generate the Windows installer using Inno Setup
REM Prerequisite: scoop must be installed
REM

REM Make sure Inno Setup command line compiler is installed
cmd /c scoop install iscc.json

REM Compile the installer
iscc install.iss