@REM @echo off

set $GOARCH=amd64
set $GOOS=windows
go build -o build/langedit.dll -trimpath -buildmode=c-shared -ldflags "-s -w -buildid=" -gcflags=all="-l -B -wb=false -C" LangApi.go LangPack.go

@pause