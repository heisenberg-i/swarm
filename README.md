# PURPOSE: SWARM project file: README.md
# BIOPOWER AI-IoT Plant Health Monitoring System

Production-ready web application for monitoring Biogas, Bio-CNG, Sanitation, Organic Waste Treatment, and Waste-to-Energy plants.

## Features

- **Live IoT Monitoring** – Real-time sensor data from ESP32, Raspberry Pi, PLC, MQTT Gateway
- **AI Plant Health Engine** – Detects acidification, overfeeding, gas yield reduction; generates recommendations
- **Alert Management** – Threshold-based alerts with email/SMS/WhatsApp ready notifications
- **Predictive Maintenance** – Equipment failure prediction with remaining useful life
- **Historical Analytics** – Interactive charts with hour/day/week/month filters
- **Multi-Plant Management** – Centralized dashboard for multiple sites
- **Role-Based Access** – Super Admin, Plant Admin, Operator roles

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Material UI, Recharts, Axios |
| Backend | Spring Boot 3, Java 17, JWT, Maven |
| Database | PostgreSQL 16 + TimescaleDB |
| IoT | MQTT, REST API |
| Deployment | Docker, Nginx |

## Quick Start (Docker)

```bash
cp .env.example .env
docker compose up -d --build
```

Details: [docs/DOCKER.md](docs/DOCKER.md).

Access the application at **http://localhost**

### First Control Center admin

There are **no default passwords**. On a new primary deploy, set `SWARM_BOOTSTRAP_ADMIN_EMAIL` and `SWARM_BOOTSTRAP_ADMIN_PASSWORD` (12+ characters) for one-time Control Center admin creation. Existing documented defaults are compromised and are not seeded.

## Local Development

### Backend

```bash
cd backend
# Windows (no global Maven required)
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

API runs at `http://localhost:8081/api` with `SPRING_PROFILES_ACTIVE=dev`.
Set `DB_PASSWORD` (and optionally `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`) before starting.
The application database is PostgreSQL 16 + TimescaleDB (`swarm` on port 5433). MySQL and H2 are not used.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:3000`

## IoT Data Ingestion

### REST API

```bash
POST /api/iot/data
Content-Type: application/json

{
  "plantId": 1,
  "nodeId": 1,
  "sensorType": "PH",
  "value": 7.2,
  "timestamp": "2026-06-20T10:30:00"
}
```

### MQTT

Publish to topic `biopower/sensors/#`:

```json
{
  "plantId": 1,
  "nodeId": 1,
  "sensorType": "TEMPERATURE",
  "value": 38.5,
  "timestamp": "2026-06-20T10:30:00"
}
```

## Project Structure

```
swarm_webapp/
├── backend/          # Spring Boot REST API
├── frontend/         # React dashboard
├── database/         # MySQL schema
├── docker/           # MQTT & deployment configs
├── docs/             # Documentation
└── docker-compose.yml
```

## Documentation

- [Installation Guide](docs/INSTALLATION.md)
- [API Documentation](docs/API.md)
- [Partner API (emPOWER Integration)](docs/PARTNER_API.md)
- [Centralized Auth (emPOWER SaaS SSO)](docs/EMPOWER_CENTRALIZED_AUTH.md)
- [Plant HMI (P&ID)](docs/PLANT_HMI.md)
- [Shared Login Connectivity (emPOWER)](docs/EMPOWER_SHARED_LOGIN_CONNECTIVITY.md)
- [User Manual](docs/USER_MANUAL.md)
- [ESP Sensor Hub Connection Manual](docs/ESP_SENSOR_HUB_MANUAL.md)
- [Raspberry Pi Deployment](docs/RASPBERRY_PI_DEPLOYMENT.md)
- [Database ER Diagram](docs/ER_DIAGRAM.md)

## License

Proprietary – BIOPOWER Plant Health Monitoring System
