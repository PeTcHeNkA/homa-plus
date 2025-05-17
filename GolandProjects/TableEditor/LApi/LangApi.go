package main

import "TableEditor/Pack"

var packs map[string]*Pack.LangTable

func main() {}

//export loadPack
func loadPack(id string, data []byte) int {
	if packs == nil {
		packs = make(map[string]*Pack.LangTable)
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
		return 3
	}
	return 0
}

//export freePack
func freePack(id string) int {
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

//export applyMappings
func applyMappings(id string, data []byte) int {
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

//export exportMappings
func exportMappings(id string, actual bool) []byte {
	if packs == nil {
		return errCode(1)
	}
	pack := packs[id]
	if pack == nil {
		return errCode(1)
	}

	mappngs := Pack.LangMapping{}
	mappngs.UseOf(pack, actual)

	result, err := mappngs.Write()
	if err != nil {
		return errCode(2)
	}
	return result
}

//export writePack
func writePack(id string) []byte {
	if packs == nil {
		return errCode(1)
	}
	pack := packs[id]

	if pack == nil {
		return errCode(1)
	}

	result, err := pack.Write()
	if err != nil {
		return errCode(2)
	}
	return result
}

func errCode(code byte) []byte {
	data := make([]byte, 1)
	data[0] = code
	return data
}
