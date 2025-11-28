# VisualDB - Web-Based Database Management System

A full-stack database management platform that allows users to create, manage, and visualize PostgreSQL databases through an intuitive web interface. Built as a university project to demonstrate modern web application architecture and secure database operations.

## ⚠️ Note: Academic Project

This project was developed as a university assignment to demonstrate core concepts. 
For production use, the following improvements would be necessary:

**Security Enhancements:**
- Implement password hashing (bcrypt/Argon2)
- Move secrets to environment variables
- Add rate limiting and input sanitization
- Implement HTTPS-only communication

**Code Quality:**
- Add comprehensive unit tests
- Implement proper error handling with custom exceptions
- Extract magic strings to constants
- Add API documentation
- Implement proper logging framework

## Features

- **Multi-User Authentication System**: Secure JWT-based authentication with role-based access control (admin/user privileges)
- **Dynamic Database Management**: Create, rename, and delete databases with full CRUD operations
- **Interactive Table Management**: 
  - Create tables with custom schemas
  - Add/remove columns dynamically with data type validation
  - Real-time data editing with transaction support
- **Connection Pooling**: HikariCP implementation for optimized concurrent database access
- **RESTful API Architecture**: Clean separation between frontend and backend services
- **Audit Logging**: Track all database and table operations with timestamps

## Tech Stack

**Backend:**
- Java 11 with Jakarta EE
- PostgreSQL database
- HikariCP (connection pooling)
- JWT (Auth0 library) for authentication
- Gson for JSON processing
- Maven for dependency management

**Frontend:**
- React 18
- TypeScript
- Modern REST API integration

**Security:**
- JWT token-based authentication
- Role-based access control
- SQL injection prevention with PreparedStatements
- CORS configuration for secure cross-origin requests

## API Endpoints

### Authentication
- `POST /login` - User authentication
- `POST /logout` - Session termination
- `POST /register` - New user registration

### Database Management
- `GET /database/search` - List user's databases
- `POST /database/new` - Create new database
- `POST /database/rename` - Rename existing database
- `DELETE /database/delete` - Remove database
- `POST /database/returntable` - Get all tables in a database

### Table Management
- `POST /table/search` - Retrieve table data
- `POST /table/create` - Create new table
- `POST /table/addcolumn` - Add column to existing table
- `POST /table/save` - Save table modifications
- `POST /table/rename` - Rename table
- `DELETE /table/delete` - Remove table

### Admin Operations
- `GET /admin/infoUser` - View all users and their database statistics (admin only)

## Security Features

- **JWT Authentication**: Stateless authentication with token verification on every request
- **Authorization Filter**: Custom servlet filter ensuring only authenticated users access protected resources
- **SQL Injection Prevention**: All queries use PreparedStatements with parameterized inputs
- **Password Security**: Secure credential validation (ready for hash implementation)
- **CORS Protection**: Configured headers for secure cross-origin requests

## Architecture Highlights

- **Connection Pooling**: HikariCP manages database connections efficiently, supporting up to 25 concurrent connections
- **Transaction Management**: Proper commit/rollback handling for data integrity
- **Dynamic Schema Management**: Metadata-driven approach for flexible table structures
- **Type Safety**: Runtime type validation for database operations (INTEGER, FLOAT, TEXT, BOOLEAN, NUMERIC)
- **Session Management**: Token-based system with automatic cleanup on logout

## Database Schema

The system uses a PostgreSQL database with the following main tables:
- `Utenti` - User accounts with privileges
- `Database` - User-created databases metadata
- `Table` - Table definitions and relationships
- `OperazioneDatabase` - Audit log for database operations
- `OperazioneTable` - Audit log for table operations
- `Accesso` - User login history

## Learning Outcomes

This project demonstrates:
- Enterprise Java development patterns
- RESTful API design and implementation
- Database connection management and optimization
- Authentication and authorization systems
- Transaction handling and data integrity
- Modern web application architecture

## Setup & Installation

1. Clone the repository
2. Configure PostgreSQL connection in `PoolingPersistenceManager.java`
3. Create the VisualDB schema and table
4. Build with Maven: `mvn clean install`
5. Deploy the WAR file to a Jakarta EE compatible server (e.g., Apache Tomcat 10+)

## Configuration

Update database credentials in `PoolingPersistenceManager.java`:
```java
config.setJdbcUrl("jdbc:postgresql://localhost:5432/VisualDB?currentSchema=public");
config.setUsername("your_username");
config.setPassword("your_password");
```

## Author

**Jean Roland Fabrizio Agbonson**  
Computer Science Student - University of Turin

---

*This project was developed as part of university coursework to demonstrate full-stack web development capabilities and modern database management techniques.*


