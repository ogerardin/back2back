@echo off
setlocal

for %%i in ("%~dp0.") do SET "B2BHOME=%%~fi"

reg Query "HKLM\Hardware\Description\System\CentralProcessor\0" | find /i "x86" > NUL && set BITNESS=32|| set BITNESS=64
set PATH=%B2BHOME%\nssm\win%BITNESS%;%PATH%

nssm stop @back2back.service-name@
nssm remove @back2back.service-name@ confirm

endlocal