.PHONY: build run test generate migrate-up migrate-down swagger docker-up docker-down lint

# Build the application
build: generate swagger
	go build -o build/pipo-go ./cmd/server

# Run the application
run: generate swagger
	go run ./cmd/server

# Generate sqlc code from schema + queries
generate:
	sqlc generate

# Generate swagger docs
swagger:
	swag init -g cmd/server/main.go -o docs

# Run all tests
test:
	go test -v -race -cover ./...

# Run migrations up
migrate-up:
	migrate -path migrations -database "postgres://$${DB_USER}:$${DB_PASSWORD}@$${DB_HOST}:$${DB_PORT}/$${DB_NAME}?sslmode=disable" up

# Run migrations down
migrate-down:
	migrate -path migrations -database "postgres://$${DB_USER}:$${DB_PASSWORD}@$${DB_HOST}:$${DB_PORT}/$${DB_NAME}?sslmode=disable" down

# Docker
docker-up:
	docker-compose up --build -d

docker-down:
	docker-compose down

# Lint
lint:
	golangci-lint run ./...
