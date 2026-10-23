package tutil

import (
	"io"
	"net/http"
	"time"
)

var httpClient *http.Client

func GetHttpClient() *http.Client {
	if httpClient != nil {
		return httpClient
	}
	client := http.Client{
		Timeout: 300 * time.Second,
	}
	httpClient = &client

	return &client
}

func ReadAllSmart(url string, cli *http.Client) ([]byte, error) {
	resp, err := cli.Get(url)
	defer resp.Body.Close()
	if err != nil {
		return nil, err
	}
	b := make([]byte, 0, 512)
	result := make(chan []byte, 1)

	var fres []byte = nil

	for fres == nil {
		go func() {
			n, err := resp.Body.Read(b[len(b):cap(b)])
			b = b[:len(b)+n]
			if err != nil {
				if err == io.EOF {
					err = nil
				}
				fres = b
			}
			if len(b) == cap(b) {
				b = append(b, 0)[:len(b)]
			}
			result <- b
		}()
		select {
		case <-time.After(5 * time.Second):
			return nil, netError
		case _ = <-result:
			continue
		}
	}
	return fres, err
}

func ReadAllBytes(url string, cli *http.Client) ([]byte, error) {
	resp, err := cli.Get(url)
	if err != nil {
		return nil, err
	}
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	return body, nil
}
