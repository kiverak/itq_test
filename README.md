# itq_test

## Запуск через Docker

### Предварительные требования
- Docker
- Docker Compose

### Запуск приложения

```bash
docker compose up --build
```

Приложение будет доступно по адресу `http://localhost:8080`

**Сервисы:**
- **Приложение (ITQ):** http://localhost:8080
- **PostgreSQL:** localhost:5432

**Переменные окружения:**
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://postgres:5432/itq
- `SPRING_DATASOURCE_USERNAME`: postgres
- `SPRING_DATASOURCE_PASSWORD`: postgres

### Остановка приложения

```bash
docker compose down
```

Для удаления данных БД:
```bash
docker compose down -v
```

---

## Генерация документов

### Предварительные требования
- python3

```bash
cd document-generator
python3 generate.py
```

### Проверка прогресса по логам

SUBMIT-worker
Регулярно проверяет БД и отправляет документы со статусом DRAFT на согласование пачками по batchSize

```
SUBMIT-worker: processed=100, success=100, failed=0
```

APPROVE-worker
Регулярно проверяет БД и отправляет документы со статусом SUBMITTED на утверждение пачками по batchSize

```
APPROVE-worker: processed=100, success=100, failed=0
```

### Чтобы сервис уверенно работал с запросами 5000+ id возможны различные решения:

- Передавать ID через временную таблицу, а в запросе на чтение использовать JOIN вместо WHERE IN;
- Увеличить лимиты JDBC драйвера и Connection Pool Size;
- Увеличить heap JVM;
- Читать запросы батчами в несколько потоков, также запись батчами;
- Создать отдельную БД, ориентированную на чтение (паттерн CQRS);

## Реестр утверждений

Реестр утверждений можно вынести в отдельный микросервис со своей БД.
Обращение к данному сервису можно сделать асинхронным через брокер сообщений
(RabbitMQ или при высокой нагрузке Kafka либо если Kafka уже есть на проекте).