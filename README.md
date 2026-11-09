# java-lab-service · reactive

![Java 25](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 4.1.2](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring-WebFlux-6DB33F?logo=spring&logoColor=white)
![Project Reactor](https://img.shields.io/badge/Project-Reactor-6DB33F)
![Tests](https://img.shields.io/badge/tests-18%20passed-brightgreen)

> **Это отдельная реактивная версия сервиса** (урок 03 программы Java Lab, ветка `lab-webflux`).
> В `main` она не сливается: MVC и WebFlux в одном приложении не смешиваются.
> Основная версия на Spring MVC и virtual threads лежит в [`main`](https://github.com/kirimikh/java-lab-service/tree/main).

## Состав ветки

- [x] WebFlux вместо Web MVC (`spring-boot-starter-webflux`, `reactor-test`)
- [x] `GET /api/rates`: бесконечный поток котировок раз в секунду (Server-Sent Events)
- [x] `GET /api/aggregate/{id}`: два источника параллельно через `Mono.zip`, таймаут 2 с и fallback
- [x] Внешний вызов через неблокирующий `WebClient`
- [x] В продовом пути нет `.block()` и блокирующих вызовов на event loop
- [x] Тесты на `StepVerifier`, включая таймаут на виртуальном времени
- [x] Заметка-сравнение reactive vs virtual threads (ниже)

## Быстрый старт

Нужен **JDK 25**. Gradle ставить не нужно, в репозитории есть wrapper.

```bash
git clone git@github.com:kirimikh/java-lab-service.git
cd java-lab-service
git switch lab-webflux

./gradlew build     # сборка и тесты
./gradlew bootRun   # сервис на http://localhost:8080
```

## API

| Метод | Путь                        | Что делает                                                                      |
|-------|-----------------------------|---------------------------------------------------------------------------------|
| `GET` | `/api/rates`                | Бесконечный поток котировок `USD/RUB` раз в секунду (SSE)                       |
| `GET` | `/api/aggregate/{id}`       | Пользователь и лимиты параллельно через `Mono.zip`, таймаут 2 с, fallback       |
| `GET` | `/api/limits/{id}`          | «Внешний» сервис лимитов, в него `LimitsClient` ходит по HTTP через `WebClient` |
| `GET` | `/api/greeting?name={name}` | Приветствие по шаблону из конфигурации (из уроков 00–01)                        |

Поток котировок удобно смотреть через `curl -N`: флаг отключает буферизацию, и события видны по мере появления.

```bash
curl -N http://localhost:8080/api/rates
# data:{"pair":"USD/RUB","value":89.1856,"timestamp":"2026-11-06T12:00:05.292458174Z"}
# data:{"pair":"USD/RUB","value":89.8992,"timestamp":"2026-11-06T12:00:06.291576011Z"}
```

Сценарии агрегации:

| Запрос                | Что происходит                                 | Ответ                                                       |
|-----------------------|------------------------------------------------|-------------------------------------------------------------|
| `/api/aggregate/42`   | оба источника ответили (1 с и 1,5 с)           | `200` с данными за ~1,7 с                                   |
| `/api/aggregate/slow` | лимиты отвечают 3 с, срабатывает таймаут       | `200` с fallback за ~2 с: пользователь `unknown`, лимит `0` |
| `/api/aggregate/fail` | сервис пользователей падает, лимиты отменяются | `500` за ~0,3 с                                             |

Fallback отдаёт нулевой лимит: если лимиты неизвестны, безопаснее отказать в операции, чем пропустить её.

### Два инстанса

По умолчанию сервис ходит за лимитами сам в себя (`lab.limits.base-url: http://localhost:${server.port:8080}`). Чтобы лимиты обслуживал отдельный процесс:

```bash
./gradlew bootJar

# «внешний» сервис лимитов
java --enable-preview -jar app/build/libs/app.jar --server.port=8082

# основной сервис ходит за лимитами в 8082
java --enable-preview -jar app/build/libs/app.jar --lab.limits.base-url=http://localhost:8082
```

Отмена проходит через сеть: когда в основном сервисе срабатывает таймаут, `WebClient` закрывает соединение, и сервис лимитов на другом процессе прекращает работу. В его логе видно `limits slow request cancelled by client`.

## Тесты

```bash
./gradlew test
```

| Тест                                                         | Что проверяет                                                                                                  |
|--------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `ReactorWarmupTest`                                          | Разминка: `map`/`filter` и сигнал ошибки в `StepVerifier`                                                      |
| `AggregateControllerTest`                                    | Параллельность, таймаут с fallback и ошибка источника на виртуальном времени                                   |
| `RatesControllerTest`                                        | Одна котировка в секунду, бесконечный поток на виртуальном времени                                             |
| `NoBlockingTest`                                             | Под [BlockHound](https://github.com/reactor/BlockHound) на настоящем Netty: в цепочках нет блокирующих вызовов |
| `GreetingControllerTest`, `GreetingControllerDevProfileTest` | `/api/greeting` через `@WebFluxTest` и `WebTestClient`                                                         |
| `AppTest`                                                    | Контекст Spring поднимается целиком                                                                            |
| `PaymentEventDescriberTest`                                  | Доменная модель из урока 02                                                                                    |

Тесты таймаута идут на виртуальном времени, поэтому 2 с «ожидания» проходят за сотые доли секунды. `NoBlockingTest` проверен контрольной поломкой: `Thread.sleep` в цепочке `LimitsController` роняет тест с `Blocking call! java.lang.Thread.sleepNanos0`.

## Reactive vs virtual threads

В двух версиях проекта `GET /api/aggregate/{id}` запрашивает пользователя и лимиты параллельно: в MVC через `StructuredTaskScope`, в WebFlux через `Mono.zip`. Успешный ответ ждёт более медленный источник. WebFlux сам по себе не ускоряет один запрос: оба подхода позволяют эффективно обслуживать множество запросов, ожидающих данные.

WebFlux удобен для потоковых ответов: в `/api/rates` клиент получает котировки через SSE по мере их появления, а `Flux` описывает поток событий. SSE можно реализовать и в MVC. WebFlux также подходит, когда используются неблокирующие клиенты и реактивные драйверы БД или брокеров: event loop обслуживает множество соединений небольшим числом потоков. Выигрыш по ресурсам относительно virtual threads нужно подтверждать измерениями.

Реактивные потоки поддерживают backpressure: потребитель сообщает, сколько элементов готов принять. Однако для медленного потребителя нужно выбрать поведение источника — например, ограниченный буфер или пропуск промежуточных котировок. `Flux.interval` не создаёт бесконечный буфер автоматически и при отсутствии спроса на очередной тик может завершиться ошибкой.

В WebFlux важно не блокировать event loop: `Thread.sleep` внутри такой цепочки задержит обработку других событий на том же потоке. В проекте ожидание реализовано через `Mono.delay`, HTTP-вызов — через неблокирующий `WebClient`, а тест с `BlockHound` помогает обнаруживать блокировки.

MVC с virtual threads подходит для обычного request-response с блокирующими библиотеками, например JDBC или синхронным HTTP-клиентом. Во время поддерживаемого блокирующего I/O virtual thread может освобождать системный поток. Поэтому большое количество ожидающих запросов само по себе не требует перехода на WebFlux.

Цена реактивного подхода — необходимость разбираться в операторах `Mono` и `Flux`, сигналах ошибок и отмене подписок. При отладке нужно учитывать, что выполнение цепочки может продолжаться позже и на другом потоке; при необходимости помогают `checkpoint()` и инструменты отладки Reactor.
