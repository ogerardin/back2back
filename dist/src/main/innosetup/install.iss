;suppress inspection "DuplicateKeyInSection" for whole file

[Setup]
AppName=back2back
AppVerName=back2back 1.0
AppPublisher=Olivier Gérardin
AppPublisherURL=https://github.com/ogerardin/back2back
AppSupportURL=https://github.com/ogerardin/back2back
AppUpdatesURL=https://github.com/ogerardin/back2back
DefaultDirName={pf}\back2back
DefaultGroupName=back2back
LicenseFile=..\..\..\..\LICENSE.txt
OutputBaseFilename=back2back-setup
;Compression=lzma
; For debugging only
Compression=none
;SolidCompression=yes
OutputDir=..\..\..\target
; For debugging only
PrivilegesRequired=none

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Components]
Name: "core"; Description: "Back2back core engine"; Types: full compact custom; Flags: fixed
Name: "service"; Description: "Install as Windows service"; Types: full custom
Name: "tray"; Description: "Tray Icon"; Types: full custom

[Files]
Source: "..\..\..\target\dependency\back2back-bundle-standalone.jar"; DestDir: "{app}"; Flags: ignoreversion; Components: core
Source: "..\..\..\target\classes\startEngine.bat"; DestDir: "{app}"; Flags: ignoreversion; Components: core
Source: "..\..\..\target\dependency\back2back-system-tray-onejar.jar"; DestDir: "{app}"; Flags: ignoreversion; Components: tray
Source: "..\..\..\target\classes\startTrayIcon.bat"; DestDir: "{app}"; Flags: ignoreversion; Components: tray
Source: "..\..\..\target\nssm-2.24\*"; DestDir: "{app}\nssm"; Flags: ignoreversion recursesubdirs; Components: service
Source: "..\..\..\target\classes\installService.bat"; DestDir: "{app}"; Flags: ignoreversion; Components: service
Source: "..\..\..\target\classes\removeService.bat"; DestDir: "{app}"; Flags: ignoreversion; Components: service

[INI]
Filename: "{app}\back2back.url"; Section: "InternetShortcut"; Key: "URL"; String: "https://github.com/ogerardin/back2back"

[Icons]
Name: "{group}\{cm:ProgramOnTheWeb,back2back}"; Filename: "{app}\back2back.url"
Name: "{group}\{cm:UninstallProgram,back2back}"; Filename: "{uninstallexe}"

[Run]
Filename: "{app}\startEngine.bat"; Description: "Start back2back now"; Flags: postinstall; Components: not service
Filename: "{app}\installService.bat"; Description: "Install as service"; Flags: postinstall; Components: service
Filename: "{app}\startTrayIcon.bat"; Description: "Start Tray Icon"; Flags: postinstall; Components: tray

[Code]
function GetJavaMajorVersion(): integer;
var
  TempFile: string;
  ResultCode: Integer;
  S: AnsiString;
  P: Integer;
begin
  Result := 0;

  // execute java -version and redirect output to a temp file
  TempFile := ExpandConstant('{tmp}\javaversion.txt');
  if (not ExecAsOriginalUser(ExpandConstant('{cmd}'), '/c java -version 2> "' + TempFile + '"', '',SW_HIDE, ewWaitUntilTerminated, ResultCode)) 
    or (ResultCode <> 0) then
  begin
    Log('Failed to execute java -version');
    exit;
  end;
  
  // read file into variable S
  LoadStringFromFile(TempFile, S)
  DeleteFile(TempFile);
  Log(Format('java -version output: ' + #13#10 + '%s', [S]));

  // extract version (between quotes)
  P := Pos('"', S);
  Delete(S, 1, P);
  P := Pos('"', S);
  SetLength(S, P - 1);
  Log(Format('Extracted version: %s', [S]));

  // extract major
  if Copy(S, 1, 2) = '1.' then
  begin
    Delete(S, 1, 2)
  end;
  P := Pos('.', S);
  SetLength(S, P - 1);
  Log(Format('Major version: %s', [S]));

  Result := StrToIntDef(S, 0);
end;

function InitializeSetup(): boolean;
var
  ResultCode: Integer;
begin
  if GetJavaMajorVersion >= 8 then
  begin
    Result := true;    
    exit;
  end;

  if MsgBox('This application requires a Java Runtime Environment version 8 or newer to run. \
    Please download and install the JRE and run this setup again.' 
    + #13#10 + #13#10 
    + 'Would you like to open the JRE download page now?', mbCriticalError, MB_YESNO) = idYes then 
  begin
    Result := false;
    ShellExec('open', 'https://java.com/download/', '', '', SW_SHOWNORMAL, ewNoWait, ResultCode);
  end;  
end;

[UninstallDelete]
Type: files; Name: "{app}\back2back.url"

