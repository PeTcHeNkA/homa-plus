package main

/*
#cgo CFLAGS: -IC:/java_liberica/include/ -IC:/java_liberica/include/win32/

#include "jni.h"
#include <stdlib.h>
#include <string.h>

jbyteArray NewByteArray(JNIEnv* env, jsize len) {
	return (*env)->NewByteArray(env, len);
}
void SetByteArrayRegion(JNIEnv* env, jbyteArray array, jsize start, jsize len, jbyte* buf) {
	(*env)->SetByteArrayRegion(env, array, start, len, buf);
}
const char* jx_GetStringUTFChars(JNIEnv *env, jstring str, jboolean *isCopy) {
    return (*env)->GetStringUTFChars(env, str, isCopy);
}

jclass DefClass(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize bufLen){
	return (*env)->DefineClass(env, name, loader, buf, bufLen);
}

jobject GetField(JNIEnv *env, char *clazz0, char *var, char *sign){
    jclass clazz = (*env)->FindClass(env, clazz0);
    jfieldID fieldID = (*env)->GetStaticFieldID(env, clazz, var, sign);
    return (*env)->GetStaticObjectField(env, clazz, fieldID);
}

jmethodID GetStaticMtdID(JNIEnv *env, jclass clazz, const char *name, const char *sig){
	return (*env)->GetStaticMethodID(env, clazz, name, sig);
}

jobject GetSelfCL(JNIEnv *env){
	return GetField(env, "plus/nlaunch/NLaunch", "inst", "Ljava/lang/ClassLoader;");
}

jobject CallStatic(JNIEnv *env, jclass clazz, jmethodID mtdid){
	return (*env)->CallStaticObjectMethod(env, clazz, mtdid);
}

int CanWork(JNIEnv *env){
	jclass fc = (*env)->FindClass(env, "sun/management/ManagementFactoryHelper");//
	jfieldID fieldID = (*env)->GetStaticFieldID(env, fc, "jvm", "Lsun/management/VMManagement;");//
	jobject vmInst = (*env)->GetStaticObjectField(env, fc, fieldID);//
	jclass clazzVMM = (*env)->FindClass(env, "sun/management/VMManagementImpl");//
	jmethodID mtdid = (*env)->GetMethodID(env, clazzVMM, "getVmArguments0", "()[Ljava/lang/String;");

	jobjectArray args = (jobjectArray)CallStatic(env, clazzVMM, mtdid);
	jsize argsLen = (*env)->GetArrayLength(env, args);
	int l2 = argsLen;

	for(int i = 0; i < argsLen; i++ ){
		jstring str = (*env)->GetObjectArrayElement(env, args, i);
		const char* str0 = jx_GetStringUTFChars(env, str, 0);
		if(strstr(str0, "jdwp")){
			return 1;
		}
	}
	return 0;
}

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

func canWork(env unsafe.Pointer) bool {
	return C.CanWork((*C.JNIEnv)(env)) == 0
}

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
