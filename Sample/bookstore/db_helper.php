<?php
// Database connection helper for MySQLi
// Get database credentials from environment variables or use defaults
function getDBConnection() {
    $servername = getenv('DB_HOST') ?: 'mysql';
    $database = getenv('DB_NAME') ?: 'bookstore';
    $username = getenv('DB_USER') ?: 'bookstore_user';
    $password = getenv('DB_PASSWORD') ?: 'bookstore_pass_123';

    $conn = new mysqli($servername, $username, $password, $database);

    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }

    return $conn;
}
?>