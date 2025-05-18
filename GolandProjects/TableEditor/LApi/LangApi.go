package main

import (
	"TableEditor/Pack"
)
import "C"

var packs map[int]*Pack.LangTable
var code1 = errCode(1)
var code2 = errCode(2)

func main() {}

//export LoadPack
func LoadPack(id int, data []byte) int {
	if packs == nil {
		packs = make(map[int]*Pack.LangTable)
	}
	var pack *Pack.LangTable

	if packs[id] == nil {
		pack = &Pack.LangTable{}
		packs[id] = pack
	} else {
		pack = packs[id]
	}
	err := pack.ReadFrom(data)
	if err != nil {
		return 2
	}
	return 0
}

//export FreePack
func FreePack(id int) int {
	if packs == nil {
		return 1
	}
	prev := packs[id]
	if prev == nil {
		return 1
	}
	delete(packs, id)
	return 0
}

//export ApplyMappings
func ApplyMappings(id int, data []byte) int {
	if packs == nil {
		return 1
	}
	pack := packs[id]
	if pack == nil {
		return 1
	}
	mappngs := Pack.LangMapping{}

	err := mappngs.ReadFrom(data)

	if err != nil {
		return 2
	}
	mappngs.ApplyFor(pack)
	return 0
}

//export ExportMappings
func ExportMappings(id int, actual bool) []byte {
	if packs == nil {
		return errCode(1)
	}
	pack := packs[id]
	if pack == nil {
		return code1
	}

	mappngs := Pack.LangMapping{}
	mappngs.UseOf(pack, actual)

	result, err := mappngs.Write()
	if err != nil {
		return code2
	}
	return result
}

//export WritePack
func WritePack(id int) []byte {
	if packs == nil {
		return code1
	}
	pack := packs[id]

	if pack == nil {
		return code1
	}

	result, err := pack.Write()
	if err != nil {
		return code2
	}
	return result
}

func errCode(code byte) []byte {
	data := make([]byte, 1)
	data[0] = code
	return data
}
