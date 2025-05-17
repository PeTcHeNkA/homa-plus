package main

var packs map[string]*LangTable

//export loadPack
func loadPack(id string, data []byte) int {
	if packs == nil {
		packs = make(map[string]*LangTable)
	}
	var pack *LangTable

	if packs[id] == nil {
		pack = &LangTable{}
		packs[id] = pack
	} else {
		pack = packs[id]
	}
	err := pack.readFrom(data)
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
	packs[id] = nil
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
	mappngs := LangMapping{}

	err := mappngs.readFrom(data)

	if err != nil {
		return 2
	}
	mappngs.applyFor(pack)
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

	mappngs := LangMapping{}
	mappngs.useOf(pack, actual)

	result, err := mappngs.write()
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

	result, err := pack.write()
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
