# User Administration Application

This is a Spring Boot-based REST APP for managing user data, including authentication, JWT authorization, and logging. The project includes integration with a MySQL database and is ready to run locally using Docker.
## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Makefile Commands](#makefile-commands)

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

    Add the following configuration parameters in your application.properties file for database and mail settings:
    #### Database configuration
   (Changes to this are needed only if you want to start app locally, this works for docker!!!)
    
    spring.datasource.url=jdbc:mysql://localhost:3306/users_db(db name)
    spring.datasource.username=root (db username)

    spring.datasource.password=db-user-password
    #### Mail configuration
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

8. ### **Makefile Commands**
   The Makefile contains a set of commands to manage Docker containers and the Spring Boot application. Here are the available commands:
  
    #### **Start containers:**
    ```bash
    make up
    ```
   
    Builds and starts Docker containers in detached mode. Use this to run the application along with the MySQL database.

   ### **Recreates images and starts containers**
   ```bash
   make build-and-up
   ```
   
   Removes all images and recreates them. After that starts containers with them 
    #### **Stop containers:**
    ```bash
   make down
    ```
   Stops and removes all running containers.

    #### **Restart containers:**
    ```bash
    make restart
   ```

    Stops and restarts the containers.

    #### **Clean up Docker resources:**
    ```bash
    make clean
    ```

   Removes unused Docker images, containers, and volumes.

    #### **Run tests:**
    ```bash
    make test
   ```

   Runs unit tests for the application.

    #### **Backup database:**
    ```bash
    make backup
    ```

   Creates a backup of the current database in the backups directory with a timestamped filename.

    #### **Restore database from backup:**
    ```bash
    make restore
    ```

   Restores the database from the latest backup file in the backups directory.

    #### **Access the Spring Boot container:**
    ```bash
    make java
    ```

   Opens an interactive shell in the Spring Boot container for executing CLI commands.
