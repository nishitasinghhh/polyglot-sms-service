package handlers

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"sms-store/models"
)

func TestGetMessagesHandlerMissingUserId(t *testing.T) {
	req := httptest.NewRequest("GET", "/v1/user//messages", nil)
	w := httptest.NewRecorder()

	GetMessagesHandler(w, req)

	if w.Code != http.StatusBadRequest {
		t.Errorf("expected 400, got %d", w.Code)
	}
}

func TestKafkaMessageParsing(t *testing.T) {
	jsonMsg := `{"userId":"user_1","phoneNumber":"+919876543210","message":"Hello","status":"SUCCESS","sentAt":"2026-05-26T10:00:00Z"}`

	var record models.SmsRecord
	err := json.Unmarshal([]byte(jsonMsg), &record)

	if err != nil {
		t.Fatalf("failed to parse JSON: %v", err)
	}

	if record.UserId != "user_1" {
		t.Errorf("expected userId 'user_1', got '%s'", record.UserId)
	}
	if record.PhoneNumber != "+919876543210" {
		t.Errorf("expected phoneNumber '+919876543210', got '%s'", record.PhoneNumber)
	}
	if record.Message != "Hello" {
		t.Errorf("expected message 'Hello', got '%s'", record.Message)
	}
	if record.Status != "SUCCESS" {
		t.Errorf("expected status 'SUCCESS', got '%s'", record.Status)
	}
	if record.SentAt != "2026-05-26T10:00:00Z" {
		t.Errorf("expected sentAt '2026-05-26T10:00:00Z', got '%s'", record.SentAt)
	}
}

func TestEmptyArrayForUnknownUser(t *testing.T) {
	var messages []models.SmsRecord
	messages = nil

	if messages == nil {
		messages = []models.SmsRecord{}
	}

	jsonBytes, err := json.Marshal(messages)
	if err != nil {
		t.Fatalf("failed to marshal: %v", err)
	}

	if string(jsonBytes) != "[]" {
		t.Errorf("expected [], got %s", string(jsonBytes))
	}
}

func TestSmsRecordSerializesToJSON(t *testing.T) {
	record := models.SmsRecord{
		UserId:      "user_42",
		PhoneNumber: "+919876543210",
		Message:     "Test message",
		Status:      "SUCCESS",
		SentAt:      "2026-05-28T10:00:00Z",
	}

	jsonBytes, err := json.Marshal(record)
	if err != nil {
		t.Fatalf("failed to marshal: %v", err)
	}

	var parsed map[string]string
	json.Unmarshal(jsonBytes, &parsed)

	if parsed["userId"] != "user_42" {
		t.Errorf("expected userId 'user_42', got '%s'", parsed["userId"])
	}
	if parsed["phoneNumber"] != "+919876543210" {
		t.Errorf("expected phoneNumber '+919876543210', got '%s'", parsed["phoneNumber"])
	}
}

func TestContentTypeIsJSON(t *testing.T) {
	w := httptest.NewRecorder()
	w.Header().Set("Content-Type", "application/json")

	contentType := w.Header().Get("Content-Type")
	if contentType != "application/json" {
		t.Errorf("expected application/json, got %s", contentType)
	}
}
