@echo off

for %%i in ("%~dp0.") do SET "B2BHOME=%%~fi"

reg Query "HKLM\Hardware\Description\System\CentralProcessor\0" | find /i "x86" > NUL && set BITNESS=32 || set BITNESS=64
set PATH=%B2BHOME%\nssm\win%BITNESS%;%PATH%

nssm install @back2back.service-name@ "java -jar %B2BHOME%\@back2back.core.jar@"
nssm set @back2back.service-name@ AppDirectory %B2BHOME%
nssm start @back2back.service-name@
