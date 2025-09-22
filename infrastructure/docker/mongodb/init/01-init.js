// Инициализация MongoDB для LMS
db = db.getSiblingDB('lms_mongo_db');

// Создание пользователя для приложения
db.createUser({
  user: 'lms_app_user',
  pwd: 'lms_app_password',
  roles: [
    {
      role: 'readWrite',
      db: 'lms_mongo_db'
    }
  ]
});

// Создание коллекций с валидацией
db.createCollection('courses', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['title', 'description', 'created_at'],
      properties: {
        title: {
          bsonType: 'string',
          description: 'Course title is required'
        },
        description: {
          bsonType: 'string',
          description: 'Course description is required'
        },
        created_at: {
          bsonType: 'date',
          description: 'Creation date is required'
        }
      }
    }
  }
});

db.createCollection('lessons', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['course_id', 'title', 'content'],
      properties: {
        course_id: {
          bsonType: 'objectId',
          description: 'Course ID is required'
        },
        title: {
          bsonType: 'string',
          description: 'Lesson title is required'
        },
        content: {
          bsonType: 'string',
          description: 'Lesson content is required'
        }
      }
    }
  }
});

// Создание индексов
db.courses.createIndex({ "title": 1 });
db.courses.createIndex({ "created_at": -1 });
db.lessons.createIndex({ "course_id": 1 });

print('LMS MongoDB initialization completed');
