@echo off
@REM -gcflags=all="-l -B"
set $GOARCH=amd64
set $GOOS=linux
garble -tiny -literals build -ldflags "-s -w" -buildvcs=false -buildmode=c-shared -o nlaunch/nlaunch.so NLaunch.go jproxy_linux.go utils.go