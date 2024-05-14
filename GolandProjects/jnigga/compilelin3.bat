set $GOARCH=amd64
set $GOOS=linux
go build -ldflags "-s -w" -buildmode=c-shared -o nlaunch/nlaunch.so NLaunch.go jproxy_linux.go utils.go