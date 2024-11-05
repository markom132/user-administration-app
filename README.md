# User Administration Application

This is a Spring Boot-based REST APP for managing user data, including authentication, JWT authorization, and logging. The project includes integration with a MySQL database and is ready to run locally using Docker.
## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [API Documentation](#api-documentation)

---

### Prerequisites

- **Java 17** - Ensure Java 17 is installed.
- **Docker** - For containerized setup with MySQL.
- **Maven** - For dependency management.

### Project Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/user-administration-app.git
   cd user-administration-app


2. **Install dependencies**:
    ```bash
   mvn clean install

3. **Docker setup (optional but recommended)**:

    To start the application and MySQL database with Docker, use docker-compose:
    ```bash
    docker-compose up
   ```
    To build Docker image:
    ```bash
   dokcker-compose up -build

4. ### **Configuration**

    Add the following configuration parameters in your application.properties file for database, mail, and JWT settings:
    # Database configuration
    spring.datasource.url=jdbc:mysql://localhost:3306/test(db name)
    spring.datasource.username=db-user-username
    spring.datasource.password=db-user-password
    # Mail configuration
    spring.mail.username=u8471084@gmail.com (email sender)
    spring.mail.password=email-sender-password

5. ### **Running the Application**

   **Using Docker**:
If you're using Docker, the application should be running on http://localhost:8080.

    **Using Maven**: 
Alternatively, start it directly with Maven:

    ```bash
    mvn spring-boot:run
    ```
   
6. ### **Testing**
    Run unit tests to ensure the service layer logic and controllers are functioning as expected:
    ```bash
    mvn test
    ```

7. ### **API Documentation**
After starting the application, access the API documentation (Swagger) at:
    ```bash
    http://localhost:8080/docs
    ```