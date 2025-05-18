set $GOARCH=amd64
set $GOOS=windows
go build -o build/langedit.dll -buildmode=c-shared LApi/LangApi.go

@pause