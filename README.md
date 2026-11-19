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
Набор для защиты содержит **106 пользователей, 85 мероприятий и 1547 заявок**:
99 волонтёров, 6 координаторов и администратора. 43 мероприятия запланированы на
ближайшие 36 дней, 42 завершены и наполняют историю за предыдущие месяцы.
Все даты сдвигаются при запуске. Рейтинги пересчитывает `RatingInitializer`.

Данные ориентированы на [debug-вход MOBILE](https://github.com/ADT-VOLUNTEERS-CASE/MOBILE/blob/main/presentation/src/debug/kotlin/org/adt/presentation/screens/credentialsConfigurator/LoginDebugViewModel.kt).
При `DEMO_MOBILE_DEBUG_LOGIN=true` (по умолчанию) три основных аккаунта получают
пароли из мобильного debug-входа, независимо от значений `*_PASSWORD` в `.env`:

| Учётная запись | Роль | Пароль debug-входа |
| --- | --- | --- |
| `admin@example.com` | Администратор | `admin_pass` |
| `coordinator@example.com` | Мария Иванова, координатор 61 мероприятия | `coordinator_pass` |
| `user@example.com` | Анна Петрова, основной демонстрационный волонтёр | `base_pass` |

`DEMO_MOBILE_DEBUG_LOGIN=false` возвращает пароли основных аккаунтов из
`ADMIN_PASSWORD`, `COORDINATOR_PASSWORD` и `BASE_USER_PASSWORD`.
`user.two@example.com` и `user.three@example.com` используют `BASE_USER_TWO_PASSWORD`
и `BASE_USER_THREE_PASSWORD`. Дополнительные `volunteer.001@example.com` …
`volunteer.096@example.com` используют `BASE_USER_TWO_PASSWORD`, а
`coordinator.01@example.com` … `coordinator.05@example.com` — `COORDINATOR_PASSWORD`.
При отсутствии этих переменных действуют значения по умолчанию из `DataLoader`.

Что показывать в мобилке:

1. Быстрый вход волонтёром: каталог с обложками, поиск, записи на ближайшие недели в календаре.
2. Профиль, статистика за месяцы и рейтинг: у основного волонтёра 33 подтверждённых завершённых участия.
3. Новая запись на «Экофорум: команда волонтёров»: изначально на нём нет заявок.
4. Быстрый вход координатором: очередь ожидающих заявок, подтверждение и отказ с причиной.
5. Профиль координатора, рейтинг и PDF-отчёт за месяц или весь период.
6. Админка: заполненные списки пользователей, тегов, локаций и обложек.

Также есть отозванные и удалённая заявки и маленькая группа
«Фотокоманда: практический инструктаж» с занятыми местами для проверки лимита.
Мероприятия и участники демонстрационные, расписание не является афишей реальных событий.

Обложки берутся из `src/main/resources/images`. Для каждого мероприятия
создаётся отдельная обложка в S3 на основе одной из шести исходных картинок. Для запуска нужны
рабочие настройки `S3_*`. Пересоздание БД не удаляет старые объекты из S3.
Docker использует Java 21 и московский часовой пояс.

После получения изменений на сервере пересоберите и перезапустите backend:

```bash
git pull --ff-only
docker compose --env-file .env -f docker-compose.yml up -d --build --force-recreate app
docker compose -f docker-compose.yml logs -f app
```

Дождитесь завершения загрузки `DataLoader` и запуска приложения. Таблицы будут
пересозданы даже при сохранении существующего тома PostgreSQL. Одна лишь отправка
коммита в GitHub не обновляет запущенный контейнер.

Проверка генератора и сервисов без внешних подключений:
`mvn -Dtest=DataLoaderTest,'*ServiceTest' test`.

Проверка обоих профилей и очистки при повторном запуске на **отдельной одноразовой PostgreSQL**
(S3 заменён тестовой реализацией; таблицы указанной БД будут удалены):

```bash
DEMO_TEST_DB_URL=jdbc:postgresql://localhost:5432/demo_test \
DEMO_TEST_DB_USER=postgres DEMO_TEST_DB_PASSWORD=test \
mvn -Dtest=DemoDataIntegrationTest test
```

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
