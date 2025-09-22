-- Инициализация базы данных LMS
-- Создание дополнительных схем если нужно

CREATE SCHEMA IF NOT EXISTS lms_core;
CREATE SCHEMA IF NOT EXISTS lms_users;
CREATE SCHEMA IF NOT EXISTS lms_content;

-- Установка прав доступа
GRANT ALL PRIVILEGES ON SCHEMA lms_core TO lms_user;
GRANT ALL PRIVILEGES ON SCHEMA lms_users TO lms_user;
GRANT ALL PRIVILEGES ON SCHEMA lms_content TO lms_user;

-- Создание расширений
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Комментарий
COMMENT ON DATABASE lms_db IS 'Learning Management System Database';
