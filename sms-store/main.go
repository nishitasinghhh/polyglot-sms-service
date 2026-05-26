package main

import (
	"log"
	"net/http"

	"sms-store/handlers"
	"sms-store/kafka"
	"sms-store/store"
)

func main() {
	store.ConnectMongo()
	go kafka.StartConsumer()
	http.HandleFunc("GET /v1/user/{userId}/messages", handlers.GetMessagesHandler)
	log.Println("SMS Store running on :8081")
	log.Fatal(http.ListenAndServe(":8081", nil))
}
