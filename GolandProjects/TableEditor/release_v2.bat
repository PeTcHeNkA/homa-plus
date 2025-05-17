



gccgo -o build/langedit_release.exe -trimpath -ldflags "-s -w"  -c main.go LangPack.go

-compiler gccgo