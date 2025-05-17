package main

import (
	"errors"
	"fmt"
	"io/fs"
	"os"
	"strings"
	"sync"
)

const invalidMsg = "Invalid arguments. Use '-p LangPack.bytes -m mappings.json -o OutPack.bytes' "
const illegalSave = "Illegal save path"
const illegalReadPack = "Illegal read pack"
const illegalReadMappings = "Illegal read mappings"
const illegalPackFormat = "Illegal pack format"
const illegalMappings = "Illegal mappings format"

func main() {
	count := len(os.Args)

	if count < 6 {
		fmt.Println(invalidMsg)
		os.Exit(1)
		return
	}

	args := make(map[string]*string)

	for i := 0; i < len(os.Args); i += 2 {
		res := strings.ReplaceAll(os.Args[i+1], "\\", "/")
		args[os.Args[i]] = &res
	}

	if args["-p"] == nil || args["-m"] == nil || args["-o"] == nil {
		fmt.Println(invalidMsg)
		os.Exit(1)
		return
	}

	out := *args["-o"]
	stat, err := os.Stat(out)

	if err != nil {
		if errors.Is(err, fs.ErrNotExist) {
			last := strings.LastIndex(out, "/")
			if last != -1 {
				dir0 := out[:last]
				err2 := os.MkdirAll(dir0, fs.ModePerm)
				if err2 != nil {
					return
				}
			}
		} else {
			fmt.Println(illegalSave, err)
			os.Exit(1)
			return
		}
	} else {
		if stat.IsDir() {
			fmt.Println(illegalSave)
			os.Exit(1)
			return
		}
	}

	table := &LangTable{}
	mappings := &LangMapping{}

	var wg sync.WaitGroup
	wg.Add(1)

	go loadMappingsFromFile(&wg, mappings, *args["-m"])
	loadPackFromFile(&wg, table, *args["-p"])
	wg.Wait()

	mappings.applyFor(table)

	result, err := table.writeTo()
	if err != nil {
		fmt.Println(illegalSave, err)
		os.Exit(1)
		return
	}

	err = os.WriteFile(out, result, fs.ModePerm)
	if err != nil {
		fmt.Println(illegalSave, err)
		os.Exit(1)
		return
	}
	fmt.Println("Success")
	os.Exit(0)
}

func loadPackFromFile(wg *sync.WaitGroup, langTable *LangTable, file string) {
	defer wg.Done()

	langPack, err := os.ReadFile(file)
	if err != nil {
		fmt.Println(illegalReadPack, err)
		os.Exit(1)
		return
	}

	err = langTable.readFrom(langPack)
	if err != nil {
		fmt.Println(illegalPackFormat, err)
		os.Exit(1)
		return
	}
	langTable.makeIndex()
}

func loadMappingsFromFile(wg *sync.WaitGroup, mappings *LangMapping, file string) {
	wg.Add(1)
	defer wg.Done()

	mappingData, err := os.ReadFile(file)
	if err != nil {
		fmt.Println(illegalReadMappings, err)
		os.Exit(1)
		return
	}
	err = mappings.readFrom(mappingData)
	if err != nil {
		fmt.Println(illegalMappings, err)
		os.Exit(1)
		return
	}
}
