package tutil

import (
	"os"
	"strings"
)

var MCINF = McInfo{
	//LaunchPatch: "C:/Users/Te4hnoPlus/GolandProjects/vwlaunch/mc",
	LaunchPatch: "C:/Users/Te4hnoPlus/Desktop/LC3",
	//LaunchPatch: "C:/Users/Te4hnoPlus/AppData/Roaming/.minecraft"
}

type McInfo struct {
	LaunchPatch string
}

func GetFiles(dir string, pref string) string {
	var files []os.DirEntry
	var err error

	files, err = os.ReadDir(dir)
	if err != nil {
		return ""
	}

	var list = ""

	for _, file := range files {
		if file.IsDir() {
			list += GetFiles(dir+"/"+file.Name(), pref+"/"+file.Name())
		} else {
			list += pref + "/" + file.Name() + ";"
		}
	}
	return list
}

func ReadFile(filename string) []byte {
	bytes, err := os.ReadFile(filename)
	if err != nil {
		return nil
	}
	return bytes
}

func CreateFile(filename string, data []byte) error {
	items := strings.Split(filename, "/")
	items = items[:len(items)-1]
	_ = os.MkdirAll(strings.Join(items, "/"), os.ModeDir)

	file, err := os.Create(filename)

	if err != nil {
		println(err.Error())
		return err
	}
	_, err2 := file.Write(data)
	if err2 != nil {
		println(err.Error())
		return err2
	}
	defer file.Close()

	return nil
}

func MakeDirs(dir string) {
	_ = os.MkdirAll(dir, os.ModeDir)
}

func SaveCache(sub string, data string) {
	dir0, err := os.UserCacheDir()
	if err != nil {
		return
	}
	_ = CreateFile(dir0+"/tehplus/vlaunch/"+sub, []byte(data))
}

func GetCache(sub string) string {
	dir0, err := os.UserCacheDir()
	if err != nil {
		return ""
	}
	data := ReadFile(dir0 + "/tehplus/vlaunch/" + sub)
	if data == nil {
		return ""
	}
	return string(data)
}
