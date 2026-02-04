## Требования
- JDK 17+
- Инструмент сборки: Maven
- Docker установлен и запущен (для docker запуска)

## Подготовка
- Клонируйте репозиторий
- Установите зависимости через выбранный инструмент:
  - Maven: `mvn clean install`

## Профили запуска

### JVM (native)
- Maven: `mvn spring-boot:run`

### Docker (контейнер)
- Prod: 
  - Сборка: `docker compose -f docker-compose.yml build`
  - Запуск: `docker compose -f docker-compose.yml up -d`
  - Запуск(получает переменные из .env): `docker compose --env-file .env -f docker-compose.yml up -d`
  - Остановка: `docker compose -f docker-compose.yml down`
- Dev:
  - Сборка: `docker compose -f docker-compose.dev.yml build`
  - Запуск: `docker compose -f docker-compose.dev.yml up -d`
  - Запуск(получает переменные из .env): `docker compose --env-file .env -f docker-compose.dev.yml up -d`
  - Остановка: `docker compose -f docker-compose.dev.yml down`

## Переменные окружения
- Prod (`docker-compose.yml`): `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`, `SECRET_KEY`, `ADMIN_PASSWORD`
- Dev (`docker-compose.dev.yml`): `DB_USRNAME`, `DB_PASSWORD`, `DB_NAME`, `SECRET_KEY`, `ADMIN_PASSWORD`
- Примеры установки:
  - Linux/macOS: `export DB_USERNAME=...`
  - PowerShell: `$env:DB_USERNAME="..."`
  - Файл `.env` (рядом с compose):  
    `DB_USERNAME=...`  
    `DB_PASSWORD=...`  
    `DB_NAME=...`  
    `SECRET_KEY=...`
    `ADMIN_PASSWORD=...`
- `DB_URL` формируется внутри compose-файлов, подставляя указанные переменные.

