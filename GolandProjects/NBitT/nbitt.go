package main

import (
	"bytes"
	"errors"
	"fmt"
	"math"
	"reflect"
	"strconv"
)

//func main() {
//	var n NBitValue
//	n = NBool(false)
//	n = NInt8(0)
//	n = NInt16(0)
//	n = NInt(0)
//	n = NString("aaaa")
//	n = NInt64(0)
//	n = NFloat64(0.8)
//	n = NMap{}
//
//	n = NInt8Arr{0, 0}
//
//	n = NInt32Arr{0, 0}
//	n = NInt64Arr{0, 0}
//
//	println(n.GetInt())
//
//	n = NList{}
//	n = NInt8(10)
//
//	println(NFloat32(10.5).GetString())
//
//	println(n.GetDouble())
//
//	n = nEND(true)
//}

//
//// todo test
//type TItem struct {
//	Id   int    `nbt:"id"`
//	Name string `nbt:"name"`
//}

//types

type NBool bool
type NInt8 byte
type NInt int
type NInt16 int16
type NInt64 int64
type NFloat32 float32
type NFloat64 float64
type NString string
type NMap map[string]*NBitValue
type NInt8Arr []byte
type NInt32Arr []int32
type NInt64Arr []int64
type nEND bool //empty
//todo list

const N_TRUE = NBool(true)
const N_FALSE = NBool(false)

var N_TYPE = reflect.TypeOf((*NBitValue)(nil)).Elem()

var n_0 *NBitValue
var n_1 *NBitValue

const n_END = nEND(false)

var n_END_RF *NBitValue

var nB_TRUE *string
var nB_FALSE *string

var nHeaders [16]*[]byte

func init() {
	//todo fix
	var t = "true"
	var f = "false"
	nB_TRUE = &t
	nB_FALSE = &f

	var n NBitValue = NInt(0)
	n_0 = &n
	n = NInt(1)
	n_1 = &n
	n = n_END
	n_END_RF = &n

	for i := 0; i < len(nHeaders); i++ {
		itm := []byte{byte(i)}
		nHeaders[i] = &itm
	}
}

type NBitValue interface {
	GetNID() uint8
	GetString() *string
	GetInt() int
	GetLong() int64
	GetFloat() float32
	GetDouble() float64
	GetByte() byte
	GetBool() bool

	GetVal(key string) *NBitValue
	GetByIdx(index int) *NBitValue
	Len() int

	GetByteArr() *NInt8Arr
	GetIntArr() *NInt32Arr
	GetLongArr() *NInt64Arr
	GetList() *NList

	WriteTo(b *bytes.Buffer)
}

type ReadFunc interface {
	ReadObj(b *bytes.Buffer, s *int) (interface{}, error)
}

type NList struct {
	RawArray []*NBitValue
	Size     uint16
}

// List utils

func (l NList) Add(v *NBitValue) {
	arr := l.RawArray
	if len(arr) <= int(l.Size) {
		arr = append(arr, make([]*NBitValue, 10)...)
		l.RawArray = arr
	}
	arr[l.Size] = v
	l.Size += 1
}

func (l NList) EnsureSize(size int) {
	arr := l.RawArray
	cLen := len(arr)
	aLen := int(l.Size)

	if aLen < size {
		arr = append(arr, make([]*NBitValue, size-cLen)...)
		l.RawArray = arr
	}
}

// List methods

func (l NList) GetNID() uint8 {
	return 9
}

func (l NList) GetString() *string {
	return nil
}

func (l NList) Len() int {
	return int(l.Size)
}

func (l NList) GetInt() int {
	return 0
}

func (l NList) GetLong() int64 {
	return 0
}

func (l NList) GetFloat() float32 {
	return 0
}

func (l NList) GetDouble() float64 {
	return 0
}

func (l NList) GetByte() byte {
	return 0
}

func (l NList) GetBool() bool {
	return l.Size != 0
}

func (l NList) GetVal(string) *NBitValue {
	return nil
}

func (l NList) GetByIdx(idx int) *NBitValue {
	ln0 := int(l.Size)
	if idx < 0 || idx >= ln0 {
		return nil
	}
	return l.RawArray[idx]
}

func (l NList) GetByteArr() *NInt8Arr {
	return nil
}

func (l NList) GetIntArr() *NInt32Arr {
	return nil
}

func (l NList) GetLongArr() *NInt64Arr {
	return nil
}

