package store

import (
	"context"
	"log"
	"time"

	"sms-store/models"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

var collection *mongo.Collection

func ConnectMongo() {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	client, err := mongo.Connect(ctx, options.Client().ApplyURI("mongodb://localhost:27017"))
	if err != nil {
		log.Fatal("Failed to connect to MongoDB:", err)
	}

	err = client.Ping(ctx, nil)
	if err != nil {
		log.Fatal("MongoDB ping failed:", err)
	}

	collection = client.Database("sms_service").Collection("messages")
	log.Println("Connected to MongoDB")
}

func SaveMessage(record models.SmsRecord) error {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	_, err := collection.InsertOne(ctx, record)
	return err
}

func GetMessages(userId string) ([]models.SmsRecord, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	cursor, err := collection.Find(ctx, bson.M{"userId": userId})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var messages []models.SmsRecord
	err = cursor.All(ctx, &messages)
	return messages, err
}
