package main

import (
	"os"
	"time"
)

func main() {
	args := os.Args

	if len(args) == 2 {
		if args[1] == "help" {
			println(" [help] - this text\n [version] - get app version\n [now] - get current time (RFC3339)\n [new] [time RFC3339] create new key")
			return
		}
		if args[1] == "version" {
			println(app_version)
			return
		}
		if args[1] == "now" {
			println(time.Now().Format(time.RFC3339))
			return
		}

	}
	if len(args) == 3 {
		if args[1] == "new" {
			tm, err := time.Parse(time.RFC3339, args[2])
			if err != nil {
				println("Invalid time: " + err.Error())
				return
			}
			println(applyMagic(tm.Format(time.RFC3339), false))
			return
		}
	}
	println("Invalid arguments, see [help]")
}