func (l NList) GetList() *NList {
	return &l
}

// Bool types

func (i NBool) GetNID() uint8 {
	return 1
}

func (i NBool) GetString() *string {
	if i {
		return nB_TRUE
	}
	return nB_FALSE
}

func (i NBool) Len() int {
	return -1
}

func (i NBool) GetInt() int {
	if i {
		return 1
	}
	return 0
}

func (i NBool) GetLong() int64 {
	if i {
		return 1
	}
	return 0
}

func (i NBool) GetFloat() float32 {
	if i {
		return 1
	}
	return 0
}

func (i NBool) GetDouble() float64 {
	if i {
		return 1
	}
	return 0
}

func (i NBool) GetByte() byte {
	if i {
		return 1
	}
	return 0
}

func (i NBool) GetBool() bool {
	return bool(i)
}

func (i NBool) GetVal(string) *NBitValue {
	return nil
}

func (i NBool) GetByIdx(idx int) *NBitValue {
	if idx == 0 {
		if i {
			return n_1
		} else {
			return n_0
		}
	}
	return nil
}

func (i NBool) GetByteArr() *NInt8Arr {
	return nil
}

func (i NBool) GetIntArr() *NInt32Arr {
	return nil
}

func (i NBool) GetLongArr() *NInt64Arr {
	return nil
}

func (i NBool) GetList() *NList {
	return nil
}

// Byte methods

func (i NInt8) GetNID() uint8 {
	return 1
}

func (i NInt8) GetString() *string {
	var r = strconv.Itoa(int(i))
	return &r
}

func (i NInt8) GetInt() int {
	return int(i)
}

func (i NInt8) GetLong() int64 {
	return int64(i)
}

func (i NInt8) GetFloat() float32 {
	return float32(i)
}

func (i NInt8) GetDouble() float64 {
	return float64(i)
}

func (i NInt8) GetByte() byte {
	return byte(i)
}

func (i NInt8) GetBool() bool {
	return i != 0
}

func (i NInt8) GetVal(string) *NBitValue {
	return nil
}

func (i NInt8) GetByIdx(idx int) *NBitValue {
	return nBinRef(int64(i), idx)
}

func (i NInt8) Len() int {
	return -1
}

func (i NInt8) GetByteArr() *NInt8Arr {
	return nil
}

func (i NInt8) GetIntArr() *NInt32Arr {
	return nil
}

func (i NInt8) GetLongArr() *NInt64Arr {
	return nil
}

func (i NInt8) GetList() *NList {
	return nil
}

// Short methods

func (i NInt16) GetNID() uint8 {
	return 2
}

func (i NInt16) GetString() *string {
	var r = strconv.Itoa(int(i))
	return &r
}

func (i NInt16) GetInt() int {
	return int(i)
}

func (i NInt16) GetLong() int64 {
	return int64(i)
}

func (i NInt16) GetFloat() float32 {
	return float32(i)
}

func (i NInt16) GetDouble() float64 {
	return float64(i)
}

func (i NInt16) GetByte() byte {
	return byte(i)
}

func (i NInt16) GetBool() bool {
	return i != 0
}

func (i NInt16) GetVal(string) *NBitValue {
	return nil
}

func (i NInt16) GetByIdx(idx int) *NBitValue {
	return nBinRef(int64(i), idx)
}

func (i NInt16) Len() int {
	return -1
}

func (i NInt16) GetByteArr() *NInt8Arr {
	return nil
}

func (i NInt16) GetIntArr() *NInt32Arr {
	return nil
}

func (i NInt16) GetLongArr() *NInt64Arr {
	return nil
}

func (i NInt16) GetList() *NList {
	return nil
}

// Int methods

func (i NInt) GetNID() uint8 {
	return 3
}

func (i NInt) GetString() *string {
	var r = strconv.Itoa(int(i))
	return &r
}

func (i NInt) GetInt() int {
	return int(i)
}

func (i NInt) GetLong() int64 {
	return int64(i)
}

func (i NInt) GetFloat() float32 {
	return float32(i)
}

func (i NInt) GetDouble() float64 {
	return float64(i)
}

func (i NInt) GetByte() byte {
	return byte(i)
}

func (i NInt) GetBool() bool {
	return i != 0
}

func (i NInt) GetVal(string) *NBitValue {
	return nil
}

func (i NInt) GetByIdx(idx int) *NBitValue {
	return nBinRef(int64(i), idx)
}

func (i NInt) Len() int {
	return -1
}

