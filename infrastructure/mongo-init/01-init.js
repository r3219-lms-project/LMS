// Инициализация пользователя для LMS приложения
db = db.getSiblingDB('admin');

// Создаем пользователя для приложения
db.createUser({
  user: 'lms_app_user',
  pwd: 'lms_app_password',
  roles: [
    {
      role: 'readWrite',
      db: 'lms_mongo_db'
    },
    {
      role: 'dbAdmin',
      db: 'lms_mongo_db'
    }
  ]
});

// Переключаемся на базу данных приложения
db = db.getSiblingDB('lms_mongo_db');

// Создаем начальные коллекции
db.createCollection('users', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['username', 'email'],
      properties: {
        username: {
          bsonType: 'string',
          description: 'Username is required'
        },
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$',
          description: 'Valid email is required'
        }
      }
    }
  }
});

db.createCollection('courses');
db.createCollection('lessons');

// Создаем индексы
db.users.createIndex({ 'email': 1 }, { unique: true });
db.users.createIndex({ 'username': 1 }, { unique: true });
db.courses.createIndex({ 'title': 1 });
db.lessons.createIndex({ 'courseId': 1 });

print('MongoDB initialization completed successfully');
