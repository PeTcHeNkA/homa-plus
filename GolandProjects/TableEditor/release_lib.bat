@REM @echo off

set $GOARCH=amd64
set $GOOS=windows
set CGO_ENABLED=1
go build -o build/langedit.dll -trimpath -buildmode=c-shared -ldflags "-s -w" -gcflags=all="-l -B -wb=false" LApi/LangApi.go

@pause