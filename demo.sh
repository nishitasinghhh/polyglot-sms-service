#!/bin/bash

echo "============================================"
echo "Polyglot SMS Service — End-to-End Demo"
echo "============================================"
echo ""

echo "Pre-requisites:"
echo "  1. Docker containers running (docker-compose up -d)"
echo "  2. Go service running (cd sms-store && go run .)"
echo "  3. Java service running (cd sms-sender && ./mvnw spring-boot:run)"
echo ""
echo "Press Enter to start the demo..."
read

echo "--- Step 1: Block a user ---"
docker exec -it redis redis-cli SADD blocked_users "+91blocked"
echo ""

echo "--- Step 2: Send SMS to blocked user (should return BLOCKED) ---"
curl -s -X POST http://localhost:8080/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","phoneNumber":"+91blocked","message":"Hello"}'
echo ""
echo ""

echo "--- Step 3: Send SMS to normal user (should return SUCCESS) ---"
curl -s -X POST http://localhost:8080/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{"userId":"user_demo","phoneNumber":"+919876543210","message":"End-to-end demo message!"}'
echo ""
echo ""

echo "--- Step 4: Check Go service logs ---"
echo ">>> Look at the Go service terminal. You should see:"
echo "    'Saved SMS for +919876543210'"
echo ""

echo "--- Step 5: Wait 2 seconds for Kafka processing ---"
sleep 2

echo "--- Step 6: Fetch SMS history from Go service ---"
curl -s http://localhost:8081/v1/user/user_demo/messages | python3 -m json.tool
echo ""

echo "--- Step 7: Test malformed JSON (should return ERROR) ---"
curl -s -X POST http://localhost:8080/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{bad json}'
echo ""
echo ""

echo "--- Step 8: Test empty phone number (should return ERROR) ---"
curl -s -X POST http://localhost:8080/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","phoneNumber":"","message":"Hello"}'
echo ""
echo ""

echo "--- Step 9: Unblock the user ---"
curl -s -X DELETE http://localhost:8080/v1/sms/block/+91blocked
echo ""
echo ""

echo "--- Step 10: Send to previously blocked user (should now return SUCCESS) ---"
curl -s -X POST http://localhost:8080/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","phoneNumber":"+91blocked","message":"Hello after unblock!"}'
echo ""
echo ""

echo "============================================"
echo "Demo complete! All steps executed."
echo "============================================"