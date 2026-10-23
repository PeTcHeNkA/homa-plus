package main

import (
	"encoding/hex"
	"time"

	"golang.org/x/sys/windows/registry"
)

const app_version = "1.0.0"
const pth = "SOFTWARE\\IrisALT\\Gf2RULaunch"
const magic = 2294918213893

var mxTime = time.Date(2027, 0, 0, 0, 0, 0, 0, time.UTC)

func CheckLicenseStartup() bool {
	if !CheckLicense() {
		panic("License invalid! Contact with IrisALT to get access permissions.")
		return false
	}
	return true
}

func CheckLicense() bool {
	k, e := registry.OpenKey(registry.LOCAL_MACHINE, pth, registry.ALL_ACCESS)

	if e != nil {
		k, _, e = registry.CreateKey(registry.LOCAL_MACHINE, pth, registry.ALL_ACCESS)

		if e != nil {
			return false
		}
	}

	var lk string
	var _ uint32

	lk, _, e = k.GetStringValue("license_key")
	ct := time.Now()

	if e != nil || lk == "" {
		if ct.After(mxTime) {
			return false
		}
		lk = applyMagic(mxTime.Format(time.RFC3339), false)
		e = k.SetStringValue("license_key", lk)
		return true
	} else {
		lk = applyMagic(lk, true)
	}

	var lcTime time.Time

	lcTime, e = time.Parse(time.RFC3339, lk)

	if e != nil {
		e = k.SetStringValue("license_key", "")
		return false
	}

	if ct.Before(lcTime) {
		return true
	}

	return false
}

func applyMagic(str string, dec bool) string {
	var chars []byte

	if dec {
		bts, err := hex.DecodeString(str)
		if err != nil {
			return ""
		}
		chars = bts
	} else {
		chars = []byte(str)
	}

	var magic2 = magic<<(magic&0x7) ^ magic ^ mxTime.Nanosecond()

	for i, chr := range chars {
		chars[i] = byte(int(chr) ^ (magic2 >> (i & 31)))
	}

	if !dec {
		str = hex.EncodeToString(chars)
	} else {
		str = string(chars)
	}
	return str
}