func (i NInt) GetByteArr() *NInt8Arr {
	return nil
}

func (i NInt) GetIntArr() *NInt32Arr {
	return nil
}

func (i NInt) GetLongArr() *NInt64Arr {
	return nil
}

func (i NInt) GetList() *NList {
	return nil
}

// Long methods

func (i NInt64) GetNID() uint8 {
	return 4
}

func (i NInt64) GetString() *string {
	var r = strconv.FormatInt(int64(i), 10)
	return &r
}

func (i NInt64) GetInt() int {
	return int(i)
}

func (i NInt64) GetLong() int64 {
	return int64(i)
}

func (i NInt64) GetFloat() float32 {
	return float32(i)
}

func (i NInt64) GetDouble() float64 {
	return float64(i)
}

func (i NInt64) GetByte() byte {
	return byte(i)
}

func (i NInt64) GetBool() bool {
	return i != 0
}

func (i NInt64) GetVal(string) *NBitValue {
	return nil
}

func (i NInt64) GetByIdx(idx int) *NBitValue {
	return nBinRef(int64(i), idx)
}

func (i NInt64) Len() int {
	return -1
}

func (i NInt64) GetByteArr() *NInt8Arr {
	return nil
}

func (i NInt64) GetIntArr() *NInt32Arr {
	return nil
}

func (i NInt64) GetLongArr() *NInt64Arr {
	return nil
}

func (i NInt64) GetList() *NList {
	return nil
}

// Float methods

func (f NFloat32) GetNID() uint8 {
	return 5
}

func (f NFloat32) GetString() *string {
	var r = strconv.FormatFloat(float64(f), 'f', -1, 64)
	return &r
}

func (f NFloat32) GetInt() int {
	return int(f)
}

func (f NFloat32) GetLong() int64 {
	return int64(f)
}

func (f NFloat32) GetFloat() float32 {
	return float32(f)
}

func (f NFloat32) GetDouble() float64 {
	return float64(f)
}

func (f NFloat32) GetByte() byte {
	return byte(f)
}

func (f NFloat32) GetBool() bool {
	return f != 0
}

func (f NFloat32) GetVal(string) *NBitValue {
	return nil
}

func (f NFloat32) GetByIdx(int) *NBitValue {
	return nil
}

func (f NFloat32) Len() int {
	return -1
}

func (f NFloat32) GetByteArr() *NInt8Arr {
	return nil
}

func (f NFloat32) GetIntArr() *NInt32Arr {
	return nil
}

func (f NFloat32) GetLongArr() *NInt64Arr {
	return nil
}

func (f NFloat32) GetList() *NList {
	return nil
}

// Double methods

func (f NFloat64) GetNID() uint8 {
	return 6
}

func (f NFloat64) GetString() *string {
	var r = strconv.FormatFloat(float64(f), 'f', -1, 64)
	return &r
}

func (f NFloat64) GetInt() int {
	return int(f)
}

func (f NFloat64) GetLong() int64 {
	return int64(f)
}

func (f NFloat64) GetFloat() float32 {
	return float32(f)
}

func (f NFloat64) GetDouble() float64 {
	return float64(f)
}

func (f NFloat64) GetByte() byte {
	return byte(f)
}

func (f NFloat64) GetBool() bool {
	return f != 0
}

func (f NFloat64) GetVal(string) *NBitValue {
	return nil
}

func (f NFloat64) GetByIdx(int) *NBitValue {
	return nil
}

func (f NFloat64) Len() int {
	return -1
}

func (f NFloat64) GetByteArr() *NInt8Arr {
	return nil
}

func (f NFloat64) GetIntArr() *NInt32Arr {
	return nil
}

func (f NFloat64) GetLongArr() *NInt64Arr {
	return nil
}

func (f NFloat64) GetList() *NList {
	return nil
}

// String methods

func (s NString) GetNID() uint8 {
	return 8
}

func (s NString) GetString() *string {
	str := (string)(s)
	return &str
}

func (s NString) GetInt() int {
	return len(s)
}

func (s NString) GetLong() int64 {
	return int64(len(s))
}

func (s NString) GetFloat() float32 {
	return float32(len(s))
}

func (s NString) GetDouble() float64 {
	return float64(len(s))
}

func (s NString) GetByte() byte {
	return byte(len(s))
}

func (s NString) GetBool() bool {
	return s != ""
}

func (s NString) GetVal(string) *NBitValue {
	return nil
}

