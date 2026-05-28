package handlers

import (
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestGetMessagesHandlerMissingUserId(t *testing.T) {
	req := httptest.NewRequest("GET", "/v1/user//messages", nil)
	w := httptest.NewRecorder()

	GetMessagesHandler(w, req)

	if w.Code != http.StatusBadRequest {
		t.Errorf("expected 400, got %d", w.Code)
	}
}
