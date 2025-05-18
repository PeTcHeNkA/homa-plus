package main

import (
	"TableEditor/Pack"
)
import "C"

var packs map[int]*tableField

func main() {}

type tableField struct {
	table *Pack.LangTable
	cache []byte
}

//export LoadPack
func LoadPack(id int, data []byte) int {
	if packs == nil {
		packs = make(map[int]*tableField)
	}
	var pack *Pack.LangTable

	if packs[id] == nil {
		pack = &Pack.LangTable{}
		packs[id] = &tableField{table: pack}
	} else {
		pack = packs[id].table
	}
	err := pack.ReadFrom(data)
	if err != nil {
		return -2
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
	mappngs.ApplyFor(pack.table)
	return 0
}

//export ExportMappings
func ExportMappings(kId int, valId int, actual bool, useId bool) int {
	if packs == nil {
		return -1
	}
	pack := packs[kId]
	if pack == nil {
		return -1
	}
	pack2 := packs[valId]
	if pack2 == nil {
		return -1
	}

	mappngs := Pack.LangMapping{}
	mappngs.UseOf(pack.table, pack2.table, actual, useId)

	result, err := mappngs.Write()
	if err != nil {
		return -2
	}
	pack.cache = result
	return len(result)
}

//export FlushTo
func FlushTo(id int, data []byte) int {
	if packs == nil {
		return -1
	}
	pack := packs[id]

	if pack == nil {
		return -1
	}
	if pack.cache == nil {
		return -2
	}
	copy(data, pack.cache)
	pack.cache = nil
	return 0
}

//export WritePack
func WritePack(id int) int {
	if packs == nil {
		return -1
	}
	pack := packs[id]

	if pack == nil {
		return -1
	}

	result, err := pack.table.Write()
	if err != nil {
		return -2
	}
	pack.cache = result
	return len(result)
}
