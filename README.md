# Polyglot Distributed SMS Service

A distributed SMS notification system built with two microservices communicating asynchronously via Apache Kafka.

## Architecture

```
                        ┌─────────────────┐
                        │      Kafka      │
                        │  (sms-events)   │
                        └───▲─────────┬───┘
               4. Publish   │         │  5. Consume
                  Event     │         ▼
┌───────────────────────┐       ┌───────────────────────┐
│  SMS Sender (Java)    │       │  SMS Store (GoLang)   │
│  Spring Boot          │       │  net/http             │
│                       │       │                       │
│  POST /v1/sms/send    │       │  GET /v1/user/{userId}│
│                       │       │      /messages        │
└──┬──────────┬─────────┘       └───────────┬───────────┘
   │          │                             │
   │ 2.Check  │ 3.Send                      │ 6.Store
   │          │                             │
┌──▼──┐  ┌───▼──────────┐           ┌──────▼──────┐
│Redis│  │ 3P SMS Vendor│           │  MongoDB    │
│     │  │  (Mocked)    │           │             │
└─────┘  └──────────────┘           └─────────────┘
```

### Data Flow

1. Client sends a POST request to the Java SMS Sender service.
2. Java checks the Redis block list — if the user is blocked, the request is rejected.
3. Java calls the 3rd party SMS vendor API (mocked with random SUCCESS/FAIL).
4. Java publishes an SMS event (with status) to the Kafka topic `sms-events`.
5. Go SMS Store consumes the event from Kafka.
6. Go stores the SMS record in MongoDB.
7. Clients can retrieve SMS history by calling the Go service's GET API.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| SMS Sender | Java 17, Spring Boot 4 |
| SMS Store | Go 1.22+, standard net/http |
| Message Broker | Apache Kafka |
| Block List | Redis |
| Data Store | MongoDB |
| Containerization | Docker Compose |

## Prerequisites

- Java 17 or later
- Go 1.22 or later
- Docker Desktop

## How to Run Locally

### Step 1: Clone the repository

```bash
git clone https://github.com/nishitasinghhh/polyglot-sms-service.git
cd polyglot-sms-service
```

### Step 2: Start infrastructure (Kafka, Redis, MongoDB)

```bash
docker-compose up -d
```

Verify all services are running:

```bash
docker-compose ps
```

All 4 containers (kafka, zookeeper, redis, mongodb) should show "Up".

### Step 3: Start the Go SMS Store service

```bash
cd sms-store
go mod tidy
go run .
```

You should see:

```
Connected to MongoDB
Kafka consumer started, listening on 'sms-events'...
SMS Store running on :8081
```

### Step 4: Start the Java SMS Sender service (in a new terminal)

```bash
cd sms-sender
./mvnw spring-boot:run
```

You should see:

```
Tomcat started on port 8080
Started SmsSenderApplication
```

Both services are now running.

## API Endpoints

### Service 1: SMS Sender (Java) — Port 8080

#### POST /v1/sms/send

Sends an SMS message. Checks the Redis block list, calls the mock 3P vendor, and publishes an event to Kafka.

**Request:**

```
POST http://localhost:8080/v1/sms/send
Content-Type: application/json
```

```json
{
  "userId": "user_42",
  "phoneNumber": "+919876543210",
  "message": "Hello World"
}
```

**Responses:**

| Status | Meaning |
|--------|---------|
| `SUCCESS` | SMS sent and event published to Kafka |
| `BLOCKED` | Phone number is in the Redis block list |
| `FAIL` | 3P vendor failed to deliver the SMS |
| `PARTIAL` | SMS sent but Kafka event publish failed |
| `ERROR` | Validation error or service unavailable |

**Success response:**

```json
{
  "status": "SUCCESS",
  "message": "SMS sent successfully"
}
```

**Blocked user response:**

```json
{
  "status": "BLOCKED",
  "message": "User is in the block list"
}
```

