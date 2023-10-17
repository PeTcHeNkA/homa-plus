package main

import "C"
import (
	"archive/zip"
	"bytes"
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
	//sysCl       jobject
	needInit    = true
	emptyJArray JbyteArray
	mutex       sync.Mutex
)

func defSafe(env unsafe.Pointer, name string) Jclass {
	if needInit {
		if !canWork(env) {
			println("NLAUNCH CRITICAL ERROR: MEMORY INTEGRITY VIOLATION")
			os.Exit(1)
		} else {
			needInit = false
		}
		emptyJArray = newByteArray(env, 0)
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
			err = decode(file, table, tcr, &group, &mutex)
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

//    ks = 512
//    maxS = len(bs0)
//    ms = min(ks, maxS)
//
//    fchunk = decode(bs0[0:ms])
//    buffer = fchunk
//
//    hs = hashFunc(fchunk)
//    fchunk = hs + fchunk
//    fdl = len(fchunk)-512
//    fchunk = fchunk + fchunk[0:fdl]
//
//    e2 = list(bs0[ms:maxS])
//
//    for i in range(len(e2)):
//        m2 = fchunk[i & 511]
//        e2[i] = e2[i] ^ m2
//
//
//    writeBytes(to, buffer+bytes(e2))

func decodeLazy(bytesSrc []byte, tcr *tutil.TCrypter) ([]byte, error) {
	const ks = 512
	key := bytesSrc[0:ks]
	data := bytesSrc[ks:]
	for i, b := range data {
		data[i] = b ^ key[i&511]
	}
	return append(tcr.Decrypt(key), data...), nil
}
