package Pack

import (
	"TableEditor/Lang"
	"bytes"
	"encoding/json"
	"google.golang.org/protobuf/proto"
	"strconv"
)

type LangTable struct {
	lines []Line
	sect  *bool
	dest  *uint32
	index map[string][]*Line
	ididx map[uint32]*Line
}

type Line struct {
	id     uint32
	str    string
	actual *string
}

type LangMapping struct {
	mappings map[string]string
}

func (inst *LangMapping) ReadFrom(data []byte) error {
	var mappings map[string]string
	if inst.mappings == nil {
		mappings = make(map[string]string)
		inst.mappings = mappings
	} else {
		mappings = inst.mappings
	}

	err := json.Unmarshal(data, &mappings)
	if err != nil {
		return err
	}
	return nil
}

func (inst *LangMapping) Write() ([]byte, error) {
	buf := new(bytes.Buffer)
	enc := json.NewEncoder(buf)
	enc.SetIndent("", "    ")
	enc.SetEscapeHTML(false)

	err := enc.Encode(inst.mappings)
	if err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
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

// UseOf flags
// 1 - use ID
// 2 - use actual
// 4 - use other ID
func (inst *LangMapping) UseOf(keySrc *LangTable, valueSrc *LangTable, flags int) {
	useId := getFlag(flags, 0)
	raw := getFlag(flags, 1)
	otherId := getFlag(flags, 2)

	mappings := make(map[string]string)
	inst.mappings = mappings
	kLines := keySrc.lines

	if useId {
		if keySrc.index == nil {
			keySrc.MakeIndex()
		}
	}

	for cursor := range kLines {
		kLine := &kLines[cursor]
		var vLine *Line = nil

		if keySrc == valueSrc {
			vLine = kLine
		} else {
			vLine = valueSrc.findLine(uint32(cursor), kLine.id)
			if vLine == nil {
				vLine = kLine
			}
		}

		value := &vLine.str
		if raw {
			actual := vLine.actual
			if actual != nil {
				value = actual
			}
		}
		mKey := kLine.str

		if useId || otherId {
			lines := keySrc.index[mKey]
			hasMoreLines := lines != nil && len(lines) > 1

			if hasMoreLines == useId || hasMoreLines != otherId {
				mKey = writeId(kLine.id) + kLine.str
			}
		}
		mappings[mKey] = *value
	}
}

func writeId(id uint32) string {
	return "#ID:" + strconv.FormatUint(uint64(id), 10) + "|"
}

func readId(str string) uint32 {
	max1 := len(str)
	if max1 <= 4 {
		return 0
	}
	if str[0:4] == "#ID:" {
		id := uint32(0)
		for i := 4; i < max1; i++ {
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
	lines := make([]Line, countLines)
	inst.lines = lines

	itemList := rawTable.Data
	empty := ""

	for i := 0; i < countLines; i++ {
		item := itemList[i]
		value := item.Name
		if value == nil {
			value = &empty
		}
		line := &lines[i]
		line.id = *item.Id
		line.str = *value
	}
	return nil
}

type writeInfo struct {
	refs    [10]*[]byte
	cap     int
	sumSize uint32
	next    *writeInfo
}

func (inst *writeInfo) add(item *[]byte, itemLen uint32) {
	inst.refs[inst.cap] = item
	inst.sumSize += itemLen
	inst.cap += 1
}

func (inst *writeInfo) isFull() bool {
	return inst.cap == 10
}

func (inst *writeInfo) createNext() *writeInfo {
	inf := &writeInfo{}
	inst.next = inf
	return inf
}

func (inst *writeInfo) merge() []byte {
	totalSize := uint32(0)
	curInf := inst

	for curInf != nil {
		totalSize += curInf.sumSize
		curInf = curInf.next
	}

	data := make([]byte, totalSize)
	curInf = inst

	curShift := 0

	for curInf != nil {
		for cursor := 0; cursor < curInf.cap; cursor++ {
			curShift += copy(data[curShift:], *curInf.refs[cursor])
		}
		curInf = curInf.next
	}
	return data
}

func (inst *LangTable) Write() ([]byte, error) {
	inf := writeInfo{}
	curInf := &inf

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

		fCursize := curSize
		fLen := uint32(len(next10))

		curInf.add(&next10, fLen)

		if curInf.isFull() {
			curInf = curInf.createNext()
		}

		item := Lang.IndexItem{
			BeginOff: &fCursize,
			Length:   &fLen,
		}
		index.Index[curIndex] = &item

		i += int(count)
		curSize += fLen
	}

	indexData, err := proto.Marshal(&index)
	idxLen := len(indexData)
	if err != nil {
		return nil, err
	}

	allData := make([]byte, 4)
	writeInt(allData, idxLen)

	header := writeInfo{}
	header.add(&allData, 4)
	header.add(&indexData, uint32(idxLen))
	header.next = &inf

	return header.merge(), nil
}

func (inst *LangTable) MakeIndex() {
	index := make(map[string][]*Line)
	inst.index = index
	lines := inst.lines

	for cursor := range lines {
		line := &lines[cursor]

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

func (inst *LangTable) findLine(index uint32, id uint32) *Line {
	table := inst.lines
	if index < uint32(len(table)) {
		candidate := &table[index]
		if candidate.id == id {
			return candidate
		}
	}
	if inst.ididx == nil {
		inst.MakeIdIndex()
	}
	return inst.ididx[id]
}

func (inst *LangTable) MakeIdIndex() {
	idIndex := make(map[uint32]*Line)
	inst.ididx = idIndex
	lines := inst.lines

	for cursor := range lines {
		line := &lines[cursor]
		idIndex[line.id] = line
	}
}

func writeNext10(from int, lines []Line, count *uint32, indexId *uint32, temp *[10]*Lang.Item) ([]byte, error) {
	*count = 0
	*indexId = lines[from].id / 10
	lenLines := len(lines)

	for i := from; i < from+10 && i < lenLines; i++ {
		line := lines[i]

		if (lines[i].id / 10) != *indexId {
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

func getFlag(flags int, num int) bool {
	return (flags >> num & 1) == 1
}
