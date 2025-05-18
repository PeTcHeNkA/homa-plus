package Pack

import (
	"TableEditor/Lang"
	"encoding/json"
	"google.golang.org/protobuf/proto"
	"strconv"
)

type LangTable struct {
	lines []*Line
	sect  *bool
	dest  *uint32
	index map[string][]*Line
	ididx map[uint32]*Line
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

func (inst *LangMapping) ReadFrom(data []byte) error {
	mappings := make(map[string]string)
	inst.mappings = mappings

	err := json.Unmarshal(data, &mappings)
	if err != nil {
		return err
	}
	return nil
}

func (inst *LangMapping) Write() ([]byte, error) {
	result, err := json.Marshal(inst.mappings)
	if err != nil {
		return nil, err
	}
	return result, nil
}

func (inst *LangMapping) ApplyFor(table *LangTable) {
	index := table.index
	if index == nil {
		table.MakeIndex()
		index = table.index
	}
	idIndex := table.ididx

	for key, val := range inst.mappings {
		id := readId(key)
		if id != 0 {
			if idIndex == nil {
				table.MakeIdIndex()
				idIndex = table.ididx
			}
			line := idIndex[id]
			if line != nil {
				line.actual = &val
			}
			continue
		}

		line := index[key]
		if line != nil {
			len0 := len(line)
			for i := 0; i < len0; i++ {
				line[i].actual = &val
			}
		}
	}
}

func (inst *LangMapping) UseOf(table *LangTable, raw bool, useId bool) {
	mappings := make(map[string]string)
	inst.mappings = mappings
	cLines := table.lines

	for cursor := range cLines {
		line := cLines[cursor]
		value := &line.str
		if raw {
			actual := line.actual
			if actual != nil {
				value = actual
			}
		}
		mKey := line.str

		if useId {
			prev := mappings[mKey]
			if prev != "" {
				mKey = writeId(uint32(line.id)) + line.str
			}
		}
		mappings[line.str] = *value
	}
}

func writeId(id uint32) string {
	return "#ID:" + strconv.FormatUint(uint64(id), 10) + "|"
}

func readId(str string) uint32 {
	if str[0:4] == "#ID:" {
		max1 := len(str)
		id := uint32(0)
		for i := 3; i < max1; i++ {
			chr := str[i]
			if chr >= '0' && chr <= '9' {
				id *= 10
				id += uint32(chr - '0')
			} else if chr == '|' {
				return id
			} else {
				return 0
			}
		}
	}
	return 0
}

func (inst *LangTable) ReadFrom(data []byte) error {
	index := Lang.Index{}
	rawTable := Lang.Translations{}

	indexLen := readInt(data)

	indexData := data[4:20]
	err := proto.Unmarshal(indexData, &index)

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

func (inst *LangTable) Write() ([]byte, error) {
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
	tempData := [10]*Lang.Item{}

	for i := 0; i < len(cLines); {
		next10, err := writeNext10(i, cLines, &count, &curIndex, &tempData)

		if err != nil {
			return nil, err
		}
		tabData = append(tabData, next10...)

		fCursize := curSize
		fLen := uint32(len(next10))

		item := Lang.IndexItem{
			BeginOff: &fCursize,
			Length:   &fLen,
		}
		index.Index[curIndex] = &item

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

func (inst *LangTable) MakeIndex() {
	index := make(map[string][]*Line)
	inst.index = index

	for _, line := range inst.lines {
		prev := index[line.str]
		if prev == nil {
			arr := make([]*Line, 1)
			arr[0] = line
			index[line.str] = arr
		} else {
			prev = append(prev, line)
			index[line.str] = prev
		}
	}
}

func (inst LangTable) MakeIdIndex() {
	idIndex := make(map[uint32]*Line)
	inst.ididx = idIndex

	for _, line := range inst.lines {
		idIndex[uint32(line.id)] = line
	}
}

func writeNext10(from int, lines []*Line, count *uint32, indexId *uint32, temp *[10]*Lang.Item) ([]byte, error) {
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
		temp[i-from] = &item

		*count += 1
	}

	pack1 := Lang.Translations{
		Data: make([]*Lang.Item, *count),
	}

	for i := 0; uint32(i) < *count; i++ {
		pack1.Data[i] = temp[i]
	}
	return proto.Marshal(&pack1)
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
