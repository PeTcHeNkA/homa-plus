package main

import (
	"fmt"
	"main/tutil"
	"net/http"
	"os"
	"strings"
)

type DynAssets struct {
	http.Handler
}

func (h *DynAssets) ServeHTTP(res http.ResponseWriter, req *http.Request) {
	var err error
	requestedFilename := strings.TrimPrefix(req.URL.Path, "/")
	//println("Requesting file:", requestedFilename)
	fileData, err := os.ReadFile(tutil.MCINF.LaunchPatch + "/" + requestedFilename)
	if err != nil {
		res.WriteHeader(http.StatusBadRequest)
		res.Write([]byte(fmt.Sprintf("Could not load file %s", tutil.MCINF.LaunchPatch+"/"+requestedFilename)))
	}

	res.Write(fileData)
}
