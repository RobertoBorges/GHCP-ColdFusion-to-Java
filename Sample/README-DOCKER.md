# Docker Development Environment Setup

This guide will help you run the PHP Bookstore application locally using Docker containers.

## Prerequisites

- **Docker Desktop** installed on your system
  - Windows: [Download Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
  - Mac: [Download Docker Desktop for Mac](https://www.docker.com/products/docker-desktop)
  - Linux: Install Docker and Docker Compose via your package manager

## Quick Start

### 1. Start the Application

Open a terminal in the project root directory and run:

```bash
docker-compose up -d
```

This command will:
- Download the necessary Docker images (first time only)
- Create and start three containers: PHP web server, MySQL database, and PHPMyAdmin
- Automatically import the database schema and sample data
- Set up networking between containers

### 2. Access the Application

Once the containers are running, you can access:

- **Bookstore Website**: http://localhost:8080
- **PHPMyAdmin** (Database Management): http://localhost:8081
  - Server: `mysql`
  - Username: `root`
  - Password: `root_password_123`

### 3. Stop the Application

To stop all containers:

```bash
docker-compose down
```

To stop and **remove all data** (including database):

```bash
docker-compose down -v
```

## Container Details

### Services

1. **web** - PHP 7.4 with Apache
   - Port: 8080
   - Document root: `./bookstore` directory
   - Extensions: mysqli, pdo, pdo_mysql

2. **mysql** - MySQL 8.0
   - Port: 3306
   - Database: `bookstore`
   - Root password: `root_password_123`
   - User: `bookstore_user`
   - Password: `bookstore_pass_123`

3. **phpmyadmin** - PHPMyAdmin (latest)
   - Port: 8081
   - Connected to MySQL container

## Useful Docker Commands

### View Running Containers
```bash
docker-compose ps
```

### View Container Logs
```bash
# All containers
docker-compose logs

# Specific container
docker-compose logs web
docker-compose logs mysql
docker-compose logs phpmyadmin

# Follow logs in real-time
docker-compose logs -f
```

### Restart Containers
```bash
# Restart all
docker-compose restart

# Restart specific container
docker-compose restart web
```

### Execute Commands Inside Containers
```bash
# Access web container bash
docker-compose exec web bash

# Access MySQL CLI
docker-compose exec mysql mysql -u root -p
# Password: root_password_123
```

### Rebuild Containers (after configuration changes)
```bash
docker-compose up -d --build
```

## Database Management

### Using PHPMyAdmin
1. Go to http://localhost:8081
2. Login with root credentials
3. Select `bookstore` database from the left sidebar

### Using MySQL CLI
```bash
docker-compose exec mysql mysql -u bookstore_user -p bookstore
# Password: bookstore_pass_123
```

### Reset Database
If you need to reset the database to its initial state:

```bash
# Stop containers and remove volumes
docker-compose down -v

# Start again (will reimport database.sql)
docker-compose up -d
```

## Troubleshooting

### Port Already in Use
If you get an error that port 8080, 8081, or 3306 is already in use:

1. **Option 1**: Stop the conflicting application
2. **Option 2**: Change ports in `docker-compose.yml`:
   ```yaml
   ports:
     - "9090:80"  # Change 8080 to 9090 (or any free port)
   ```

### Container Won't Start
Check the logs:
```bash
docker-compose logs
```

### Database Connection Issues
1. Ensure all containers are running: `docker-compose ps`
2. Check MySQL is ready: `docker-compose logs mysql`
3. Verify environment variables in `.env` file

### Permission Issues (Linux)
If you encounter permission issues:
```bash
sudo chown -R $USER:$USER bookstore/
```

### Clear Everything and Start Fresh
```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove all images (optional)
docker-compose down --rmi all -v

# Start fresh
docker-compose up -d
```

## Development Workflow

1. **Code Changes**: Edit PHP files in the `bookstore/` directory
2. **See Changes**: Refresh your browser at http://localhost:8080
3. **Database Changes**: Use PHPMyAdmin or MySQL CLI
4. **Logs**: Monitor with `docker-compose logs -f web`

## Environment Variables

Configuration is stored in `.env` file:

```env
MYSQL_ROOT_PASSWORD=root_password_123
MYSQL_DATABASE=bookstore
MYSQL_USER=bookstore_user
MYSQL_PASSWORD=bookstore_pass_123
```

**Important**: Never commit real passwords to version control. The `.env` file should be added to `.gitignore` for production projects.

## Next Steps

Now that your development environment is running, you can:

1. Register a new user at http://localhost:8080
2. Browse the Book catalog
3. Test the shopping Cart functionality
4. Begin addressing the security and code quality issues identified in the analysis

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PHP Docker Official Image](https://hub.docker.com/_/php)
- [MySQL Docker Official Image](https://hub.docker.com/_/mysql)
