package main

import (
	"bytes"
	"fmt"
)

func main() {
	item := TItem{
		Id:   10,
		Name: "Test",
		Cargo: Ti2{
			ValueDD: "FFFFFFF",
		},
	}

	fmt.Println(item)

	buf := &bytes.Buffer{}
	WriteBytes(item, buf)

	item2 := (interface{})(TItem{})

	println(buf.String())

	ReadBytes(&item2, buf)

	fmt.Println(item2)

	//os.WriteFile("test.bin", buf.Bytes(), os.ModePerm)
}

type TItem struct {
	Id    int32   `nbt:"id"`
	Name  NString `nbt:"name"`
	Cargo Ti2     `nbt:"cargo"`
}

type Ti2 struct {
	ValueDD NString `nbt:"vld"`
}
