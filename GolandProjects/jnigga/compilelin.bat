set $GOARCH=amd64
set $GOOS=linux
set CGO_ENABLED=1
garble -tiny -literals build -ldflags "-s -w" -buildvcs=false -buildmode=c-shared -o nlaunch/nlaunch.so NLaunch.go jproxy_linux.go utils.go