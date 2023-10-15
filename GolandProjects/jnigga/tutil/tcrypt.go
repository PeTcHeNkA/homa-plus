package tutil

import (
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/sha512"
	"crypto/subtle"
	"crypto/x509"
	"encoding/pem"
	"errors"
	"hash"
	"io"
	"log"
	"math/big"
	"sync"
)

type TCrypter struct {
	key *rsa.PrivateKey
	pub *rsa.PublicKey
}

const errorEcnode = "failed to load key"

func readPem(key string) *pem.Block {
	block, _ := pem.Decode([]byte(key))
	if block == nil {
		log.Fatal(errorEcnode)
	}
	return block
}

func (enc TCrypter) ReadKey(key string) TCrypter {
	block := readPem(key)
	priv, err := x509.ParsePKCS1PrivateKey(block.Bytes)
	if err != nil {
		log.Fatal(errorEcnode)
	}
	enc.key = priv
	enc.pub = &priv.PublicKey
	return enc
}

func (enc TCrypter) ReadPub(key string) TCrypter {
	block := readPem(key)
	key0, err := x509.ParsePKIXPublicKey(block.Bytes)
	if err != nil {
		log.Fatal(errorEcnode)
	}
	enc.pub = key0.(*rsa.PublicKey)
	return enc
}

func (enc *TCrypter) Check(sign []byte, msg []byte) bool {
	options := rsa.PSSOptions{
		Hash:       crypto.SHA256,
		SaltLength: 32,
	}
	sh2 := sha256.New()
	sh2.Write(msg)
	sum := sh2.Sum(nil)
	err := rsa.VerifyPSS(enc.pub, crypto.SHA256, sum, sign, &options)
	return err == nil
}

func (enc *TCrypter) Decrypt(msg []byte) []byte {
	result, err := decryptOAEP(sha256.New(), sha512.New(), rand.Reader, enc.key, msg, nil)
	if err != nil {
		println(err.Error())
	}
	return result
}

func (enc *TCrypter) Encrypt(msg []byte) []byte {
	result, err := encryptOAEP(sha256.New(), sha512.New(), rand.Reader, enc.pub, msg, nil)
	if err != nil {
		println(err.Error())
	}
	return result
}

//FIX RSA -------------------------------------------------------------------

var (
	closedChanOnce    sync.Once
	closedChan        chan struct{}
	bigOne            = big.NewInt(1)
	bigZero           = big.NewInt(0)
	errPublicModulus  = errors.New("crypto/rsa: missing public modulus")
	errPublicExponent = errors.New("crypto/rsa: public exponent not valid")
)

func encryptOAEP(hash hash.Hash, hashMGF1 hash.Hash, random io.Reader, pub *rsa.PublicKey, msg []byte, label []byte) ([]byte, error) {
	if err := checkPub(pub); err != nil {
		return nil, err
	}
	hash.Reset()
	k := pub.Size()
	if len(msg) > k-2*hash.Size()-2 {
		return nil, rsa.ErrMessageTooLong
	}

	hash.Write(label)
	lHash := hash.Sum(nil)
	hash.Reset()

	em := make([]byte, k)
	seed := em[1 : 1+hash.Size()]
	db := em[1+hash.Size():]

	copy(db[0:hash.Size()], lHash)
	db[len(db)-len(msg)-1] = 1
	copy(db[len(db)-len(msg):], msg)

	_, err := io.ReadFull(random, seed)
	if err != nil {
		return nil, err
	}

	hashMGF1.Reset()
	mgf1XOR(db, hashMGF1, seed)
	mgf1XOR(seed, hashMGF1, db)

	m := new(big.Int)
	m.SetBytes(em)
	c := encrypt(new(big.Int), pub, m)

	out := make([]byte, k)
	return c.FillBytes(out), nil
}

func encrypt(c *big.Int, pub *rsa.PublicKey, m *big.Int) *big.Int {
	e := big.NewInt(int64(pub.E))
	c.Exp(m, e, pub.N)
	return c
}

