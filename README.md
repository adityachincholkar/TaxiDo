# UrbanGo — Real-time Ride‑Sharing Dispatch System

A full-stack ride-hailing application with a Spring Boot backend and a simple HTML-based frontend for testing Razorpay payments and real-time WebSocket messaging.

## Features

- __Backend (Spring Boot):__
  - User auth (JWT) for Rider/Driver registration and login (`/api/auth/...`)
  - Role-based access control (Spring Security)
  - Ride lifecycle management: request, accept, start, complete, cancel, rate
  - Nearby drivers and geo-location tracking with Redis Geo
  - WebSocket (STOMP over SockJS) for driver live location streaming
  - Razorpay integration for ride payments
  - Email support (SMTP)
  - PostgreSQL persistence with JPA/Hibernate
- __Frontend (Static HTML):__
  - `Userfrontend/index.html`: Razorpay payment test UI
  - `Userfrontend/testing.html`: WebSocket test client for driver location

## Tech Stack

- __Backend:__
  - Spring Boot 3.5.4 (`urban-go/pom.xml`)
  - Java 21
  - Spring Data JPA + Hibernate
  - PostgreSQL (runtime driver in `pom.xml`)
  - Spring Security + JJWT (0.11.5)
  - Spring WebSocket (STOMP, SockJS)
  - Spring Data Redis
  - Spring Mail
  - Lombok, MapStruct
  - Razorpay Java SDK
- __Frontend:__
  - Plain HTML/JS
  - Razorpay Checkout.js
  - SockJS + STOMP.js

## Project Structure

- Backend: `urban-go/`
- Frontend: `Userfrontend/`
  - `index.html` (Razorpay test)
  - `testing.html` (WebSocket test)

## Prerequisites

- Java 21 (JDK 21)
- Maven 3.9+
- PostgreSQL 14+ (configured database and user)
- Redis 6+
- Internet access for CDN scripts (Razorpay, SockJS, STOMP)
- Optional: A static file server (or open HTML files directly in a browser)

## Configuration

Backend config is in `urban-go/src/main/resources/application.properties`. Replace secrets with your own and preferably move them to environment variables or a `application-local.properties` that’s gitignored.

Example (do NOT commit secrets):
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/uber
spring.datasource.username=postgres
spring.datasource.password=REPLACE_ME
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
jwt.secret=${JWT_SECRET:REPLACE_ME}
jwt.expiration=3600000

# Redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0

# Razorpay
razorpay.api.key=${RAZORPAY_KEY_ID:REPLACE_ME}
razorpay.api.secret=${RAZORPAY_SECRET:REPLACE_ME}
razorpay.currency=INR
razorpay.company.name=MyCompany

# SMTP (Gmail example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${SMTP_USERNAME:REPLACE_ME}
spring.mail.password=${SMTP_PASSWORD:REPLACE_ME}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Recommended environment variables:
- JWT_SECRET
- RAZORPAY_KEY_ID, RAZORPAY_SECRET
- SMTP_USERNAME, SMTP_PASSWORD
- DB_USERNAME, DB_PASSWORD (if templating your datasource)

## Secret Management

- __Never commit real secrets__ (API keys, passwords, JWT secrets) into git.
- The live config now reads secrets from environment variables: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `RAZORPAY_KEY_ID`, `RAZORPAY_SECRET`, `SMTP_USERNAME`, `SMTP_PASSWORD`.
- A safe template is provided at `urban-go/src/main/resources/application-example.properties`.

### Option A: Use environment variables (recommended)

Linux/macOS (bash/zsh):
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_db_password
export JWT_SECRET=replace_with_strong_random
export RAZORPAY_KEY_ID=rzp_xxx
export RAZORPAY_SECRET=xxx
export SMTP_USERNAME=your_email@example.com
export SMTP_PASSWORD=your_app_password
```

Windows PowerShell:
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_db_password"
$env:JWT_SECRET="replace_with_strong_random"
$env:RAZORPAY_KEY_ID="rzp_xxx"
$env:RAZORPAY_SECRET="xxx"
$env:SMTP_USERNAME="your_email@example.com"
$env:SMTP_PASSWORD="your_app_password"
```

Run Spring Boot after exporting:
```bash
cd urban-go
mvn spring-boot:run
```

### Option B: Local override file

- Copy the example file and fill values locally:
```bash
cp urban-go/src/main/resources/application-example.properties \
   urban-go/src/main/resources/application-local.properties
```
- Ensure `application-local.properties` is gitignored (we added it in `.gitignore`).
- Activate with a profile if you prefer (e.g., `--spring.profiles.active=local`) or rename to `application.properties` locally but do not commit.

## Installation

1) Backend
- __Create DB__:
  - Create PostgreSQL DB `uber` and a user with privileges.
- __Start Redis__:
  - Ensure Redis is running on `localhost:6379`.
- __Build__:
  - From `urban-go/`: `mvn clean install`
- __Configure__:
  - Update `application.properties` (or use env vars).

2) Frontend
- No build needed. Files are in `Userfrontend/`.
- You can open HTML files directly in a browser or serve via a simple HTTP server (helps with CORS on some setups).