func (s NString) GetByIdx(idx int) *NBitValue {
	ln := len(s)
	if idx >= ln {
		return nil
	}
	val := (NBitValue)(NInt8(s[idx]))
	return &val
}

func (s NString) Len() int {
	return len(s)
}

func (s NString) GetByteArr() *NInt8Arr {
	arr := ([]byte)(s)
	return (*NInt8Arr)(&arr)
}

func (s NString) GetIntArr() *NInt32Arr {
	return nil
}

func (s NString) GetLongArr() *NInt64Arr {
	return nil
}

func (s NString) GetList() *NList {
	return nil
}

// Map methods

func (m NMap) GetNID() uint8 {
	return 10
}

func (m NMap) GetString() *string {
	return nil
}

func (m NMap) GetInt() int {
	return len(m)
}

func (m NMap) GetLong() int64 {
	return int64(len(m))
}

func (m NMap) GetFloat() float32 {
	return float32(len(m))
}

func (m NMap) GetDouble() float64 {
	return float64(len(m))
}

func (m NMap) GetByte() byte {
	return byte(len(m))
}

func (m NMap) GetBool() bool {
	return len(m) != 0
}

func (m NMap) GetVal(key string) *NBitValue {
	return m[key]
}

func (m NMap) GetByIdx(int) *NBitValue {
	return nil
}

func (m NMap) Len() int {
	return len(m)
}

func (m NMap) GetByteArr() *NInt8Arr {
	return nil
}

func (m NMap) GetIntArr() *NInt32Arr {
	return nil
}

func (m NMap) GetLongArr() *NInt64Arr {
	return nil
}

func (m NMap) GetList() *NList {
	return nil
}

// Byte Arr methods

func (a NInt8Arr) GetNID() uint8 {
	return 7
}

func (a NInt8Arr) GetString() *string {
	return nil
}

func (a NInt8Arr) GetInt() int {
	return len(a)
}

func (a NInt8Arr) GetLong() int64 {
	return int64(len(a))
}

func (a NInt8Arr) GetFloat() float32 {
	return 0
}

func (a NInt8Arr) GetDouble() float64 {
	return 0
}

func (a NInt8Arr) GetByte() byte {
	return 0
}

func (a NInt8Arr) GetBool() bool {
	return len(a) != 0
}

func (a NInt8Arr) Len() int {
	return len(a)
}

func (a NInt8Arr) GetVal(string) *NBitValue {
	return nil
}

func (a NInt8Arr) GetByIdx(idx int) *NBitValue {
	if idx < 0 || idx >= len(a) {
		return nil
	}
	var n2 NBitValue
	n2 = NInt8(a[idx])
	return &n2
}

func (a NInt8Arr) GetByteArr() *NInt8Arr {
	return &a
}

func (a NInt8Arr) GetIntArr() *NInt32Arr {
	return nil
}

func (a NInt8Arr) GetLongArr() *NInt64Arr {
	return nil
}

func (a NInt8Arr) GetList() *NList {
	return nil
}

// Int array methods

func (a NInt32Arr) GetNID() uint8 {
	return 11
}

func (a NInt32Arr) GetString() *string {
	return nil
}

func (a NInt32Arr) GetInt() int {
	return len(a)
}

func (a NInt32Arr) GetLong() int64 {
	return int64(len(a))
}

func (a NInt32Arr) GetFloat() float32 {
	return 0
}

func (a NInt32Arr) GetDouble() float64 {
	return 0
}

func (a NInt32Arr) GetByte() byte {
	return 0
}

func (a NInt32Arr) GetBool() bool {
	return len(a) != 0
}

func (a NInt32Arr) Len() int {
	return len(a)
}

func (a NInt32Arr) GetVal(string) *NBitValue {
	return nil
}

func (a NInt32Arr) GetByIdx(idx int) *NBitValue {
	if idx < 0 || idx >= len(a) {
		return nil
	}
	var n2 NBitValue
	n2 = NInt8(a[idx])
	return &n2
}

func (a NInt32Arr) GetByteArr() *NInt8Arr {
	return nil
}

func (a NInt32Arr) GetIntArr() *NInt32Arr {
	return &a
}

func (a NInt32Arr) GetLongArr() *NInt64Arr {
	return nil
}

func (a NInt32Arr) GetList() *NList {
	return nil
}

// Long array methods

func (a NInt64Arr) GetNID() uint8 {
	return 12
}

