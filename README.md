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

- Prod (`docker-compose.yml`):
  `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`, `SECRET_KEY`, `ADMIN_PASSWORD`,
  `COORDINATOR_PASSWORD`, `BASE_USER_PASSWORD`, `BASE_USER_TWO_PASSWORD`, `BASE_USER_THREE_PASSWORD`,
  `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_PUBLIC_BASE_URL`,
  `S3_COVER_PREFIX`, `S3_PATH_STYLE_ACCESS`

- Dev (`docker-compose.dev.yml`):
  `DB_USRNAME`, `DB_PASSWORD`, `DB_NAME`, `SECRET_KEY`, `ADMIN_PASSWORD`,
  `COORDINATOR_PASSWORD`, `BASE_USER_PASSWORD`, `BASE_USER_TWO_PASSWORD`, `BASE_USER_THREE_PASSWORD`,
  `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_PUBLIC_BASE_URL`,
  `S3_COVER_PREFIX`, `S3_PATH_STYLE_ACCESS`

- Примеры установки:
  - Linux/macOS: `export DB_USERNAME=...`
  - PowerShell: `$env:DB_USERNAME="..."`
  - Файл `.env` (рядом с compose):

  ```env
  DB_USERNAME=...
  DB_PASSWORD=...
  DB_NAME=...
  SECRET_KEY=...

  ADMIN_PASSWORD=...
  COORDINATOR_PASSWORD=...
  BASE_USER_PASSWORD=...
  BASE_USER_TWO_PASSWORD=...
  BASE_USER_THREE_PASSWORD=...

  S3_ENDPOINT=...
  S3_REGION=...
  S3_BUCKET=...
  S3_ACCESS_KEY=...
  S3_SECRET_KEY=...
  S3_PUBLIC_BASE_URL=...
  S3_COVER_PREFIX=...
  S3_PATH_STYLE_ACCESS=...

- DB_URL формируется внутри compose-файлов, подставляя указанные переменные.
