# Shuttle Shout - Badminton Queue Management System

## Language / 语言

- [English](README.md) | [中文](README_ZH.md)

---

## Current Development Status

**Note:** This project is currently under active development. Currently implemented features:

- ✅ User authentication and authorization (registration, login, JWT tokens)
- ✅ Team management (create, update, delete teams, member management)
- ✅ Resource page management and role-based access control

Other features (player management, court management, queue system) are planned for future releases.

---

## Project Overview

Shuttle Shout is a comprehensive badminton queue management system consisting of frontend and backend components. The system is designed to manage players, courts, and queue systems, helping badminton venues efficiently manage their queue and calling processes.

## Tech Stack

### Frontend
- **React 19**
- **Next.js 16**
- **TypeScript**
- **Tailwind CSS**
- **Radix UI**

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Gradle**
- **H2 Database** (Development) / **PostgreSQL** (Production)

## Project Structure

```
shuttle-shout/
├── shuttle-shout-frontend/  # React Frontend Project
│   ├── app/                 # Next.js App Directory
│   ├── components/          # React Components
│   ├── hooks/              # Custom Hooks
│   └── lib/                # Utility Libraries
│
└── src/                    # Spring Boot Backend Project
    ├── main/java/com/shuttleshout/
    │   ├── controller/     # REST API Controllers
    │   ├── service/        # Business Logic Layer
    │   ├── repository/     # Data Access Layer
    │   └── model/          # Entity Classes and DTOs
    └── resources/          # Configuration Files
```

## Quick Start

### Prerequisites
- Node.js 18+ and npm/pnpm
- JDK 17+
- Gradle 8.5+ (or use the project's Gradle Wrapper)

### Start Frontend

```bash
cd shuttle-shout-frontend
npm install  # or pnpm install
npm run dev  # or pnpm dev
```

Frontend will run at http://localhost:3000

### Start Backend

```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

Backend will run at http://localhost:8080/api

### API Documentation

After starting the backend, access the following URLs to view API documentation:
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API Docs: http://localhost:8080/api/api-docs

## Features

### Player Management
- ✅ Create and manage player information
- ✅ Search players
- ✅ Update player information

### Court Management
- ✅ Create and manage courts
- ✅ Enable/disable courts
- ✅ View court status

### Queue System
- ✅ Join waiting queue
- ✅ Calling functionality
- ✅ Complete service
- ✅ Cancel queue
- ✅ View waiting list

### User Authentication & Authorization
- ✅ User registration and login
- ✅ Role-based access control (RBAC)
- ✅ JWT token authentication
- ✅ Resource page management

### Team Management
- ✅ Create and manage teams
- ✅ Team member management
- ✅ Team level management

## UI Demo

Below are screenshots of the main features of the system:

### Login Page
![Login Page](image/login.png)

### Personal Page
![Personal Page](image/personal-page.png)

### Team Overview
![Team Overview](image/team-overview.png)

### Create Team
![Create Team](image/create-team.png)

### Edit Team
![Edit Team](image/edit-team.png)

### Team Members Management
![Team Members Management](image/team-members.png)

### Anonymous Team Overview
![Anonymous Team Overview](image/anosy-team-overview.png)

## Development Roadmap

- [ ] WebSocket real-time notifications
- [ ] Queue history records
- [ ] Statistics and reporting features
- [x] User authentication and authorization
- [ ] Multi-language support

## License

MIT License
