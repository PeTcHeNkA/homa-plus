package tutil

import (
	"bytes"
	"fmt"
)

var Pcoder = newCoder()

type SigCoder struct {
	table  []string
	length int
}

func newCoder() *SigCoder {
	chars := "qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM"
	length := len(chars)
	table := make([]string, 256)

	cNum := func(i int) string {
		if i == 0 {
			return "__"
		}
		s := ""
		for i > 0 {
			index := i % length
			if index == 0 {
				index = length
			}
			s = string(chars[index-1]) + s
			i = i / length
		}
		for len(s) < 2 {
			s = "_" + s
		}
		return s
	}

	for i := 0; i < 256; i++ {
		table[i] = cNum(i)
	}

	return &SigCoder{
		table:  table,
		length: len(table),
	}
}

func (c *SigCoder) Code(bytes0 []byte) string {
	var v bytes.Buffer
	for _, b := range bytes0 {
		v.WriteString(c.table[b])
	}
	return v.String()
}

func (c *SigCoder) Encode(data string) []byte {
	val := make([]byte, 0)
	for i := 0; i < len(data); i += 2 {
		if i+1 < len(data) {
			index := -1
			for j := 0; j < c.length; j++ {
				if c.table[j] == data[i:i+2] {
					index = j
					break
				}
			}
			if index != -1 {
				val = append(val, byte(index))
			} else {
				fmt.Println(i)
				fmt.Println(data[i : i+2])
			}
		}
	}
	return val
}

func (enc TCrypter) CheckE(sign string, msg []byte) bool {
	return enc.Check(Pcoder.Encode(sign), msg)
}
