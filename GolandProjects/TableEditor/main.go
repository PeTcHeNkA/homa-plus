package main

import (
	"TableEditor/Pack"
	"errors"
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
		println(invalidMsg)
		os.Exit(1)
		return
	}

	args := make(map[string]*string)
	var key *string

	for i := 0; i < len(os.Args); i += 1 {
		cur := os.Args[i]
		if cur[0:1] == "-" {
			key = &cur
		} else {
			if key != nil {
				res := strings.ReplaceAll(cur, "\\", "/")
				args[*key] = &res
				key = nil
			}
		}
	}

	if args["-p"] == nil || args["-m"] == nil || args["-o"] == nil {
		println(invalidMsg)
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
			println(illegalSave, err)
			os.Exit(1)
			return
		}
	} else {
		if stat.IsDir() {
			println(illegalSave)
			os.Exit(1)
			return
		}
	}

	table := &Pack.LangTable{}
	mappings := &Pack.LangMapping{}

	var wg sync.WaitGroup
	wg.Add(1)

	go loadMappingsFromFile(&wg, mappings, *args["-m"])
	loadPackFromFile(&wg, table, *args["-p"])
	wg.Wait()

	mappings.ApplyFor(table)

	result, err := table.Write()
	if err != nil {
		println(illegalSave, err)
		os.Exit(1)
		return
	}

	err = os.WriteFile(out, result, fs.ModePerm)
	if err != nil {
		println(illegalSave, err)
		os.Exit(1)
		return
	}
	println("Success")
	os.Exit(0)
}

func loadPackFromFile(wg *sync.WaitGroup, langTable *Pack.LangTable, file string) {
	defer wg.Done()

	langPack, err := os.ReadFile(file)
	if err != nil {
		println(illegalReadPack, err)
		os.Exit(1)
		return
	}

	err = langTable.ReadFrom(langPack)
	if err != nil {
		println(illegalPackFormat, err)
		os.Exit(1)
		return
	}
	langTable.MakeIndex()
}

func loadMappingsFromFile(wg *sync.WaitGroup, mappings *Pack.LangMapping, file string) {
	wg.Add(1)
	defer wg.Done()

	mappingData, err := os.ReadFile(file)
	if err != nil {
		println(illegalReadMappings, err)
		os.Exit(1)
		return
	}
	err = mappings.ReadFrom(mappingData)
	if err != nil {
		println(illegalMappings, err)
		os.Exit(1)
		return
	}
}
