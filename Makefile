# Makefile for Docker commands

# Start Docker containers if images already exists
up:
	docker-compose up -d

# Recreates images and starts containers
build-and-up:
	docker-compose up --build -d

# Stop and remove Docker containers
down:
	docker-compose down

# Restart Docker containers
restart:
	docker-compose down && docker-compose up -d

# Clean up unused Docker images, containers, and volumes
clean:
	docker system prune -f && docker volume prune -f

# Run tests
test:
	mvn test

# Run Spring Boot application locally (without Docker)
run:
	mvn spring-boot:run

BACKUP_DIR=backups
BACKUP_FILE=$(BACKUP_DIR)/db_backup_$(shell date +%Y%m%d%H%M%S).sql
TEMP_DUMP_FILE=/tmp/db_backup.sql

# Ensure the backups directory exists and create the database dump
backup:
	@mkdir -p $(BACKUP_DIR)  # Create the backups directory if it doesn't exist
	@echo "Checking if MySQL container is running..."
	@docker inspect -f '{{.State.Running}}' mysql_db || { echo "MySQL container is not running."; exit 1; }
	@echo "Creating database backup..."
	@docker exec mysql_db bash -c "mysqldump -u root --password=password test > $(TEMP_DUMP_FILE)" 2>&1 || { echo "Failed to create backup in container"; exit 1; }
	@docker cp mysql_db:$(TEMP_DUMP_FILE) $(BACKUP_FILE) || { echo "Failed to copy backup to host"; exit 1; }
	@echo "Backup created successfully at $(BACKUP_FILE)"
	@docker exec mysql_db rm -f $(TEMP_DUMP_FILE)  # Clean up temporary dump file

# Restore database from a dump file
restore:
	@echo "Checking if MySQL container is running..."
	@docker inspect -f '{{.State.Running}}' mysql_db || { echo "MySQL container is not running."; exit 1; }
	@echo "Restoring database from $(BACKUP_FILE)..."
	@docker exec -i mysql_db mysql -u root --password=password test < $(BACKUP_FILE) || { echo "Failed to restore database from backup"; exit 1; }
	@echo "Database restored successfully."

# Access the Spring Boot application container
java:
	@echo "Accessing the Spring Boot application container..."
	@docker exec -it spring_app bash || { echo "Failed to access the Spring Boot container"; exit 1; }