## Running

- __Backend:__
  - From `urban-go/`: `mvn spring-boot:run`
  - Default port: `http://localhost:8080`
  - WebSocket endpoint: `http://localhost:8080/ws`

- __Frontend (Option A: open directly):__
  - Open `Userfrontend/index.html` or `Userfrontend/testing.html` in your browser.
- __Frontend (Option B: serve statically):__
  - Using Python: `python3 -m http.server 5500` (then open http://localhost:5500/Userfrontend/index.html)
  - Or use any static server (e.g., VS Code Live Server).

## API Endpoints (Core)

All secured endpoints require `Authorization: Bearer <JWT>`.

- __Auth__ (`AuthController` at `/api/auth`)
  - POST `/api/auth/login`
  - POST `/api/auth/register/rider`
  - POST `/api/auth/register/driver`

- __Rider__ (`RiderController` at `/api/riders`)
  - GET `/api/riders/` (ADMIN)
  - GET `/api/riders/me`
  - PUT `/api/riders/me` (RIDER)
  - DELETE `/api/riders/me` (RIDER)
  - POST `/api/riders/me/nearby-drivers` (RIDER) — body: `{ "longitude": <num>, "latitude": <num> }`
  - GET `/api/riders/me/rides` (RIDER) — query: `pageNumber`, `pageSize`

- __Driver__ (`DriverController` at `/api/drivers`)
  - GET `/api/drivers/` (ADMIN)
  - GET `/api/drivers/me`
  - PUT `/api/drivers/me` (DRIVER)
  - DELETE `/api/drivers/me` (DRIVER)
  - PUT `/api/drivers/me/availability` (DRIVER) — body: `AvailableDto`
  - GET `/api/drivers/me/rides/pending`
  - GET `/api/drivers/me/rides` — query: `pageNumber`, `pageSize`

- __Rides__ (`RideController` at `/api/rides`)
  - POST `/api/rides/me/requestride` (RIDER) — body: `RideRequestDto`
  - POST `/api/rides/me/{rideId}/accept` (DRIVER)
  - POST `/api/rides/me/{rideId}/start` (DRIVER)
  - POST `/api/rides/me/{rideId}/complete` (DRIVER)
  - POST `/api/rides/me/{rideId}/cancel` (DRIVER or RIDER)
  - POST `/api/rides/me/{rideId}/rate` (DRIVER or RIDER) — body: `RatingDto`
  - POST `/api/rides/me/estimateFair` (RIDER) — body: `RideRequestDto`

- __Payments__ (`RazorPayController` under `/api/riders`)
  - POST `/api/riders/{rideId}/payment/initiate` (RIDER)
  - POST `/api/riders/{rideId}/payment/verify` (RIDER) — body: `PaymentVerificationDto`

- __Email Verification__ (`VerificationController` under `/api/auth`)
  - GET `/api/auth/verify?token=...`

- __WebSocket__ (`DriverLocationController`)
  - Connect: SockJS to `/ws`
  - Send (client -> server): destination `/app/location` with JSON `{ "longitude": <num>, "latitude": <num> }`
  - Receive (server -> clients): topic `/topic/driver/location`

### Example Requests

- Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "user@example.com", "password": "secret" }'
```

- Get rider profile
```bash
curl http://localhost:8080/api/riders/me \
  -H "Authorization: Bearer <JWT>"
```

- Request a ride
```bash
curl -X POST http://localhost:8080/api/rides/me/requestride \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{ "sourceLongitude": 72.8, "sourceLatitude": 19.1, "destinationLongitude": 72.9, "destinationLatitude": 19.2 }'
```

- Initiate payment
```bash
curl -X POST http://localhost:8080/api/riders/123/payment/initiate \
  -H "Authorization: Bearer <JWT>"
```

## Frontend Usage

- __Razorpay test (`Userfrontend/index.html`):__
  - Replace `key`, `order_id`, and amounts with values created via the backend `/payment/initiate`.
  - Click “Pay Now” to open Razorpay modal.

- __WebSocket test (`Userfrontend/testing.html`):__
  - Enter backend base URL (default `http://localhost:8080`) and a valid Driver JWT.
  - Connect, then send coordinates to broadcast to `/topic/driver/location`.

## Screenshots

- Add screenshots in `docs/` and reference them here:
  - `docs/home.png`
  - `docs/websocket.png`
  - `docs/payment.png`

## Development Tips

- Prefer environment variables for secrets.
- Enable SQL logs with `spring.jpa.show-sql=true` (already set).
- If CORS issues arise for the HTML pages, serve them via a local HTTP server instead of file://.

## Contributing

- Fork, create a feature branch, and open a PR.
- Write clear commit messages and add tests where applicable.
- Follow standard Java code style; keep controllers thin and use services.

## License

- Specify your license here (e.g., MIT). Example MIT header:
  - Copyright (c) 2025
  - Permission is hereby granted, free of charge, to any person obtaining a copy of this software...
- Replace this section with your chosen license file and text.

## Contact

- Project Maintainer: [Your Name]
- Email: [your.email@example.com]
- Issues: Please open a GitHub issue in this repository.

---

Updated on: 2025-08-31
