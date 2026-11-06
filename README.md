# java-lab-service · reactive

![Java 25](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 4.1.2](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)
![Spring WebFlux](https://img.shields.io/badge/Spring-WebFlux-6DB33F?logo=spring&logoColor=white)
![Project Reactor](https://img.shields.io/badge/Project-Reactor-6DB33F)

> **Это отдельная реактивная версия сервиса** (урок 03 программы Java Lab, ветка `lab-webflux`).
> В `main` она не сливается: MVC и WebFlux в одном приложении не смешиваются.
> Основная версия на Spring MVC и virtual threads лежит в [`main`](https://github.com/kirimikh/java-lab-service/tree/main).

**Статус:** 🔄 в работе

## Что поменялось относительно `main`

|                     | `main`                           | `lab-webflux`                     |
|---------------------|----------------------------------|-----------------------------------|
| Веб-стек            | Spring Web MVC                   | Spring WebFlux                    |
| Сервер              | Tomcat                           | Reactor Netty                     |
| Модель потоков      | поток на запрос, virtual threads | event loop, потоков по числу ядер |
| Параллельные вызовы | `StructuredTaskScope`            | `Mono.zip`                        |
| HTTP-клиент         | —                                | `WebClient`                       |
| Тесты цепочек       | JUnit, `MockMvc`                 | `StepVerifier`                    |

## Состав ветки

- [x] WebFlux вместо Web MVC (`spring-boot-starter-webflux`, `reactor-test`)
- [x] `GET /api/rates`: бесконечный поток котировок раз в секунду (Server-Sent Events)
- [x] `GET /api/aggregate/{id}`: два источника параллельно через `Mono.zip`, таймаут 2 с и fallback
- [x] Внешний вызов через неблокирующий `WebClient`
- [x] В продовом пути нет `.block()` и блокирующих вызовов на event loop
- [ ] Тесты на `StepVerifier`, включая таймаут на виртуальном времени
- [ ] Заметка-сравнение reactive vs virtual threads (ниже)

## Быстрый старт

Нужен **JDK 25**. Gradle ставить не нужно, в репозитории есть wrapper.

```bash
git clone git@github.com:kirimikh/java-lab-service.git
cd java-lab-service
git switch lab-webflux

./gradlew build     # сборка и тесты
./gradlew bootRun   # сервис на http://localhost:8080
```

Поток котировок удобно смотреть через `curl -N`: флаг отключает буферизацию, и события видны по мере появления.

```bash
curl -N http://localhost:8080/api/rates
```

## Reactive vs virtual threads

### Когда брать WebFlux

-

**Стриминг**: SSE, WebSocket, долгие соединения. `Flux` — естественная модель для потока событий, а тысячи открытых соединений дёшево держать на event loop.

- **Backpressure**: потребитель сам задаёт темп, и быстрый источник не заваливает медленного клиента.
- **Реактивные драйверы на всём
  пути**: R2DBC, реактивные клиенты Mongo, Redis, Kafka. Выигрыш появляется, только когда неблокирующей является вся цепочка.
- **Предельная плотность на
  ядро**: несколько потоков event loop обслуживают огромное число запросов при минимуме памяти.

### Когда брать virtual threads

- **Обычный request-response поверх блокирующих
  библиотек**: JDBC, jOOQ, `RestClient`. Код остаётся простым и последовательным.
- **Читаемые stacktrace и простая отладка**: в стеке видно твой код, а не внутренности Reactor.
- **Параллельные вызовы без смены парадигмы**: `StructuredTaskScope` даёт то же, что `Mono.zip`, обычными `try`/`catch`.
- **Масштабируемости ожидания хватает с
  запасом**: в уроке 02 тысяча параллельных запросов к `/api/aggregate` на virtual threads отработала примерно за 1,5 с каждый, а на платформенных потоках самый долгий ждал около 6,8 с.

### Чем платим за реактив

- **Когнитивная
  сложность.** Логика собирается из операторов, а не пишется циклами и `if`. `Mono`/`Flux` расползаются по сигнатурам всех слоёв.
- **Отладка
  цепочек.** Stacktrace показывает внутренности Reactor; чтобы найти место ошибки, нужны `checkpoint()` и отладочные хуки.
- **Цена одной
  ошибки.** Один блокирующий вызов на event loop занимает поток, которых всего по числу ядер, и проседает пропускная способность всего сервиса.
- **Тесты и
  инфраструктура.** Нужны `StepVerifier` и виртуальное время, а контекст (MDC, трейсинг) приходится передавать отдельно.

### Выбор для типовых сценариев

| Сценарий                                          | Стек            | Почему                                                                                                                                           |
|---------------------------------------------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| Стриминг котировок клиентам в реальном времени    | WebFlux         | Поток событий и долгие соединения, backpressure из коробки                                                                                       |
| CRUD-сервис над MySQL через jOOQ                  | Virtual threads | Обычный JDBC блокирующий; ради реактива пришлось бы переписать доступ к БД, а virtual threads дают масштабируемость ожидания без этого           |
| Агрегатор нескольких внешних API (запрос — ответ) | Virtual threads | Запрос в основном ждёт I/O; `StructuredTaskScope` распараллеливает вызовы, код остаётся читаемым. WebFlux имеет смысл, если ответ нужно стримить |