func (a NInt64Arr) GetString() *string {
	return nil
}

func (a NInt64Arr) GetInt() int {
	return len(a)
}

func (a NInt64Arr) GetLong() int64 {
	return int64(len(a))
}

func (a NInt64Arr) GetFloat() float32 {
	return 0
}

func (a NInt64Arr) GetDouble() float64 {
	return 0
}

func (a NInt64Arr) GetByte() byte {
	return 0
}

func (a NInt64Arr) GetBool() bool {
	return len(a) != 0
}

func (a NInt64Arr) Len() int {
	return len(a)
}

func (a NInt64Arr) GetVal(string) *NBitValue {
	return nil
}

func (a NInt64Arr) GetByIdx(idx int) *NBitValue {
	if idx < 0 || idx >= len(a) {
		return nil
	}
	var n2 NBitValue
	n2 = NInt8(a[idx])
	return &n2
}

func (a NInt64Arr) GetByteArr() *NInt8Arr {
	return nil
}

func (a NInt64Arr) GetIntArr() *NInt32Arr {
	return nil
}

func (a NInt64Arr) GetLongArr() *NInt64Arr {
	return &a
}

func (a NInt64Arr) GetList() *NList {
	return nil
}

// End "methods"

func (e nEND) GetNID() uint8 {
	return 0
}

func (e nEND) GetString() *string {
	return nil
}

func (e nEND) GetInt() int {
	return 0
}

func (e nEND) GetLong() int64 {
	return 0
}

func (e nEND) GetFloat() float32 {
	return 0
}

func (e nEND) GetDouble() float64 {
	return 0
}

func (e nEND) GetByte() byte {
	return 0
}

func (e nEND) GetBool() bool {
	return false
}

func (e nEND) GetVal(string) *NBitValue {
	return nil
}

func (e nEND) GetByIdx(int) *NBitValue {
	return nil
}

func (e nEND) Len() int {
	return -1
}

func (e nEND) GetByteArr() *NInt8Arr {
	return nil
}

func (e nEND) GetIntArr() *NInt32Arr {
	return nil
}

func (e nEND) GetLongArr() *NInt64Arr {
	return nil
}

func (e nEND) GetList() *NList {
	return nil
}

// Utilites funcs

func nBinRef(num int64, idx int) *NBitValue {
	if idx > 63 || idx < 0 {
		return nil
	}
	var data NBitValue = NInt8((num >> idx) & 1)
	return &data
}

func writeHead(val NBitValue, b *bytes.Buffer) {
	b.WriteByte(val.GetNID())
}

// IO (Write) Functions
func writeKV(key string, val NBitValue, b *bytes.Buffer) {
	id := val.GetNID()
	if id != 0 {
		b.WriteByte(id)
		((NString)(key)).WriteTo(b)
		val.WriteTo(b)
	}
}

func (i NBool) WriteTo(b *bytes.Buffer) {
	if i {
		b.WriteByte(1)
	} else {
		b.WriteByte(0)
	}
}

func (i NInt8) WriteTo(b *bytes.Buffer) {
	b.WriteByte(byte(i))
}

func (i NInt16) WriteTo(b *bytes.Buffer) {
	b.WriteByte(byte((i >> 8) & 0xFF))
	b.WriteByte(byte((i) & 0xFF))
}

func (i NInt) WriteTo(b *bytes.Buffer) {
	b.WriteByte(byte((i >> 24) & 0xFF))
	b.WriteByte(byte((i >> 16) & 0xFF))
	b.WriteByte(byte((i >> 8) & 0xFF))
	b.WriteByte(byte((i) & 0xFF))
}

func (i NInt64) WriteTo(b *bytes.Buffer) {
	b.WriteByte(byte((i >> 56) & 0xFF))
	b.WriteByte(byte((i >> 48) & 0xFF))
	b.WriteByte(byte((i >> 40) & 0xFF))
	b.WriteByte(byte((i >> 32) & 0xFF))
	b.WriteByte(byte((i >> 24) & 0xFF))
	b.WriteByte(byte((i >> 16) & 0xFF))
	b.WriteByte(byte((i >> 8) & 0xFF))
	b.WriteByte(byte((i) & 0xFF))
}

func (s NString) WriteTo(b *bytes.Buffer) {
	str := (string)(s)
	NInt16(len(str)).WriteTo(b)
	b.WriteString(str)
}

