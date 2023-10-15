package main

/*
#cgo CFLAGS: -IC:/java_liberica/include/ -IC:/java_liberica/include/win32/

#include "jni.h"
#include <stdlib.h>

jbyteArray NewByteArray(JNIEnv* env, jsize len) {
	return (*env)->NewByteArray(env, len);
}
void SetByteArrayRegion(JNIEnv* env, jbyteArray array, jsize start, jsize len, jbyte* buf) {
	(*env)->SetByteArrayRegion(env, array, start, len, buf);
}
const char* jx_GetStringUTFChars(JNIEnv *env, jstring str, jboolean *isCopy) {
    return (*env)->GetStringUTFChars(env, str, isCopy);
}

//jobject GetSystemClassLoader(JNIEnv *env) {
//    jclass classloaderClass = (*env)->FindClass(env, "java/lang/ClassLoader");
//    jmethodID getsysloaderMethod = (*env)->GetStaticMethodID(env, classloaderClass, "getSystemClassLoader","()Ljava/lang/ClassLoader;");
//
//    jobject loader = (*env)->CallStaticObjectMethod(env, classloaderClass, getsysloaderMethod);
//	return loader;
//}

jclass DefClass(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize bufLen){
	return (*env)->DefineClass(env, name, loader, buf, bufLen);
}

jobject GetSelfCL(JNIEnv *env){
    jclass clazz = (*env)->FindClass(env, "plus/nlaunch/NLaunch");
    jfieldID fieldID = (*env)->GetStaticFieldID(env, clazz, "inst", "Ljava/lang/ClassLoader;");
    return (*env)->GetStaticObjectField(env, clazz, fieldID);
}

jobject GetDomain(JNIEnv *env){
    jclass clazz = (*env)->FindClass(env, "plus/nlaunch/NLaunch");
    jfieldID fieldID = (*env)->GetStaticFieldID(env, clazz, "inst", "Ljava/lang/ClassLoader;");
    return (*env)->GetStaticObjectField(env, clazz, fieldID);
}

//jclass FindJClass(JNIEnv *env, const char *name){
//	return (*env)->FindClass(env, name);
//}
//
//jmethodID GetStaticMtdID(JNIEnv *env, jclass clazz, const char *name, const char *sig){
//	return (*env)->GetStaticMethodID(env, clazz, name, sig);
//}

#ifndef _Included_plus_nlaunch_NLaunch
#define _Included_plus_nlaunch_NLaunch
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jbyteArray JNICALL Java_plus_nlaunch_NLaunch_getSafe
  (JNIEnv *, jclass, jstring);

JNIEXPORT jclass JNICALL Java_plus_nlaunch_NLaunch_loadSafe
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif
#endif

*/
import "C"
import (
	"log"
	"unsafe"
)

type (
	JbyteArray uintptr
	Jclass     uintptr
	jstring    uintptr
	jobject    uintptr
	//jbyte      uintptr
	//jmethodID  uintptr
)

func newByteArray(env unsafe.Pointer, len int) JbyteArray {
	return JbyteArray(unsafe.Pointer(C.NewByteArray((*C.JNIEnv)(env), C.jsize(len))))
}

func setByteArrayRegion(env unsafe.Pointer, array JbyteArray, start int, len int, buf unsafe.Pointer) {
	C.SetByteArrayRegion((*C.JNIEnv)(env), C.jbyteArray(array), C.jsize(start), C.jsize(len), (*C.jbyte)(buf))
}

func toString(env unsafe.Pointer, str jstring) string {
	s := C.jx_GetStringUTFChars((*C.JNIEnv)(env), (C.jstring)(str), (*C.jboolean)(nil))
	return C.GoString(s)
}

func getJClassLoader(env unsafe.Pointer) jobject {
	return jobject(unsafe.Pointer(C.GetSelfCL((*C.JNIEnv)(env))))
}

func defJClass(env unsafe.Pointer, name string, loader jobject, bytesV []byte) Jclass {
	cname := C.CString(name)
	size := len(bytesV)
	var buf unsafe.Pointer

	buf = malloc(uintptr(size))
	copy(createMinSizeArr(size, buf), bytesV)

	res := C.DefClass((*C.JNIEnv)(env), cname, C.jobject(loader), (*C.jbyte)(buf), C.jsize(size))

	defer free(buf)
	defer free(unsafe.Pointer(cname))
	return Jclass(res)
}

func newJbyteArr(env unsafe.Pointer, bytesV []byte) JbyteArray {
	if bytesV == nil {
		return emptyJArray
	}
	size := len(bytesV)
	arr := newByteArray(env, size)
	var ptr unsafe.Pointer
	if size > 0 {
		ptr = malloc(uintptr(size))
		defer free(ptr)
		copy(createMinSizeArr(size, ptr), bytesV)
		setByteArrayRegion(env, arr, 0, size, ptr)
	}
	return arr
}

func createMinSizeArr(size int, ptr unsafe.Pointer) []byte {
	if size < 1024*1024 {
		if size < 1024*256 {
			return (*(*[1024 * 1024]byte)(ptr))[:size]
		} else {
			return (*(*[1024 * 256]byte)(ptr))[:size]
		}
	} else {
		if size < 1024*1024*16 {
			return (*(*[1024 * 1024 * 16]byte)(ptr))[:size]
		} else {
			if size < 1024*1024*128 {
				return (*(*[1024 * 1024 * 128]byte)(ptr))[:size]
			} else {
				return (*(*[1024 * 1024 * 16]byte)(ptr))[:size]
			}
		}
	}
}

func malloc(size uintptr) unsafe.Pointer {
	p := C.malloc(C.size_t(size))
	if p == nil {
		log.Panicf("C malloc failed (size = %d)", size)
	}
	return p
}

func free(ptr unsafe.Pointer) {
	C.free(ptr)
}
