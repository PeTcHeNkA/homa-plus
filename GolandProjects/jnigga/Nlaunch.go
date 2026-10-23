package main

import (
	"C"
	"unsafe"
)

//export Java_plus_nlaunch_NLaunch_getSafe
func Java_plus_nlaunch_NLaunch_getSafe(env unsafe.Pointer, clazz Jclass, str jstring) JbyteArray {
	return newJbyteArr(env, getSafe(toString(env, str)))
}

//export Java_plus_nlaunch_NLaunch_loadSafe
func Java_plus_nlaunch_NLaunch_loadSafe(env unsafe.Pointer, clazz Jclass, str jstring) Jclass {
	return defSafe(env, toString(env, str))
}

func printA() {

}
