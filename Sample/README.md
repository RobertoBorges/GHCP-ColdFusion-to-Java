# PHP Basic BookStore Website (For Study Purpose Only)
This BookStore Website is using PHP and Database(MySQL). In this website you can Register and Edit Profile.
And also all the book data will store at the database for easy to add, edit and delete.

## Home Page & Edit Profile Page:
![HomePage](/homepage.PNG)
![EditProfile](/editprofile.PNG)

## DataBase:
![Database](/db.PNG)

## Getting Started with Docker

This application now runs in Docker containers for easy setup and consistent development environment.

### Prerequisites

- **Docker Desktop** installed on your system
  - Windows: [Download Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
  - Mac: [Download Docker Desktop for Mac](https://www.docker.com/products/docker-desktop)
  - Linux: Install Docker and Docker Compose via your package manager

### Quick Start

1. **Clone or download this repository**

2. **Start the application** - Open a terminal in the project root directory and run:
   ```bash
   docker-compose up -d
   ```
   This will:
   - Download the necessary Docker images (first time only)
   - Create and start PHP web server, MySQL database, and PHPMyAdmin containers
   - Automatically import the database schema and sample data

3. **Access the application**:
   - **Bookstore Website**: http://localhost:8080
   - **PHPMyAdmin** (Database Management): http://localhost:8081
     - Server: `mysql`
     - Username: `root`
     - Password: `root_password_123`

4. **Stop the application**:
   ```bash
   docker-compose down
   ```

### Container Services

- **Web Server**: PHP 7.4 with Apache (Port 8080)
- **Database**: MySQL 8.0 (Port 3306)
- **PHPMyAdmin**: Database management interface (Port 8081)

For more detailed Docker commands and configuration, see [README-DOCKER.md](README-DOCKER.md).

## Alternative Setup (XAMPP)

If you prefer not to use Docker, you can still run this application with [XAMPP](https://www.apachefriends.org/index.html):
1. Download the [bookstore](bookstore) folder
2. Place it in your XAMPP `htdocs` directory
3. Import [database.sql](bookstore/database.sql) to your MySQL server via PHPMyAdmin
 
