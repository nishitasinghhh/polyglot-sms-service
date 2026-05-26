package kafka

import (
	"context"
	"encoding/json"
	"log"

	"sms-store/models"
	"sms-store/store"

	"github.com/segmentio/kafka-go"
)

func StartConsumer() {
	reader := kafka.NewReader(kafka.ReaderConfig{
		Brokers: []string{"localhost:9092"},
		Topic:   "sms-events",
		GroupID: "sms-store-group",
	})

	log.Println("Kafka consumer started, listening on 'sms-events'...")

	for {
		msg, err := reader.ReadMessage(context.Background())
		if err != nil {
			log.Println("Kafka read error:", err)
			continue
		}

		var record models.SmsRecord
		err = json.Unmarshal(msg.Value, &record)
		if err != nil {
			log.Println("JSON parse error:", err)
			continue
		}

		err = store.SaveMessage(record)
		if err != nil {
			log.Println("MongoDB save error:", err)
		} else {
			log.Println("Saved SMS for", record.PhoneNumber)
		}
	}
}
