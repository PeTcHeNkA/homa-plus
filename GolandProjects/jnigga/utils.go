package main

import "C"
import (
	"archive/zip"
	"bytes"
	"errors"
	"io"
	"main/tutil"
	"os"
	"strings"
	"sync"
	"unsafe"
)

var table = map[string][]byte{}
var tcrypter *tutil.TCrypter

func main() {
}

func getSafe(str string) []byte {
	cmd := str[0]
	if cmd == uint8('!') {
		if tcrypter == nil {
			tcrypter0 := tutil.TCrypter{}.ReadKey("-----BEGIN RSA PRIVATE KEY-----\n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\n-----END RSA PRIVATE KEY-----\n")

			tcrypter = &tcrypter0
		}

		err := packDecode(strings.Split(str[1:], ";"), table, tcrypter)
		if err == nil {
			return nil
		} else {
			root, _ := os.Executable()
			return []byte("[" + root + "] error unpack: " + err.Error())
		}
	} else {
		if strings.HasSuffix(str, ".class") {
			return nil
		}
		return table[str]
	}
}

var (
	needInit = true
	//emptyJArray JbyteArray
	mutex sync.Mutex
)

func defSafe(env unsafe.Pointer, name string) Jclass {
	if needInit {
		needInit = true
		if !canWork(env) {
			println("NLAUNCH CRITICAL ERROR: MEMORY INTEGRITY VIOLATION")
			os.Exit(1)
			return Jclass(C.NULL)
		} else {
			needInit = false
		}
	}
	clName := strings.ReplaceAll(name, ".", "/")
	classBytes := table[clName+".class"]
	if classBytes == nil {
		return Jclass(C.NULL)
	}
	return defJClass(env, clName, getJClassLoader(env), classBytes)
}

func packDecode(files []string, table map[string][]byte, tcr *tutil.TCrypter) error {
	var group sync.WaitGroup
	var err error

	for _, file0 := range files {
		if err != nil {
			return err
		}
		group.Add(1)
		go func(file string) {
			defer group.Done()
			err = decode(strings.ReplaceAll(file, "\\", "/"), table, tcr, &group, &mutex)
			if err != nil {
				return
			}
		}(file0)
	}
	group.Wait()
	return err
}

const ks = 512

func decode(filename string, table map[string][]byte, tcr *tutil.TCrypter, group *sync.WaitGroup, mutex *sync.Mutex) error {
	var data []byte
	var err error

	var bytesSrc []byte
	bytesSrc, err = os.ReadFile(filename)
	if err != nil {
		return err
	}

	if len(bytesSrc) > 512*4 {
		data, err = decodeLazy(bytesSrc, tcr)
	} else {
		data, err = decodeDf(bytesSrc, tcr)
	}

	if err != nil {
		return err
	}
	reader := bytes.NewReader(data)
	zipReader, err := zip.NewReader(reader, int64(len(data)))
	if err != nil {
		return err
	}
	for _, file0 := range zipReader.File {
		if err != nil {
			return err
		}
		group.Add(1)
		go func(file *zip.File) {
			group.Done()
			var reader0 io.ReadCloser
			reader0, err = file.Open()
			if err != nil {
				return
			}
			var fileData []byte
			fileData, err = io.ReadAll(reader0)
			if err != nil {
				return
			}
			mutex.Lock()
			table[file.Name] = fileData
			mutex.Unlock()
		}(file0)
	}
	return err
}

func decodeDf(bytesSrc []byte, tcr *tutil.TCrypter) ([]byte, error) {
	maxS := len(bytesSrc)
	buffer := []byte{}
	cursor := 0
	for {
		shift := cursor * ks
		ms := min(shift+ks, maxS)
		chunk := bytesSrc[shift:ms]
		buffer = append(buffer, tcr.Decrypt(chunk)...)
		cursor += 1
		if ms >= maxS {
			break
		}
	}
	return buffer, nil
}

func decodeLazy(bytesSrc []byte, tcr *tutil.TCrypter) ([]byte, error) {
	const ks = 512
	fchunk := tcr.Decrypt(bytesSrc[0:ks])
	buffer := fchunk
	hs := tutil.HashFunc(fchunk)
	fchunk = append(hs, fchunk...)
	fdl := len(fchunk) - 512
	if fdl < 0 {
		return nil, errors.New("???")
	}
	fchunk = append(fchunk, fchunk[0:fdl]...)

	data := bytesSrc[ks:]

	for i, b := range data {
		data[i] = b ^ fchunk[i&511]
	}
	return append(buffer, data...), nil
}
