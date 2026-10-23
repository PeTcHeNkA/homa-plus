package main

import (
	"context"
	"github.com/wailsapp/wails/v2/pkg/runtime"
	. "main/tutil"
	"os/exec"
	"strings"
)

type App struct {
	ctx context.Context
}

func NewApp() *App {
	return &App{}
}

func (a *App) startup(ctx context.Context) {
	a.ctx = ctx
	runtime.WindowCenter(ctx)
}

func (a *App) Minimize() {
	runtime.WindowMinimise(a.ctx)
}

func (a *App) Quit() {
	runtime.Quit(a.ctx)
}

func (a *App) GetFiles(dir string) string {
	MakeDirs(dir)
	return GetFiles(dir, "/screenshots")
}

func (a *App) GetLaunchPatch() string {
	return MCINF.LaunchPatch
}

func (a *App) GetCache(sub string) string {
	return GetCache(sub)
}

func (a *App) SetCache(sub string, data string) {
	SaveCache(sub, data)
}

func (a *App) ExplorerSelect(obj string) {
	_ = exec.Command("explorer", "/select,"+strings.ReplaceAll(obj, "/", "\\")).Run()
}

var checked = false

func (a App) ClickPlay(fnName string, params string, name string, uuid string, token string) {
	var cb func(cb string)
	if fnName == "" {
		cb = func(str string) {}
	} else {
		cb = func(str string) {
			println(str)
			runtime.WindowExecJS(a.ctx, fnName+"('"+str+"')")
		}
	}
	if !checked {
		err := CheckUpdates(cb)
		if err != nil {
			println(err.Error())
			return
		} else {
			checked = true
		}
	}
	//println("NAME: " + name + ", UUID: " + uuid + ", TOKEN: " + token)
	Launch(MCINF.LaunchPatch, params, &name, &uuid, &token)
}
