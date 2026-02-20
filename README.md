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