func (f NFloat32) WriteTo(b *bytes.Buffer) {
	NInt(math.Float32bits(float32(f))).WriteTo(b)
}

func (f NFloat64) WriteTo(b *bytes.Buffer) {
	NInt64(math.Float64bits(float64(f))).WriteTo(b)
}

func (m NMap) WriteTo(b *bytes.Buffer) {
	for k, v := range m {
		writeKV(k, *v, b)
	}
	b.WriteByte(0)
}

func (l NList) WriteTo(b *bytes.Buffer) {
	ln := int(l.Size)
	NInt(ln).WriteTo(b)

	for i := 0; i < ln; i++ {
		obj := l.RawArray[i]
		if obj != nil {
			(*obj).WriteTo(b)
		} else {
			b.WriteByte(0)
		}
	}
}

func (a NInt8Arr) WriteTo(b *bytes.Buffer) {
	NInt(len(a)).WriteTo(b)
	b.Write(a)
}

func (a NInt32Arr) WriteTo(b *bytes.Buffer) {
	dLen := len(a)
	NInt(dLen).WriteTo(b)
	b.Grow(dLen * 4)

	//todo make it faster?
	for i := 0; i < dLen; i++ {
		NInt(a[i]).WriteTo(b)
	}
}

func (a NInt64Arr) WriteTo(b *bytes.Buffer) {
	dLen := len(a)
	NInt(dLen).WriteTo(b)
	b.Grow(dLen * 8)

	//todo make it faster?
	for i := 0; i < dLen; i++ {
		NInt64(a[i]).WriteTo(b)
	}
}

func (e nEND) WriteTo(*bytes.Buffer) {}

//todo

func WriteBytes(obj interface{}, a *bytes.Buffer) bool {
	return writeBytes(obj, a, 0, nil)
}

type wNHead struct {
	head *wNode
	last *wNode
}

type wNode struct {
	type0 int8
	key0  string
	next  *wNode
}

func (n wNHead) add(t int8, key string) {
	if n.head == nil {
		n.head = &wNode{}
		n.last = n.head
	} else {
		cw := &wNode{}
		n.last.next = cw
		n.last = cw
	}
	n.last.key0 = key
	n.last.type0 = t
}

func (n *wNHead) writeAll(b *bytes.Buffer) {
	head := n.head
	for head != nil {
		if head.type0 != 0 {
			NInt8(head.type0).WriteTo(b)
			NString(head.key0).WriteTo(b)
		}
		head = head.next
	}
	n.head = nil
	n.last = nil
}

func writeBytes(obj interface{}, b *bytes.Buffer, deep uint, pre *wNHead) bool {
	if deep > 512 {
		return false
	}
	written := false
	val := reflect.ValueOf(obj)

	knd := val.Kind()

	switch knd {
	case
		reflect.Int:
		if v, ok := obj.(int); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NInt(v), b)
		}
		return true
	case
		reflect.Int8:
		if v, ok := obj.(int8); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NInt8(v), b)
		}
		return true
	case
		reflect.Int16:
		if v, ok := obj.(int16); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NInt16(v), b)
		}
		return true
	case
		reflect.Int32:
		if v, ok := obj.(int32); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NInt(int(v)), b)
		}
		return true
	case
		reflect.Int64:
		if v, ok := obj.(int64); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NInt64(v), b)
		}
		return true
	case
		reflect.Float32:
		if v, ok := obj.(float32); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NFloat32(v), b)
		}
		return true
	case
		reflect.Float64:
		if v, ok := obj.(float64); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NFloat64(v), b)
		}
		return true
	case
		reflect.String:
		if v, ok := obj.(string); ok {
			if pre != nil {
				pre.writeAll(b)
			}
			WriteWithTagTo(NString(v), b)
		}
		return true
	case
		reflect.Array,
		reflect.Slice:
		return false
	}

	type0 := val.Type()
	n := val.NumField()

	for i := 0; i < n; i++ {
		tag := type0.Field(i).Tag.Get("nbt")

		if tag == "-" {
			continue
		}

		raw := val.Field(i).Interface()
		if raw == nil {
			continue
		}

		if val1, ok := raw.(NBitValue); ok {
			if !written {
				if pre != nil {
					pre.writeAll(b)
				}

				b.WriteByte(10)
				written = true
			}
			writeKV(tag, val1, b)
		} else {
			if pre == nil {
				pre = &wNHead{}
			}
			if reflect.TypeOf(raw).Kind() == reflect.Struct {
				pre.add(10, tag)
			}

			if writeBytes(raw, b, deep+1, pre) {
				written = true
			}
		}
	}
	if written {
		b.WriteByte(0)
	}

	return written
}

