@echo off
set $GOARCH=amd64
set $GOOS=windows
go build -ldflags "-s -w" -gcflags=all="-l -B" -buildvcs=false -buildmode=c-shared -o nlaunch/nlaunch.dll NLaunch.go jproxy.go utils.go
cd ./nlaunch
upx --best --lzma nlaunch.dll
echo Compiled success