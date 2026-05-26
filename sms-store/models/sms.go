package models

type SmsRecord struct {
	PhoneNumber string `json:"phoneNumber" bson:"phoneNumber"`
	Message     string `json:"message"     bson:"message"`
	Status      string `json:"status"      bson:"status"`
	SentAt      string `json:"sentAt"      bson:"sentAt"`
}