func WriteWithTagTo(value NBitValue, b *bytes.Buffer) {
	b.WriteByte(value.GetByte())
	value.WriteTo(b)
}

// IO (Read) Functions
//todo

func ReadBytes(obj0 *interface{}, b *bytes.Buffer) error {
	obj := *obj0
	type0 := reflect.TypeOf(obj)

	raw, err := ReadBytesRaw(b)
	if err != nil {
		return err
	}

	if type0 == N_TYPE {
		*obj0 = raw
		return nil
	}
	fmt.Println(raw.GetNID()) //todo
	if raw.GetNID() != 10 {
		return errors.New("only [struct / map] supported")
	}
	return fillRec(obj0, reflect.TypeOf(obj0), raw)
}

func fillRec(obj0 *interface{}, t reflect.Type, data NBitValue) error {
	val := reflect.ValueOf(obj0)

	for i := 0; i < val.NumField(); i++ {
		fld := t.Field(i)

		tag := fld.Tag.Get("nbt")

		if tag != "" {
			rWal := *data.GetVal(tag)
			var cWal interface{}

			vfld := val.Field(i)

			if rWal.GetNID() == 10 {
				c2w := vfld.Interface()
				err := fillRec(&c2w, reflect.TypeOf(c2w), rWal)
				if err != nil {
					return err
				}
			}
			vfld.Set(reflect.ValueOf(cWal))
		}
	}
	return nil
}

func ReadBytesRaw(b *bytes.Buffer) (NBitValue, error) {
	nid, err := ReadNid(b)
	if err != nil {
		return nil, err
	}
	s := 0
	return ReadAnyB(nid, b, &s)
}

func readKV(b *bytes.Buffer, s *int) (string, NBitValue, error) {
	nid, err := ReadNid(b)
	var key NString
	var val NBitValue
	if nid != 0 && err != nil {
		key, err = ReadStr(b, s)
		if err != nil {
			return "", nil, err
		}
		val, err = ReadAnyB(nid, b, s)
		if err != nil {
			return "", nil, err
		}
		return string(key), val, nil
	}
	return "", nil, err
}

func ReadNid(b *bytes.Buffer) (int8, error) {
	nid, err := b.ReadByte()
	return int8(nid), err
}

func ReadAny(b *bytes.Buffer, s *int) (NBitValue, error) {
	nid, err := ReadNid(b)
	if err != nil {
		return nil, err
	}
	return ReadAnyB(nid, b, s)
}

func ReadAnyB(nid int8, b *bytes.Buffer, s *int) (NBitValue, error) {
	switch nid {
	case 0:
		return n_END, nil
	case 1:
		return ReadI8(b, s)
	case 2:
		return ReadI16(b, s)
	case 3:
		return ReadI32(b, s)
	case 4:
		return ReadI64(b, s)
	case 5:
		return ReadF32(b, s)
	case 6:
		return ReadF64(b, s)
	case 7:
		return ReadARRI8(b, s)
	case 8:
		return ReadStr(b, s)
	case 9:
		return ReadList(b, s)
	case 10:
		return ReadMap(b, s)
	case 11:
		return ReadARRI32(b, s)
	case 12:
		return ReadARRI64(b, s)
	default:
		return nil, nil
	}
}

//func ReadB(b *bytes.Buffer, s *int) (NBool, error) {
//	*s += 1
//	v, err := b.ReadByte()
//	if err != nil {
//		return false, err
//	}
//	return v != 0, nil
//}

func ReadI8(b *bytes.Buffer, s *int) (NBitValue, error) {
	*s += 1
	v, err := b.ReadByte()
	if err != nil {
		return NBool(false), err
	}

	if v == 0 {
		return NBool(false), nil
	} else {
		if v == 1 {
			return NBool(true), nil
		}
	}

	return NInt8(v), nil
}

func ReadI16(b *bytes.Buffer, s *int) (NInt16, error) {
	*s += 2
	b1, err1 := b.ReadByte()
	b2, err2 := b.ReadByte()
	if err1 != nil || err2 != nil {
		return 0, errors.Join(err1, err2)
	}
	return NInt16(b1)<<8 | NInt16(b2), nil
}

