@echo off

for %%i in ("%~dp0.") do SET "B2BHOME=%%~fi"

start /b javaw -jar %B2BHOME%\@back2back.tray-icon.jar@
