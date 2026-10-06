# StudyHub

Минимальный REST API для управления учебными курсами на Java 21 и Spring Boot.

## Выполненные требования

- REST: CRUD курсов, добавление/изменение/удаление уроков, просмотр/создание/удаление тегов.
- Request/response DTO и Bean Validation.
- JWT-аутентификация и роли `USER`, `ADMIN`.
- Единая обработка ошибок через `@RestControllerAdvice`.
- PostgreSQL и 6 миграций Flyway.
- Swagger/OpenAPI с JWT Bearer-схемой.
- Устранение N+1 через `@EntityGraph`.
- 2 unit-теста и 3 MockMvc-теста.
- Docker Compose и Postman collection.

## Запуск

Нужны Docker Desktop и свободные порты `8080`, `5432`.

```bash
docker compose up --build -d
docker compose ps
```

После запуска доступны:

- Сайт: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- API: <http://localhost:8080/api/courses>

Остановка:

```bash
docker compose down
```

## JWT и роли

Публичные запросы:

- `POST /api/auth/register`
- `POST /api/auth/login`

Остальные запросы требуют заголовок:

```text
Authorization: Bearer <token>
```

Регистрация создаёт пользователя с ролью `USER`. Создание и удаление тегов разрешено только роли `ADMIN`. Для проверки роли зарегистрируйте `student@example.com`, затем выполните:

```bash
docker compose exec postgres psql -U postgres -d study_hub -c "UPDATE users SET role='ADMIN' WHERE email='student@example.com';"
```

После смены роли снова выполните вход, чтобы получить новый JWT.

## Flyway

Миграции находятся в `src/main/resources/db/migration`:

1. `V1__create_users.sql`
2. `V2__create_courses.sql`
3. `V3__create_lessons.sql`
4. `V4__create_tags.sql`
5. `V5__create_course_tags.sql`
6. `V6__add_catalog_indexes.sql`

`spring.jpa.hibernate.ddl-auto=validate`: Hibernate проверяет схему, а создаёт её Flyway.

## Устранение N+1

`GET /api/courses/{id}` возвращает курс вместе с автором, уроками и тегами. Без специальной загрузки Hibernate мог бы отдельно запрашивать каждую ленивую связь. Метод `CourseRepository.findDetailedById` использует:

```java
@EntityGraph(attributePaths = {"author", "lessons", "tags"})
```

Связанные данные загружаются в рамках одного обращения к репозиторию, поэтому дополнительные запросы для каждой связи не выполняются.

## Тесты

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS:

```bash
./mvnw clean verify
```

`CourseServiceTest` содержит 2 unit-теста. `AuthControllerTest` содержит 2 MockMvc-теста, `SecurityAccessTest` — 1 MockMvc-тест безопасности.

## Postman

Импортируйте `postman/StudyHub.postman_collection.json` и выполните запросы по порядку:

1. `Register`
2. `Login`
3. `List courses`
4. `Create course`

После регистрации или входа JWT автоматически сохраняется в переменную коллекции `token`.