func ReadI32(b *bytes.Buffer, s *int) (NInt, error) {
	*s += 4
	b1, err1 := b.ReadByte()
	b2, err2 := b.ReadByte()
	b3, err3 := b.ReadByte()
	b4, err4 := b.ReadByte()

	if err1 != nil || err2 != nil || err3 != nil || err4 != nil {
		return 0, errors.Join(err1, err2)
	}

	return (NInt(b1) << 24) | (NInt(b2) << 16) | (NInt(b3) << 8) | NInt(b4), nil
}

func ReadI64(b *bytes.Buffer, s *int) (NInt64, error) {
	*s += 8
	b1, err1 := b.ReadByte()
	b2, err2 := b.ReadByte()
	b3, err3 := b.ReadByte()
	b4, err4 := b.ReadByte()

	b5, err5 := b.ReadByte()
	b6, err6 := b.ReadByte()
	b7, err7 := b.ReadByte()
	b8, err8 := b.ReadByte()

	if err1 != nil || err2 != nil || err3 != nil || err4 != nil ||
		err5 != nil || err6 != nil || err7 != nil || err8 != nil {
		return 0, errors.Join(err1, err2, err3, err4, err5, err6, err7, err8)
	}

	return ((NInt64(b1)) << 56) | ((NInt64(b2)) << 48) | ((NInt64(b3)) << 40) | ((NInt64(b4)) << 32) |
			((NInt64(b5)) << 24) | ((NInt64(b6)) << 16) | ((NInt64(b7)) << 8) | (NInt64(b8)),
		nil
}

func (s NString) ReadStr(b *bytes.Buffer, st *int) (NString, error) {
	ln, err := ReadI16(b, st)
	if err != nil || ln == 0 {
		return "", err
	}
	*st += int(ln)
	data := make([]byte, ln)
	_, err = b.Read(data)

	return NString(data), err
}

func ReadF32(b *bytes.Buffer, s *int) (NFloat32, error) {
	v, err := ReadI32(b, s)
	return NFloat32(math.Float32frombits(uint32(v))), err
}

func ReadF64(b *bytes.Buffer, s *int) (NFloat64, error) {
	v, err := ReadI64(b, s)
	return NFloat64(math.Float64frombits(uint64(v))), err
}

func ReadMap(b *bytes.Buffer, s *int) (NMap, error) {
	nid, err := ReadNid(b)
	mp := NMap{}
	for nid != 0 && err != nil {
		*s += 1
		k, v, err1 := readKV(b, s)
		if err1 != nil {
			return nil, err1
		}
		*s -= 1
		mp[k] = &v
	}
	return mp, nil
}

func ReadList(b *bytes.Buffer, s *int) (NList, error) {
	ln, err := ReadI32(b, s)
	if err != nil || ln == 0 {
		return NList{}, err
	}
	ln2 := int(ln)

	lst := NList{}
	lst.EnsureSize(ln2)

	for i := 0; i <= ln2; i++ {
		obj, err1 := ReadAny(b, s)
		if err1 != nil {
			return lst, err1
		}
		lst.RawArray[i] = &obj
	}

	return lst, nil
}

func ReadARRI8(b *bytes.Buffer, s *int) (NInt8Arr, error) {
	ln, err := ReadI32(b, s)
	if err != nil {
		return nil, err
	}
	arr := make(NInt8Arr, ln)
	_, err = b.Read(arr)
	return arr, err
}

func ReadStr(b *bytes.Buffer, s *int) (NString, error) {
	str, err := ReadARRI8(b, s)
	if err != nil {
		return "", err
	}
	return NString(str), err
}

func ReadARRI32(b *bytes.Buffer, s *int) (NInt32Arr, error) {
	ln, err := ReadI32(b, s)
	if err != nil {
		return nil, err
	}
	arr := make(NInt32Arr, ln)

	ln2 := int(ln)
	var num NInt

	for i := 0; i < ln2; i++ {
		num, err = ReadI32(b, s)
		arr[i] = int32(num)
		if err != nil {
			break
		}
	}
	return arr, err
}

func ReadARRI64(b *bytes.Buffer, s *int) (NInt64Arr, error) {
	ln, err := ReadI32(b, s)
	if err != nil {
		return nil, err
	}
	arr := make(NInt64Arr, ln)

	ln2 := int(ln)
	var num NInt64

	for i := 0; i < ln2; i++ {
		num, err = ReadI64(b, s)
		arr[i] = int64(num)
		if err != nil {
			break
		}
	}
	return arr, err
}
