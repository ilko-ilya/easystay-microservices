# EasyStay! 🏡✨

Welcome to **EasyStay** – our microservices-based booking platform that simplifies the search for rental accommodations.
With **EasyStay**, you can easily find and book housing while ensuring a smooth payment and notification process.

## 🌟 Features

- **User authentication and authorization** (Spring Security, JWT)
- **Accommodation management** (listings, availability, and pricing)
- **Booking system** (create, update, cancel bookings)
- **Payment processing** (Stripe API integration)
- **Notifications** (RabbitMQ, Telegram API, email, and SMS)
- **API Gateway & Service Discovery** (Spring Cloud Gateway, Eureka)
- **Caching & Performance** (Redis, database indexing)
- **Scalable & containerized architecture** (Docker, Docker Compose)

---

## 🚀 **Getting Started**
Follow these simple steps to set up and run the project locally.

### 📌 **1. Prerequisites**
Make sure you have installed:
- Java 17
- Maven
- Docker & Docker Compose
- PostgreSQL (optional, as the database runs in Docker)

### 📌 **2. Clone the Repository**
- git clone https://github.com/ilko-ilya/easystay-microservices

### 3️⃣ Build and Run Docker Containers
```bash
docker-compose build
docker-compose up -d
```

### 4️⃣ Access the Application
Once running, the EasyStay app will be available at:
- **API Gateway**: [http://localhost:8222](http://localhost:8222)
- **Swagger Docs**: [http://localhost:8222/swagger-ui.html](http://localhost:8222/swagger-ui.html)
- **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)

## 📌 API Endpoints

### 🔐 Authentication (Public Endpoints)
#### Register
```http
POST /api/auth/register
```
**Request Body:**
```json
{
  "email": "john.doe@gmail.com",
  "password": "password123",
  "repeatPassword": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
```
**Request Body:**
```json
{
  "email": "john.doe@gmail.com",
  "password": "password123"
}
```

### 🏡 Accommodation Service
#### View Available Listings
```http
GET /api/accommodations
```
#### Get Accommodation by ID
```http
GET /api/accommodations/{id}
```
#### Create a New Accommodation (MANAGER only)
```http
POST /api/accommodations
```
#### Update an Accommodation (MANAGER only)
```http
PUT /api/accommodations/{id}
```
#### Delete an Accommodation (MANAGER only)
```http
DELETE /api/accommodations/{id}
```

### 📅 Booking Service
#### Create a Booking (CUSTOMER only)
```http
POST /api/bookings
```
**Request Body:**
```json
{
  "accommodationId": 123,
  "checkInDate": "2024-04-01",
  "checkOutDate": "2024-04-10"
}
```
#### View Your Bookings
```http
GET /api/bookings/my
```
#### View Booking by ID
```http
GET /api/bookings/{id}
```
#### Cancel Booking
```http
DELETE /api/bookings/{id}
```

### 💳 Payment Service
#### View Payments
```http
GET /api/payments/my
```

### ✉️ Notification Service
#### Send a Test Notification
```http
POST /api/notifications/send
```

## ⚙️ Technologies Used
- **Backend:** Java 17, Spring Boot 3, Spring Security, Spring Data JPA
- **API & Communication:** REST, Feign Client, OpenAPI (Swagger)
- **Database:** PostgreSQL, Liquibase
- **Infrastructure:** Docker, Eureka Service Discovery, Config Server, RabbitMQ
- **Payments:** Stripe API
- **Messaging & Notifications:** Telegram API, RabbitMQ
- **Logging & Tracing:** Zipkin, Spring Actuator

## 🎯 Future Enhancements
- Implement Admin dashboard for better management.
- Add support for multiple payment providers.
- Enhance security measures and OAuth authentication.

## 📞 Contact & Support
If you have any questions, feel free to reach out:
- Email: support@easystay.com
- Phone: +123 456 7890

Thank you for choosing EasyStay! 🚀🏡✨
