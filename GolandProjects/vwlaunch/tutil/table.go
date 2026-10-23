package tutil

import (
	"errors"
	"net/http"
	"strings"
	"sync"
)

type Loading struct {
	patch string
	url   string
	sign  string
}

type Checker struct {
	sigUrl    string
	tableUrl  string
	tableFile string
	curTbSig  string
	client    *http.Client
}

type Table struct {
	lines []string
	sign  string
}

var encoder = TCrypter{}.ReadKey(keyCode)

//func (c Checker) GetTable() (error, *Table) {
//	sig, err := ReadAllBytes(c.sigUrl, c.client)
//	if err != nil {
//		return nulError, nil
//	}
//	return nil, nil
//}

func (c Checker) LoadTable() (Table, error) {
	bytes, err := ReadAllBytes(c.tableUrl, c.client)
	if err != nil {
		return Table{}, err
	}
	_ = CreateFile(c.tableFile, bytes)
	return newTable(string(bytes)), nil
}

func (c Checker) ReadLocalTable() (Table, error) {
	bytes := ReadFile(c.tableFile)
	if bytes == nil || len(bytes) < 3 {
		return Table{}, nulError
	}
	return newTable(string(bytes)), nil
}

func newTable(data string) Table {
	lines := strings.Split(data, "\n")
	sign := lines[len(lines)-1]
	lines = lines[:len(lines)-2]
	return Table{lines: lines, sign: sign}
}

func (t Table) IsActualSig(c *Checker) bool {
	return t.sign == c.curTbSig
}

func (t Table) CheckSig() bool {
	return encoder.CheckE(t.sign, []byte(strings.Join(t.lines, "\n")))
}

func (c Checker) LoadActualSig() error {
	res, err := ReadAllBytes(c.sigUrl, c.client)
	if err == nil {
		return err
	}
	c.curTbSig = string(res)
	return nil
}

func (t Table) GetUpdates() map[string]Loading {
	table := map[string]Loading{}
	var mutex sync.Mutex
	var group sync.WaitGroup

	for _, line := range t.lines {
		group.Add(1)
		go func(line0 string) {
			items := strings.Split(line0, ";")
			ld := Loading{items[0], items[1], items[2]}
			if !ld.isValid() {
				mutex.Lock()
				table[ld.patch] = ld
				mutex.Unlock()
			}
			defer group.Done()
		}(line)
	}

	group.Wait()
	return table
}

func (l Loading) isValid() bool {
	data := ReadFile(MCINF.LaunchPatch + "/" + l.patch)
	if data == nil {
		println("File [" + MCINF.LaunchPatch + "/" + l.patch + "] not exist!")
		return false
	}
	return encoder.CheckE(l.sign, data)
}

func (l Loading) load(client *http.Client) error {
	res, err := ReadAllSmart(l.url, client)
	if err != nil {
		return err
	}
	return CreateFile(MCINF.LaunchPatch+"/"+l.patch, res)
}

var (
	netError    = errors.New("net Error")
	nulError    = errors.New("null Error")
	updateError = errors.New("update Error")
)

var checker *Checker

func getOrNewChecker() *Checker {
	if checker == nil {
		host := "https://lochost.ru/launch/"
		ch := Checker{
			sigUrl:    host + "version.sign",
			tableUrl:  host + "version.table",
			tableFile: MCINF.LaunchPatch + "/version.table",
			client:    GetHttpClient(),
		}
		return &ch
	}
	return checker
}

const (
	stateInit           = "init"
	stateCheckSig       = "lsig"
	stateEmpty          = "empty"
	stateLoadTable      = "ld_tb"
	stateLoadingUpdates = "ld_upd"
	stateCheckUpdates   = "ch_upd"
)

func (c Checker) LoadUpdates(updates map[string]Loading, callback func(str string)) error {
	var group sync.WaitGroup
	var crit error
	ch := make(chan Loading, 10)

	for i := 0; i < 10; i++ {
		group.Add(1)
		go func() {
			for {
				loading, opened := <-ch
				if crit != nil || !opened {
					break
				}
				var attemts = 5
				callback(stateLoadingUpdates + "." + loading.patch)
				err := loading.load(c.client)
				for err != nil && errors.Is(err, netError) {
					if attemts > 0 {
						attemts -= 1
						callback(stateLoadingUpdates + "." + loading.patch)
						err = loading.load(c.client)
					} else {
						crit = err
						break
					}
				}
			}
			defer group.Done()
		}()
	}
	for _, loading0 := range updates {
		ch <- loading0
	}
	close(ch)
	group.Wait()

	return crit
}

func CheckUpdates(callback func(str string)) error {
	defer func() {
		callback(stateEmpty)
	}()

	callback(stateInit)
	ch := getOrNewChecker()

	callback(stateCheckSig)
	err := ch.LoadActualSig()

	if err != nil {
		return err
	}

	var table Table
	table, err = ch.ReadLocalTable()

	if err != nil || !table.IsActualSig(ch) {
		callback(stateLoadTable)
		table, err = ch.LoadTable()

		if err != nil {
			return err
		}
	}

	callback(stateCheckUpdates)
	updates := table.GetUpdates()
	var attemts = 3

	for len(updates) > 0 && attemts > 0 {
		err = ch.LoadUpdates(updates, callback)
		if err != nil {
			return err
		}
		callback(stateCheckUpdates)
		updates = table.GetUpdates()
		attemts -= 1
	}
	if len(updates) > 0 {
		return updateError
	}
	return nil
}
