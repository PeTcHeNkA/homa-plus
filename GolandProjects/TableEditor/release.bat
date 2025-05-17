@REM @echo off

set $GOARCH=amd64
set $GOOS=windows
go build -o build/langedit_release.exe -trimpath -ldflags "-s -w -buildid=" -gcflags=all="-l -B -wb=false -C" main.go LangPack.go

@pause