package handlers

import (
	"encoding/json"
	"log"
	"net/http"

	"sms-store/models"
	"sms-store/store"
)

func GetMessagesHandler(w http.ResponseWriter, r *http.Request) {
	userId := r.PathValue("userId")

	if userId == "" {
		http.Error(w, `{"error":"userId is required"}`, http.StatusBadRequest)
		return
	}

	messages, err := store.GetMessages(userId)
	if err != nil {
		log.Println("Error fetching messages:", err)
		http.Error(w, `{"error":"failed to fetch messages"}`, http.StatusInternalServerError)
		return
	}

	if messages == nil {
		messages = []models.SmsRecord{}
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(messages)
}

func HealthHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.Write([]byte(`{"status":"ok"}`))
}
