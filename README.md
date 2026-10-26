## Требования

- JDK 21+
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

## Демонстрационные данные и пересоздание БД

В профилях `dev` и `prod` сейчас включено `spring.jpa.hibernate.ddl-auto=create`.
При каждом запуске backend Hibernate удаляет таблицы приложения и создаёт их заново,
затем `DataLoader` заполняет их демонстрационными данными. **Все изменения данных
в этих таблицах будут потеряны при следующем запуске backend.** Перезапуск только
контейнера PostgreSQL не запускает `DataLoader`; для обновления данных перезапустите приложение.

Даты рассчитываются относительно момента запуска в часовом поясе JVM.
Для московского времени при запуске можно задать `JAVA_TOOL_OPTIONS=-Duser.timezone=Europe/Moscow`.
Предстоящие мероприятия назначаются на ближайшие 1–14 дней, завершённые — на вчерашний день,
10 дней назад и четыре предыдущих календарных месяца. Это демонстрационное расписание.
Волонтёрские часы, интересы пользователей и история заявок заполняются вместе с мероприятиями;
рейтинги пересчитываются существующим `RatingInitializer` после загрузки данных.

| Учётная запись | Роль | Пароль из переменной |
| --- | --- | --- |
| `admin@example.com` | Администратор | `ADMIN_PASSWORD` |
| `coordinator@example.com` | Координатор всех мероприятий | `COORDINATOR_PASSWORD` |
| `user@example.com` | Волонтёр: Анна Петрова | `BASE_USER_PASSWORD` |
| `user.two@example.com` | Волонтёр: Дмитрий Соколов | `BASE_USER_TWO_PASSWORD` |
| `user.three@example.com` | Волонтёр: Елена Волкова | `BASE_USER_THREE_PASSWORD` |

У каждого волонтёра есть подтверждённые и ожидающие заявки, завершённые участия
и доступные для новой регистрации мероприятия. Для проверки экранов также есть
отклонённые и отозванные заявки, удалённая заявка, мероприятие без заявок и группа с лимитом
в два активных участника, в которой все места заняты.

Обложки берутся из `src/main/resources/images` и загружаются через существующее
S3-хранилище. Для запуска по-прежнему нужны рабочие настройки `S3_*`.
Пересоздание схемы БД не удаляет ранее загруженные объекты из S3.

Проверка генератора и сервисов без внешних подключений:
`mvn -Dtest=DataLoaderTest,'*ServiceTest' test`.

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
