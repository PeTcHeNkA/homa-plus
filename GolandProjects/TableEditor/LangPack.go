package main

import (
	"TableEditor/Lang"
	"encoding/json"
	"google.golang.org/protobuf/proto"
)

type LangTable struct {
	lines []*Line
	sect  *bool
	dest  *uint32
	index map[string]*Line
}

type Line struct {
	id     int64
	index  int64
	str    string
	actual *string
}

type LangMapping struct {
	mappings map[string]string
}

func (inst *LangMapping) readFrom(data []byte) error {
	mappings := make(map[string]string)
	inst.mappings = mappings

	err := json.Unmarshal(data, &mappings)
	if err != nil {
		return err
	}
	return nil
}

func (inst *LangMapping) applyFor(table *LangTable) {
	index := table.index
	if index == nil {
		table.makeIndex()
		index = table.index
	}

	for key, val := range inst.mappings {
		line := index[key]
		if line != nil {
			line.actual = &val
		}
	}
}

func (inst *LangTable) readFrom(data []byte) error {
	index := Lang.Index{}
	rawTable := Lang.Translations{}

	indexLen := readInt(data)

	indexData := data[4:indexLen]
	err := proto.Unmarshal(indexData, &index)
	if err != nil {
		return err
	}
	//fmt.Printf("%s %s\n", *index.Dist, *index.Sect)

	inst.sect = index.Sect
	inst.dest = index.Dist

	tableData := data[4+indexLen:]

	err = proto.Unmarshal(tableData, &rawTable)
	if err != nil {
		return err
	}
	countLines := len(rawTable.Data)
	lines := make([]*Line, countLines)
	inst.lines = lines

	itemList := rawTable.Data
	empty := ""

	for i := 0; i < countLines; i++ {
		item := itemList[i]
		value := item.Name
		if value == nil {
			value = &empty
		}
		line := Line{
			*item.Id, int64(i), *value, nil,
		}
		lines[i] = &line
	}
	return nil
}

func (inst *LangTable) writeTo() ([]byte, error) {
	tabData := make([]byte, 0)

	var count uint32 = 0
	var curSize uint32 = 0

	index := Lang.Index{
		Index: make(map[uint32]*Lang.IndexItem),
		Sect:  inst.sect,
		Dist:  inst.dest,
	}

	cLines := inst.lines

	curIndex := uint32(0)

	for i := 0; i < len(cLines); {
		next10, err := writeNext10(i, cLines, &count, &curIndex)
		//fmt.Println("I", i, len(next10))

		if err != nil {
			return nil, err
		}
		tabData = append(tabData, next10...)

		fCursize := curSize
		fLen := uint32(len(next10))

		//fmt.Println(fCursize)

		item := Lang.IndexItem{
			BeginOff: &fCursize,
			Length:   &fLen,
		}
		index.Index[curIndex] = &item

		//fmt.Println(index.Index[curIndex])

		i += int(count)
		curSize += fLen
	}

	indexData, err := proto.Marshal(&index)
	if err != nil {
		return nil, err
	}

	allData := make([]byte, 4)
	writeInt(allData, len(indexData))

	allData = append(allData, indexData...)
	allData = append(allData, tabData...)

	return allData, nil
}

func (inst *LangTable) makeIndex() {
	index := make(map[string]*Line)

	for _, line := range inst.lines {
		index[line.str] = line
	}

	inst.index = index
}

func writeNext10(from int, lines []*Line, count *uint32, indexId *uint32) ([]byte, error) {
	data := make([]byte, 0)
	*count = 0

	*indexId = uint32(lines[from].id / 10)

	for i := from; i < from+10 && i < len(lines); i++ {
		line := lines[i]

		if (uint32(lines[i].id / 10)) != *indexId {
			break
		}

		actual := line.actual
		if actual == nil {
			actual = &line.str
		}

		item := Lang.Item{
			Id:   &line.id,
			Name: actual,
		}

		data0, err := proto.Marshal(&item)
		if err != nil {
			return nil, err
		}

		data = append(data, 10, 5)
		data = append(data, data0...)

		*count += 1
	}
	return data, nil
}

func writeInt(buf []byte, data int) {
	buf[3] = byte((data >> 24) & 0xff)
	buf[2] = byte((data >> 16) & 0xff)
	buf[1] = byte((data >> 8) & 0xff)
	buf[0] = byte(data & 0xff)
}

func readInt(buf []byte) int {
	return int(buf[3]&0xff)<<24 |
		int(buf[2]&0xff)<<16 |
		int(buf[1]&0xff)<<8 |
		int(buf[0]&0xff)
}