**Validation error response:**

```json
{
  "status": "ERROR",
  "message": "Phone number is required"
}
```

**Malformed JSON response (400 Bad Request):**

```json
{
  "status": "ERROR",
  "message": "Invalid JSON format. Please check your request body."
}
```

#### POST /v1/sms/block/{phoneNumber}

Adds a phone number to the block list.

```
POST http://localhost:8080/v1/sms/block/+919876543210
```

#### DELETE /v1/sms/block/{phoneNumber}

Removes a phone number from the block list.

```
DELETE http://localhost:8080/v1/sms/block/+919876543210
```

### Service 2: SMS Store (GoLang) — Port 8081

#### GET /v1/user/{userId}/messages

Fetches all stored SMS records for a specific userId.

**Request:**

```
GET http://localhost:8081/v1/user/user_42/messages
```

**Response:**

```json
[
  {
    "userId": "user_42",
    "phoneNumber": "+919876543210",
    "message": "Hello World",
    "status": "SUCCESS",
    "sentAt": "2026-05-28T10:30:00.123456Z"
  }
]
```

Returns an empty array `[]` if no messages exist for the given userId.

#### GET /health

Health check endpoint.

```
GET http://localhost:8081/health
```

**Response:**

```json
{
  "status": "ok"
}
```

## End-to-End Demonstration

A demo script is included. With both services running:

```bash
./demo.sh
```

Or run the steps manually:

```bash
# 1. Block a user
docker exec -it redis redis-cli SADD blocked_users "+91blocked"

# 2. Try sending to blocked user (should return BLOCKED)
curl -X POST http://localhost:8080/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","phoneNumber":"+91blocked","message":"Hello"}'

# 3. Send to normal user (should return SUCCESS)
curl -X POST http://localhost:8080/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{"userId":"user_42","phoneNumber":"+919876543210","message":"Demo message!"}'

# 4. Check the Go service terminal — you should see:
#    "Saved SMS for +919876543210"

# 5. Fetch SMS history from Go service
curl http://localhost:8081/v1/user/user_42/messages
```

## Running Tests

### Java (6 tests)

```bash
cd sms-sender
./mvnw test
```

### Go (5 tests)

```bash
cd sms-store
go test ./...
```

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Redis unreachable | Fail-closed — SMS blocked for safety, returns ERROR |
| Kafka down | Returns PARTIAL — SMS sent but event not logged |
| 3P vendor failure | Returns FAIL status |
| Go service unreachable | Logs warning, continues (Kafka handles delivery) |
| Malformed JSON request | Returns 400 with clear error message |
| Empty phone number | Returns ERROR with validation message |
| Empty message | Returns ERROR with validation message |
| MongoDB down (Go side) | Logs error, Kafka retries the message |

## Project Structure

```
polyglot-sms-service/
├── docker-compose.yml              # Infrastructure (Kafka, Redis, MongoDB)
├── README.md                       # This file
├── demo.sh                         # End-to-end demo script
│
├── sms-sender/                     # Java / Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/sms/
│       ├── SmsSenderApplication.java
│       ├── AppConfig.java
│       ├── controller/
│       │   ├── SmsController.java
│       │   └── GlobalExceptionHandler.java
│       ├── service/
│       │   ├── SmsService.java
│       │   ├── BlockListService.java
│       │   ├── SmsVendorService.java
│       │   └── SmsStoreClient.java
│       ├── kafka/
│       │   └── SmsEventProducer.java
│       └── model/
│           ├── SmsRequest.java
│           ├── SmsResponse.java
│           └── SmsEvent.java
│
└── sms-store/                      # GoLang
    ├── go.mod
    ├── main.go
    ├── handlers/
    │   ├── messages.go
    │   └── messages_test.go
    ├── kafka/
    │   └── consumer.go
    ├── store/
    │   └── mongodb.go
    └── models/
        └── sms.go
```