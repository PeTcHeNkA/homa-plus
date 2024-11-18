@echo off
@REM -gcflags=all="-l -B"
set $GOARCH=amd64
set $GOOS=windows
set CGO_ENABLED=1
garble -tiny -literals build -ldflags "-s -w" -buildvcs=false -buildmode=c-shared -o nlaunch/nlaunch.dll NLaunch.go jproxy.go utils.go
cd ./nlaunch
upx --best --lzma nlaunch.dll
echo Compiled success