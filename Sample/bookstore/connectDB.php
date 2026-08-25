<?php
// Get database credentials from environment variables or use defaults for local development
$db_host = getenv('DB_HOST') ?: 'mysql';
$db_name = getenv('DB_NAME') ?: 'bookstore';
$db_user = getenv('DB_USER') ?: 'bookstore_user';
$db_pass = getenv('DB_PASSWORD') ?: 'bookstore_pass_123';

$pdo = new PDO("mysql:host=$db_host;port=3306;dbname=$db_name", $db_user, $db_pass);
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
?>