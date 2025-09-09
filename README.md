# Telegram Bot Feedback for Service Stations

🌐 Available languages: [English](README.md) | [Українська](README.ua.md)

This project is a Spring Boot application that collects feedback through a Telegram bot, stores it in PostgreSQL, duplicates it in Google Docs, and automatically creates a Trello card for critical ratings (level 4–5).

---

## 1. Install Docker

To run the project, you need Docker and Docker Compose.

* Install Docker Desktop from the official site: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
* Verify installation:

  ```bash
  docker --version
  docker compose version
  ```

---

## 2. Configure the .env File

In the project root, create a `.env` file based on `.env.example` and fill it with your values.

Example:

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

## 3. Configure Google Docs API

To store feedback in Google Docs:

1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Create a project and enable the Google Docs API.
3. Create a service account and download the key in JSON format (`sa-key.json`).
4. Rename it to `sa-key.json` and place it in `src/main/resources`.

---

## 4. Configure Telegram Bot

To make the bot work, you need a Telegram Username and Token.

1. Create a bot using @BotFather in Telegram.
2. Obtain **BOT\_USERNAME** and **BOT\_TOKEN**.
3. Add them to the `.env` file:

```env
# Telegram Bot
BOT_USERNAME=your_bot_username
BOT_TOKEN=your_bot_token
```

---

## 5. Get Trello API and List ID

### 5.1 API Key and Token

1. Go to [Trello API Key](https://trello.com/power-ups/admin/new).
2. Copy your **API Key**.
3. Click the link to generate a **Token**, grant access, and save it.

Example:

```env
TRELLO_API_KEY=ae639aab4c75aa2ee86eafb4f14b9e15
TRELLO_API_TOKEN=ded9dde13c993c50661fccdb60a947604b4639a97496227333ea528ed2c83930
```

### 5.2 List ID

1. Find the `boardId` in your Trello board URL.
   Example: `https://trello.com/b/80WRZ9Xt/sto` → boardId = `80WRZ9Xt`

2. Make a request:

   ```bash
   curl "https://api.trello.com/1/boards/<BOARD_ID>/lists?key=<API_KEY>&token=<API_TOKEN>"
   ```

3. In the response, find the `id` of the desired list.

   ```json
   {
     "id": "68bc2a6e9b0ed71abfdff172",
     "name": "To Do"
   }
   ```

   → `68bc2a6e9b0ed71abfdff172` is your **List ID**.

### 5.3 Label IDs

1. Make a request:

   ```bash
   curl "https://api.trello.com/1/boards/<BOARD_ID>/labels?key=<API_KEY>&token=<API_TOKEN>"
   ```
2. Find the `id` of the required labels.
3. Add them to `.env`, separated by commas:

   ```env
   TRELLO_CRITICAL_LABEL_IDS=labelId1,labelId2
   ```

---

## 6. Build and Run with Docker Compose

The project includes a `docker-compose.yml` that launches PostgreSQL, PgAdmin, and the application itself.

### 6.1 Build the project

```bash
docker compose build
```

### 6.2 Run the project

```bash
docker compose up -d
```

### 6.3 Verify

* App: [http://localhost:8080](http://localhost:8080)
* PgAdmin: [http://localhost:80](http://localhost:80)

---

## 7. Useful Commands

Stop containers:

```bash
docker compose down
```

View logs:

```bash
docker compose logs -f app
```

---

Now the project is ready to use 🎉
