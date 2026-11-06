# java-lab-service

![Java 25](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 4.1.2](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)
![Gradle 9.8.2](https://img.shields.io/badge/Gradle-9.8.2-02303A?logo=gradle&logoColor=white)
![Tests](https://img.shields.io/badge/tests-11%20passed-brightgreen)

Учебный сервис менторской программы **Java Lab**. Принцип программы: одна технология — один урок. Сервис растёт от урока
к уроку: от пустого Gradle-проекта до Kafka, GraphQL и деплоя.

## Прогресс

| Урок | Тема                                        | Статус | Ветка            |
|:----:|---------------------------------------------|:------:|------------------|
|  00  | Доступы, окружение и настройка проекта      |   ✅   | `main`           |
|  01  | Spring Boot 4: сервис и конфигурация        |   ✅   | `task-2` → PR #1 |
|  02  | Java 25: virtual threads и современные фичи |   ✅   | `task-3`         |

<details>
<summary>Дальше по программе</summary>

| Урок | Тема                                       |
|:----:|--------------------------------------------|
|  03  | Реактивный стек: Project Reactor и WebFlux |
|  04  | Наблюдаемость: Micrometer и Actuator       |
|  05  | Flyway: миграции схемы БД                  |
|  06  | JOOQ: типобезопасный SQL                   |
|  07  | MongoDB: документы и GridFS                |
|  08  | Redis: кэш и структуры данных              |
|  09  | ElasticSearch: полнотекстовый поиск        |
|  10  | ClickHouse: аналитическое хранилище        |
|  11  | HTTP-клиенты: RestClient и HTTP Interfaces |
|  12  | RabbitMQ: асинхронные бизнес-флоу          |
|  13  | Kafka: стриминг событий                    |
|  14  | GraphQL: сервер, клиент и федерация        |
|  15  | Тестирование: JUnit 5 и Testcontainers     |
|  16  | Docker и деплой на тестовую среду          |

</details>

Каждый урок делается в отдельной ветке и попадает в `main` через pull request после ревью ментора.

## Быстрый старт

Нужен **JDK 25**, например Temurin через [SDKMAN!](https://sdkman.io). Gradle ставить не нужно, в репозитории есть
wrapper. Если JDK 25 не найден, Gradle скачает его сам через toolchain.

```bash
git clone git@github.com:kirimikh/java-lab-service.git
cd java-lab-service

./gradlew build     # сборка и тесты
./gradlew bootRun   # сервис на http://localhost:8080
```

```bash
curl 'http://localhost:8080/api/greeting?name=lab'
# {"message":"Hello, lab!"}
```

## API

| Метод | Путь                        | Что делает                                                                           |
|-------|-----------------------------|--------------------------------------------------------------------------------------|
| `GET` | `/api/greeting?name={name}` | Приветствие по шаблону из конфигурации. `name` по умолчанию — `world`                |
| `GET` | `/api/aggregate/{id}`       | Параллельно собирает данные пользователя и его лимиты из двух «медленных» источников |
| `GET` | `/api/aggregate/fail`       | Демонстрация отказа: один источник падает, второй отменяется                         |

```bash
curl 'http://localhost:8080/api/aggregate/42'
# {"user":{"id":"42","name":"User 42"},"limits":{"userId":"42","daily":50000.00}}
```

## Конфигурация

Настройки описаны в [`application.yaml`](app/src/main/resources/application.yaml) и связаны с типизированным record
[`GreetingProperties`](app/src/main/java/org/example/config/GreetingProperties.java) через `@ConfigurationProperties`,
без `@Value`.

| Свойство                         | По умолчанию | Описание                                                     |
|----------------------------------|--------------|--------------------------------------------------------------|
| `lab.greeting.template`          | `Hello, %s!` | Шаблон приветствия, `%s` заменяется на имя                   |
| `lab.greeting.signed`            | `false`      | Флаг подписи. В `prod` равен `true`, на ответ пока не влияет |
| `spring.threads.virtual.enabled` | `true`       | Обработка HTTP-запросов на virtual threads                   |

### Профили

| Профиль | `template`         | `signed` | Ответ на `?name=lab` |
|---------|--------------------|----------|----------------------|
| —       | `Hello, %s!`       | `false`  | `Hello, lab!`        |
| `dev`   | `[dev] Hello, %s!` | `false`  | `[dev] Hello, lab!`  |
| `prod`  | `Hello, %s!`       | `true`   | `Hello, lab!`        |

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Приоритет источников

Любое свойство можно переопределить без пересборки. Источники ниже перечислены от самого сильного к самому слабому:

1. аргументы командной строки: `--lab.greeting.template='Yo, %s'`
2. переменные окружения: `LAB_GREETING_TEMPLATE='Hi, %s'`
3. документ активного профиля в `application.yaml`
4. базовый документ `application.yaml`

```bash
LAB_GREETING_TEMPLATE='Hi, %s' ./gradlew bootRun
# {"message":"Hi, lab"}
```

## Virtual threads и structured concurrency

HTTP-запросы обрабатываются на virtual threads: на каждый запрос в лог пишется `handled on VirtualThread[...]`.

Эндпоинт `/api/aggregate/{id}` ходит в два независимых источника через `StructuredTaskScope`
([JEP 505](https://openjdk.org/jeps/505), preview в Java 25): `UserClient` отвечает за 1 с, `LimitsClient` — за 1,5 с.
Подзадачи выполняются параллельно, поэтому ответ приходит примерно за 1,5 с, то есть за время самого медленного
вызова, а не за сумму 2,5 с.

Если одна подзадача падает, scope отменяет вторую, и эндпоинт отвечает ошибкой, не дожидаясь её:
`/api/aggregate/fail` возвращает `500` примерно за 0,25 с вместо 1,5 с.

Preview-возможности включены флагом `--enable-preview` для компиляции, тестов и `bootRun`
в [`app/build.gradle.kts`](app/build.gradle.kts).

### Нагрузка: virtual vs platform threads

Время самого долгого ответа `/api/aggregate/42` при N параллельных запросах (ноутбук, 14 ядер, настройки Tomcat по
умолчанию):

| Параллельных запросов | virtual threads | platform threads |
|:---------------------:|:---------------:|:----------------:|
|          100          |     ~1,5 с      |      ~1,5 с      |
|         1000          |     ~1,5 с      |      ~6,8 с      |

На 100 запросах разницы нет: в пуле Tomcat 200 платформенных потоков, и места хватает всем. На 1000 платформенные потоки
обрабатывают запросы волнами по 200, а virtual threads — все сразу. Почти всё время запрос ждёт ввода-вывода, а
ожидающий virtual thread не занимает поток ОС.

Как воспроизвести:

```bash
./gradlew bootRun                                                # virtual threads
./gradlew bootRun --args='--spring.threads.virtual.enabled=false' # platform threads

seq 1000 | xargs -P1000 -I{} curl -s -o /dev/null -w '%{http_code} %{time_total}\n' \
  'http://localhost:8080/api/aggregate/42' | sort -k2 -n | tail -3
```

## Доменная модель

События платежа описаны sealed-интерфейсом и records. Компилятор знает все варианты, поэтому `switch` обходится без
`default`. Если добавить новый тип в `permits` и не обработать его, сборка упадёт.

```java
public sealed interface PaymentEvent permits Authorized, Declined, Refunded, Chargeback {
}
```

```java
return switch (event) {
    case Authorized(var id, var amount) -> "authorized %s: %s".formatted(id, amount);
    case Declined(var id, var reason)   -> "declined %s: %s".formatted(id, reason);
    case Refunded(var id, var amount)   -> "refunded %s: %s".formatted(id, amount);
    case Chargeback(var id, var amount) -> "chargeback %s: %s".formatted(id, amount);
};
```

## Структура

```
app/src/main/java/org/example/
├── App.java                 # точка входа, @ConfigurationPropertiesScan
├── config/                  # типизированная конфигурация
├── controller/              # REST-контроллеры и DTO ответов
├── client/                  # «медленные» внешние источники (имитация)
└── payment/                 # доменная модель событий платежа
```

## Тесты

```bash
./gradlew test
```

| Тест                               | Что проверяет                                                   |
|------------------------------------|-----------------------------------------------------------------|
| `AppTest`                          | Контекст Spring поднимается целиком (smoke, `@SpringBootTest`)  |
| `GreetingControllerTest`           | `/api/greeting` с именем и без (`@WebMvcTest` + `MockMvc`)      |
| `GreetingControllerDevProfileTest` | Шаблон профиля `dev` (`@ActiveProfiles`)                        |
| `AggregateControllerTest`          | Параллельная агрегация, быстрый отказ и отмена второй подзадачи |
| `PaymentEventDescriberTest`        | `describe()` для каждого типа события                           |
