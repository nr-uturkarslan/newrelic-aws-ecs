package main

import (
	"time"
)

func main() {

	s := Simulator{}
	s.initialize()

	go s.makeCreateRequests()
	go s.makeListRequests()
	go s.makeDeleteRequests()

	for {
		time.Sleep(time.Minute * 10)
	}
}
