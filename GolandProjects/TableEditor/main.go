package main

import (
	"TableEditor/Pack"
	"errors"
	"io/fs"
	"os"
	"strings"
	"sync"
)

const invalidMsg = "Invalid arguments. Use '-p LangPack.bytes -m mappings.json -o OutPack.bytes' or `-map`"
const illegalMsg2 = "Incalid arguments. Use '-map -k LangPackEN.bytes -v LangPackRU.bytes -o mappings.json'"
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
	args := parseSysArgs()

	if args["-map"] != nil {
		buildMappings(args)
	} else {
		buildPack(args)
	}
	println("Success")
	os.Exit(0)
}

func buildMappings(args map[string]*string) {
	keys := args["-k"]
	values := args["-v"]

	if keys == nil {
		println(illegalMsg2)
		os.Exit(1)
		return
	}

	if values == nil {
		values = keys
	}

	out := outFile(args)

	pack1 := &Pack.LangTable{}
	var pack2 *Pack.LangTable = nil

	var wg sync.WaitGroup
	if *keys != *values {
		pack2 = &Pack.LangTable{}

		wg.Add(2)
		go loadPackFromFile(&wg, pack1, *keys)
		go loadPackFromFile(&wg, pack2, *values)
		wg.Wait()
	} else {
		pack2 = pack1
		wg.Add(1)
		loadPackFromFile(&wg, pack1, *keys)
	}

	mappngs := Pack.LangMapping{}
	mappngs.UseOf(pack1, pack2, boolOf(args, "-actual", false), boolOf(args, "-use-id", false))

	result, err := mappngs.Write()
	if err != nil {
		println(err.Error(), err)
		os.Exit(1)
		return
	}
	writeOut(out, result)
}

func buildPack(args map[string]*string) {
	if args["-p"] == nil || args["-m"] == nil || args["-o"] == nil {
		println(invalidMsg)
		os.Exit(1)
		return
	}

	out := outFile(args)

	table := &Pack.LangTable{}
	mappings := &Pack.LangMapping{}

	var wg sync.WaitGroup
	wg.Add(2)

	go loadMappingsFromFile(&wg, mappings, *args["-m"])
	loadPackFromFile(&wg, table, *args["-p"])
	wg.Wait()

	mappings.ApplyFor(table)

	result, err := table.Write()
	if err != nil {
		println(err.Error(), err)
		os.Exit(1)
		return
	}

	writeOut(out, result)
}

func writeOut(out string, data []byte) {
	err := os.WriteFile(out, data, fs.ModePerm)
	if err != nil {
		println(illegalSave+err.Error(), err)
		os.Exit(1)
		return
	}
}

func outFile(args map[string]*string) string {
	out := *args["-o"]
	stat, err := os.Stat(out)

	if err != nil {
		if errors.Is(err, fs.ErrNotExist) {
			last := strings.LastIndex(out, "/")
			if last != -1 {
				dir0 := out[:last]
				err2 := os.MkdirAll(dir0, fs.ModePerm)
				if err2 != nil {
					println(illegalSave, err)
					os.Exit(1)
				}
			}
		} else {
			println(illegalSave, err)
			os.Exit(1)
		}
	} else {
		if stat.IsDir() {
			println(illegalSave)
			os.Exit(1)
		}
	}
	return out
}

func boolOf(args map[string]*string, key string, def bool) bool {
	res := args[key]
	if res == nil {
		return def
	}
	if *res == "" {
		return !def
	}
	return *res == "true"
}

func parseSysArgs() map[string]*string {
	args := make(map[string]*string)
	var key *string
	empty := ""

	for i := 0; i < len(os.Args); i += 1 {
		cur := os.Args[i]
		if cur[0:1] == "-" {
			if key != nil {
				args[*key] = &empty
			}
			key = &cur
		} else {
			if key != nil {
				res := strings.ReplaceAll(cur, "\\", "/")
				args[*key] = &res
				key = nil
			}
		}
	}
	return args
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
