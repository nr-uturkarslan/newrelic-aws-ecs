package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"math/rand"
	"net/http"
	"strconv"
	"time"
)

const LOAD_BALANCER_URL string = "http://LOAD_BALANCER_URL"

const CREATE_INTERVAL = time.Second * 4
const LIST_INTERVAL = time.Second * 2
const DELETE_INTERVAL = time.Second * 8

type RequestDto struct {
	CustomItem CustomItem `json:"customItem"`
}

type ResponseDto struct {
	Data []CustomItem `json:"data"`
}

type Simulator struct {
	Departments [][]string
	CustomItems []CustomItem
}

func (s *Simulator) initialize() {
	rand.Seed(time.Now().UnixNano())
	s.Departments = createDepartments()
	s.CustomItems = createCustomItems()
}

func (s *Simulator) makeCreateRequests() {
	for {
		func() {
			department, user := s.getRandomDepartmentAndUser()
			requestDto := RequestDto{
				CustomItem: s.getRandomCustomItem(),
			}

			client := &http.Client{
				Timeout: time.Second * 10,
			}

			requestDtoInBytes, _ := json.Marshal(requestDto)
			request, _ := http.NewRequest(http.MethodPost,
				LOAD_BALANCER_URL+"/proxy/create",
				bytes.NewBufferString(string(requestDtoInBytes)),
			)

			request.Header.Set("Content-Type", "application/json")
			request.Header.Add("x-user-department", department)
			request.Header.Add("x-user-name", user)

			response, err := client.Do(request)
			if err != nil {
				fmt.Println(err)
				return
			}
			defer response.Body.Close()

			fmt.Println("Create response: " + strconv.Itoa(response.StatusCode))
			time.Sleep(CREATE_INTERVAL)
		}()
	}
}

func (s *Simulator) makeListRequests() {
	for {
		func() {
			department, user := s.getRandomDepartmentAndUser()

			client := &http.Client{
				Timeout: time.Second * 10,
			}

			request, _ := http.NewRequest(http.MethodGet,
				LOAD_BALANCER_URL+"/proxy/list?limit=5", nil)

			request.Header.Set("Content-Type", "application/json")
			request.Header.Add("x-user-department", department)
			request.Header.Add("x-user-name", user)

			response, err := client.Do(request)
			if err != nil {
				fmt.Println(err)
				return
			}
			defer response.Body.Close()

			fmt.Println("List response: " + strconv.Itoa(response.StatusCode))
			time.Sleep(LIST_INTERVAL)
		}()
	}
}

func (s *Simulator) makeDeleteRequests() {
	for {
		func() {
			time.Sleep(DELETE_INTERVAL)
			department, user := s.getRandomDepartmentAndUser()

			listClient := &http.Client{
				Timeout: time.Second * 10,
			}

			listRequest, _ := http.NewRequest(http.MethodGet,
				LOAD_BALANCER_URL+"/proxy/list?limit=1", nil)

			listRequest.Header.Set("Content-Type", "application/json")
			listRequest.Header.Add("x-user-department", department)
			listRequest.Header.Add("x-user-name", user)

			listResponse, err := listClient.Do(listRequest)
			if err != nil {
				fmt.Println(err)
				return
			}
			defer listResponse.Body.Close()

			responseDtoInBytes, err := ioutil.ReadAll(listResponse.Body)
			if err != nil {
				fmt.Println(err)
			}

			var responseDto ResponseDto
			json.Unmarshal(responseDtoInBytes, &responseDto)

			if len(responseDto.Data) == 0 {
				fmt.Println("No items to delete.")
				return
			}

			customItem := responseDto.Data[0]

			deleteClient := &http.Client{
				Timeout: time.Second * 10,
			}

			deleteRequest, _ := http.NewRequest(http.MethodDelete,
				LOAD_BALANCER_URL+"/proxy/delete?customItemId="+customItem.Id, nil)

			listRequest.Header.Set("Content-Type", "application/json")
			listRequest.Header.Add("x-user-department", department)
			listRequest.Header.Add("x-user-name", user)

			deleteResponse, err := deleteClient.Do(deleteRequest)
			if err != nil {
				fmt.Println(err)
				return
			}
			defer deleteResponse.Body.Close()

			fmt.Println("Delete response: " + strconv.Itoa(deleteResponse.StatusCode))
		}()
	}
}

func (s *Simulator) getRandomCustomItem() CustomItem {
	randomNumber := rand.Intn(len(s.CustomItems))
	return s.CustomItems[randomNumber]
}

func (s Simulator) getRandomDepartmentAndUser() (
	string,
	string,
) {
	randomNumber := rand.Intn(len(s.Departments))
	entry := s.Departments[randomNumber]

	return entry[0], entry[1]
}
