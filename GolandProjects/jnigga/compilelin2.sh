export PATH=$PATH:/home/homa/go/bin
export GOPATH=/mnt/c/Users/Te4hnoPlus/GolandProjects/jnigga
export GOARCH=amd64
export GOOS=linux
export CGO_ENABLED=1

go build -ldflags "-s -w" -buildmode=c-shared -o nlaunch/nlaunch.so NLaunch.go jproxy_linux.go utils.go