func decryptOAEP(hash hash.Hash, hashMGF1 hash.Hash, random io.Reader, priv *rsa.PrivateKey, ciphertext []byte, label []byte) ([]byte, error) { // hashMGF1 hash.Hash added
	if err := checkPub(&priv.PublicKey); err != nil {
		return nil, err
	}
	k := priv.Size()
	if len(ciphertext) > k || k < hash.Size()*2+2 {
		return nil, rsa.ErrDecryption
	}

	c := new(big.Int).SetBytes(ciphertext)

	m, err := decrypt(random, priv, c)
	if err != nil {
		return nil, err
	}

	hash.Write(label)
	lHash := hash.Sum(nil)
	hash.Reset()

	// We probably leak the number of leading zeros.
	// It's not clear that we can do anything about this.
	em := m.FillBytes(make([]byte, k))

	firstByteIsZero := subtle.ConstantTimeByteEq(em[0], 0)

	seed := em[1 : hash.Size()+1]
	db := em[hash.Size()+1:]

	hashMGF1.Reset()
	mgf1XOR(seed, hashMGF1, db) // apply hashMGF1
	mgf1XOR(db, hashMGF1, seed) // apply hashMGF1

	lHash2 := db[0:hash.Size()]

	// We have to validate the plaintext in constant time in order to avoid
	// attacks like: J. Manger. A Chosen Ciphertext Attack on RSA Optimal
	// Asymmetric Encryption Padding (OAEP) as Standardized in PKCS #1
	// v2.0. In J. Kilian, editor, Advances in Cryptology.
	lHash2Good := subtle.ConstantTimeCompare(lHash, lHash2)

	// The remainder of the plaintext must be zero or more 0x00, followed
	// by 0x01, followed by the message.
	//   lookingForIndex: 1 iff we are still looking for the 0x01
	//   index: the offset of the first 0x01 byte
	//   invalid: 1 iff we saw a non-zero byte before the 0x01.
	var lookingForIndex, index, invalid int
	lookingForIndex = 1
	rest := db[hash.Size():]

	for i := 0; i < len(rest); i++ {
		equals0 := subtle.ConstantTimeByteEq(rest[i], 0)
		equals1 := subtle.ConstantTimeByteEq(rest[i], 1)
		index = subtle.ConstantTimeSelect(lookingForIndex&equals1, i, index)
		lookingForIndex = subtle.ConstantTimeSelect(equals1, 0, lookingForIndex)
		invalid = subtle.ConstantTimeSelect(lookingForIndex&^equals0, 1, invalid)
	}

	if firstByteIsZero&lHash2Good&^invalid&^lookingForIndex != 1 {
		return nil, rsa.ErrDecryption
	}

	return rest[index+1:], nil
}

func decrypt(random io.Reader, priv *rsa.PrivateKey, c *big.Int) (m *big.Int, err error) {
	if c.Cmp(priv.N) > 0 {
		err = rsa.ErrDecryption
		return
	}
	if priv.N.Sign() == 0 {
		return nil, rsa.ErrDecryption
	}

	var ir *big.Int
	if random != nil {
		maybeReadByte(random)

		var r *big.Int
		ir = new(big.Int)
		for {
			r, err = rand.Int(random, priv.N)
			if err != nil {
				return
			}
			if r.Cmp(bigZero) == 0 {
				r = bigOne
			}
			ok := ir.ModInverse(r, priv.N)
			if ok != nil {
				break
			}
		}
		bigE := big.NewInt(int64(priv.E))
		rpowe := new(big.Int).Exp(r, bigE, priv.N) // N != 0
		cCopy := new(big.Int).Set(c)
		cCopy.Mul(cCopy, rpowe)
		cCopy.Mod(cCopy, priv.N)
		c = cCopy
	}

	if priv.Precomputed.Dp == nil {
		m = new(big.Int).Exp(c, priv.D, priv.N)
	} else {
		m = new(big.Int).Exp(c, priv.Precomputed.Dp, priv.Primes[0])
		m2 := new(big.Int).Exp(c, priv.Precomputed.Dq, priv.Primes[1])
		m.Sub(m, m2)
		if m.Sign() < 0 {
			m.Add(m, priv.Primes[0])
		}
		m.Mul(m, priv.Precomputed.Qinv)
		m.Mod(m, priv.Primes[0])
		m.Mul(m, priv.Primes[1])
		m.Add(m, m2)

		for i, values := range priv.Precomputed.CRTValues {
			prime := priv.Primes[2+i]
			m2.Exp(c, values.Exp, prime)
			m2.Sub(m2, m)
			m2.Mul(m2, values.Coeff)
			m2.Mod(m2, prime)
			if m2.Sign() < 0 {
				m2.Add(m2, prime)
			}
			m2.Mul(m2, values.R)
			m.Add(m, m2)
		}
	}

	if ir != nil {
		m.Mul(m, ir)
		m.Mod(m, priv.N)
	}

	return
}

func maybeReadByte(r io.Reader) {
	closedChanOnce.Do(func() {
		closedChan = make(chan struct{})
		close(closedChan)
	})
	select {
	case <-closedChan:
		return
	case <-closedChan:
		var buf [1]byte
		_, _ = r.Read(buf[:])
	}
}

func mgf1XOR(out []byte, hash hash.Hash, seed []byte) {
	var counter [4]byte
	var digest []byte

	done := 0
	for done < len(out) {
		hash.Write(seed)
		hash.Write(counter[0:4])
		digest = hash.Sum(digest[:0])
		hash.Reset()

		for i := 0; i < len(digest) && done < len(out); i++ {
			out[done] ^= digest[i]
			done++
		}
		incCounter(&counter)
	}
}

func checkPub(pub *rsa.PublicKey) error {
	if pub.N == nil {
		return errPublicModulus
	}
	if pub.E < 2 || pub.E > 1<<31-1 {
		return errPublicExponent
	}
	return nil
}

func incCounter(c *[4]byte) {
	if c[3]++; c[3] != 0 {
		return
	}
	if c[2]++; c[2] != 0 {
		return
	}
	if c[1]++; c[1] != 0 {
		return
	}
	c[0]++
}
