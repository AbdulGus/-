# StudyHub Minimal

Учебный REST API для управления курсами.
## Что реализовано

- REST CRUD для курсов, уроков и тегов;
- request/response DTO — JPA Entity не выходят из контроллеров;
- регистрация и вход по JWT;
- роли `USER` и `ADMIN`, проверка через `@PreAuthorize`;
- Bean Validation и единый формат ошибок через `@ControllerAdvice`;
- PostgreSQL и 6 Flyway-миграций;
- Swagger/OpenAPI с JWT Bearer-схемой;
- устранение одного случая N+1 через `@EntityGraph`;
- 2 unit-теста сервиса и MockMvc-тесты контроллеров/безопасности;
- Docker Compose, README и Postman collection.

В проекте нет Redis, очередей, фоновых задач и frontend — они не требуются по выбранным критериям.

## Стек

Java 21, Spring Boot 4, Spring Web MVC, Spring Data JPA, Spring Security, PostgreSQL, Flyway, JWT, Swagger, JUnit 5, Mockito, MockMvc, Docker.

## Запуск через Docker

Нужен запущенный Docker Desktop.

```bash
docker compose up --build -d
```

Проверка контейнеров:

```bash
docker compose ps
```

После запуска:

- API: <http://localhost:8080/api/courses>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- PostgreSQL: `localhost:5432`, база `study_hub`, логин и пароль `postgres`

Остановка без удаления данных:

```bash
docker compose down
```

## JWT и роли

Публичные endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`

Остальные endpoints требуют заголовок:

```text
Authorization: Bearer <JWT_TOKEN>
```

Новый пользователь получает роль `USER`. Только `ADMIN` может создавать и удалять теги. Пользователь может изменять только свои курсы, а администратор — любые.

Для проверки роли администратора можно изменить роль зарегистрированного пользователя в локальной БД:

```bash
docker compose exec postgres psql -U postgres -d study_hub -c "UPDATE users SET role='ADMIN' WHERE email='student@example.com';"
```

После изменения роли нужно войти заново и получить новый JWT.

## Основные endpoints

| Метод | URL | Доступ |
|---|---|---|
| POST | `/api/auth/register` | публичный |
| POST | `/api/auth/login` | публичный |
| GET | `/api/courses` | USER, ADMIN |
| GET | `/api/courses/{id}` | USER, ADMIN |
| POST | `/api/courses` | USER, ADMIN |
| PUT/DELETE | `/api/courses/{id}` | автор, ADMIN |
| POST/PUT/DELETE | `/api/courses/{courseId}/lessons/**` | автор, ADMIN |
| GET | `/api/tags` | USER, ADMIN |
| POST/DELETE | `/api/tags/**` | ADMIN |

Тела запросов и ответов удобно смотреть и выполнять в Swagger.

## Flyway

Миграции находятся в `src/main/resources/db/migration`:

1. создание пользователей;
2. создание курсов;
3. создание уроков;
4. создание тегов;
5. таблица связи курсов и тегов;
6. индексы каталога.

Hibernate использует `ddl-auto: validate`, поэтому схему создаёт и изменяет только Flyway.

## Устранение N+1

При загрузке подробного курса нужны автор, уроки и теги. Обычная ленивая загрузка могла выполнить отдельные SQL-запросы для каждой связи.

Метод `CourseRepository.findDetailedById` использует:

```java
@EntityGraph(attributePaths = {"author", "lessons", "tags"})
```

Поэтому Hibernate загружает карточку курса и нужные связи одним запросом вместо нескольких дополнительных запросов. Этот метод используется в `GET /api/courses/{id}`.

## Тесты

Windows:

```powershell
.\mvnw.cmd verify
```

Linux/macOS:

```bash
./mvnw verify
```

В проекте есть два unit-сценария `CourseServiceTest`, MockMvc-проверки регистрации/валидации и отдельная MockMvc-проверка доступа без JWT.

## Postman

Импортируйте файл `postman/StudyHub.postman_collection.json`. Запрос регистрации или входа автоматически сохраняет JWT в переменную коллекции `token`.
