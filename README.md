# Telegram Bot Feedback для СТО

Цей проєкт — Spring Boot додаток, який приймає відгуки через Telegram-бота, зберігає їх у PostgreSQL, дублює в Google Docs і при критичних оцінках (рівень 4–5) автоматично створює картку в Trello.

---

## 1. Встановлення Docker

Для запуску проєкту потрібні Docker та Docker Compose.

* Встановіть Docker Desktop з офіційного сайту: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
* Перевірте встановлення:

  ```bash
  docker --version
  docker compose version
  ```

---

## 2. Налаштування .env файлу

У корені проєкту створіть файл `.env` за зразком `.env.example` і заповніть своїми даними.

Приклад:

```env
# Database
DB_URL=postgres:5432/feedbackdb
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Telegram Bot
BOT_USERNAME=your_bot_username
BOT_TOKEN=your_bot_token

# Google Docs
DOC_ID=your_google_doc_id

# OpenAI
OPENAI_API_KEY=sk-xxxxxx

# Trello
TRELLO_API_KEY=your_api_key
TRELLO_API_TOKEN=your_api_token
TRELLO_LIST_ID=your_list_id
TRELLO_CRITICAL_LABEL_IDS=labelId1,labelId2
```

---
## 3. Налаштування Google Docs API

Щоб зберігати відгуки в Google Docs:

1. Перейдіть у [Google Cloud Console](https://console.cloud.google.com/).
2. Створіть проєкт та увімкніть API Google Docs.
3. Створіть сервісний акаунт і завантажте ключ у форматі JSON (`sa-key.json`).
4. Перейменуйте його на `sa-key.json` та помістіть у папку `src/main/resources`.

---

## 4. Налаштування Telegram Bot

Щоб бот працював, потрібні Telegram Username та Token.

1. Створіть бота через @BotFather у Telegram.
2. Отримайте **BOT\_USERNAME** та **BOT\_TOKEN**.
3. Додайте їх у `.env` файл:

```env
# Telegram Bot
BOT_USERNAME=your_bot_username
BOT_TOKEN=your_bot_token
```

---

## 5. Отримання API Trello та ID списку

### 5.1 API Key та Token

1. Перейдіть на [Trello API Key](https://trello.com/power-ups/admin/new).
2. Скопіюйте **API Key**.
3. Натисніть на посилання для генерації **Token**, підтвердіть доступ та збережіть його.

Приклад:

```env
TRELLO_API_KEY=ae639aab4c75aa2ee86eafb4f14b9e15
TRELLO_API_TOKEN=ded9dde13c993c50661fccdb60a947604b4639a97496227333ea528ed2c83930
```

### 5.2 ID списку (List ID)

1. Знайдіть `boardId` у URL вашої дошки.
   Приклад: `https://trello.com/b/80WRZ9Xt/sto` → boardId = `80WRZ9Xt`

2. Виконайте запит:

   ```bash
   curl "https://api.trello.com/1/boards/<BOARD_ID>/lists?key=<API_KEY>&token=<API_TOKEN>"
   ```

3. У відповіді знайдіть `id` потрібного списку.

   ```json
   {
     "id": "68bc2a6e9b0ed71abfdff172",
     "name": "Потрібно зробити"
   }
   ```

   → `68bc2a6e9b0ed71abfdff172` — ваш **List ID**.

### 5.3 ID міток

1. Виконайте запит:

   ```bash
   curl "https://api.trello.com/1/boards/<BOARD_ID>/labels?key=<API_KEY>&token=<API_TOKEN>"
   ```
2. Знайдіть `id` потрібних міток.
3. Додайте їх через кому у `.env`:

   ```env
   TRELLO_CRITICAL_LABEL_IDS=labelId1,labelId2
   ```

---

## 6. Збірка та запуск з Docker Compose

У проєкті є `docker-compose.yml`, який запускає PostgreSQL, PgAdmin та сам додаток.

### 6.1 Збірка проєкту

```bash
docker compose build
```

### 6.2 Запуск проєкту

```bash
docker compose up -d
```

### 6.3 Перевірка

* Додаток: [http://localhost:8080](http://localhost:8080)
* PgAdmin: [http://localhost:80](http://localhost:80)

---

## 7. Корисні команди

Зупинити контейнери:

```bash
docker compose down
```

Перегляд логів:

```bash
docker compose logs -f app
```

---

Тепер проєкт готовий до використання 🎉
