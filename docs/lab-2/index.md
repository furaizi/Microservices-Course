# Практическое задание №2. Реализация API Gateway и Асинхронного Взаимодействия

## 1. Введение и Цель

Данное задание направлено на практическое закрепление знаний, полученных в рамках лекций по теме "Взаимодействие микросервисов". Целью является реализация двух ключевых паттернов:

1.  **API Gateway:** Централизованная точка входа для внешних запросов к системе микросервисов с использованием **Spring Cloud Gateway**.
2.  **Асинхронное взаимодействие:** Организация обмена сообщениями между микросервисами с использованием брокера сообщений (**Apache Kafka** *или* **RabbitMQ**).

Выполнение задания позволит студентам получить практический опыт настройки и использования современных инструментов для построения распределенных систем.

## 2. Обзор Системы (Пример)

Для выполнения задания будем исходить из следующей упрощенной архитектуры:

* **`order-service` (Сервис Заказов):** Микросервис, отвечающий за создание и управление заказами. Предоставляет REST эндпоинт для создания заказа (например, `POST /orders`).
* **`notification-service` (Сервис Уведомлений):** Микросервис, отвечающий за отправку (имитацию отправки) уведомлений пользователям. Может иметь REST эндпоинт для проверки статуса (например, `GET /notifications/status`) и должен уметь принимать сообщения асинхронно.
* **`api-gateway` (Шлюз API):** Новый сервис, который будет служить единой точкой входа.
* **Брокер сообщений:** Kafka или RabbitMQ для асинхронной передачи информации о создании заказа от `order-service` к `notification-service`.

## 3. Требования к Реализации

### 3.1. Реализация Notification Service

**Основная Роль `notification-service`:**


Этот сервис выступает как централизованный компонент для **отправки (или имитации отправки)** различных уведомлений пользователям системы. Он инкапсулирует логику, связанную с форматом уведомлений, выбором канала доставки и взаимодействием с внешними провайдерами (email, SMS, push и т.д.). Важно, что он **реагирует на события**, произошедшие в других частях системы (в нашем случае, на создание заказа), получая информацию о них асинхронно.

**Функциональные Требования к `notification-service` для Задания:**

1.  **Асинхронный Прием Событий:**
    * **Задача:** Слушать сообщения в определенной очереди (RabbitMQ) или топике (Kafka), куда `order-service` отправляет информацию о *создании нового заказа*.
    * **Входные данные (из сообщения):** Сообщение должно содержать как минимум `orderId`. В идеале, для упрощения, оно также должно содержать `userId` пользователя, сделавшего заказ. Пример JSON сообщения:
    * **Реализация:** Использовать аннотации `@KafkaListener` или `@RabbitListener` для настройки метода-обработчика.
```json
{  
    "orderId": "ord-12345abc",  
    "userId": "user-67890def",  
    "timestamp": "2025-04-03T23:15:00Z"  
    // Можно добавить и другие поля, если order-service может их легко предоставить  
}  
```

2.  **Получение Дополнительной Информации (Обогащение Контекста):**
    * **Задача:** Чтобы отправить осмысленное уведомление, часто недостаточно данных только из события. Сервису нужно получить контактную информацию пользователя.
    * **Взаимодействие:** После получения сообщения с `userId`, `notification-service` должен **выполнить синхронный запрос (например, REST вызов)** к вашему существующему `user-service`.
    * **Запрос к `user-service`:** Обратиться к эндпоинту `user-service` (например, `GET /users/{userId}/profile` или `GET /users/{userId}/contact-info`), чтобы получить данные пользователя, в первую очередь его **email** (или другой контакт, который вы хотите использовать для имитации).
    * **Обработка ответа/ошибок:** Обработать ответ от `user-service`. Если `user-service` недоступен или не нашел пользователя, залогировать ошибку (для задания этого достаточно).

3.  **Формирование Уведомления:**
    * **Задача:** Собрать текст (или структуру) уведомления на основе полученных данных.
    * **Логика:** Сформировать строку сообщения, например: `"Уважаемый пользователь [Имя пользователя из user-service], ваш заказ с ID [orderId из сообщения] успешно создан и принят в обработку."` (Имя пользователя можно опустить, если `user-service` отдает только email).

4.  **Выбор Канала и Имитация Отправки:**
    * **Задача:** Определить канал отправки (для задания достаточно одного, например, Email) и выполнить саму "отправку".
    * **Имитация:** **Ключевой момент для упрощения задания:** Не нужно интегрироваться с реальными сервисами отправки email/SMS. Достаточно **вывести в лог** информацию о том, какое уведомление и куда было бы отправлено.
        * Пример лога: `INFO: [NotificationService] Simulating sending EMAIL to [user_email@example.com]. Content: "Уважаемый пользователь ..., ваш заказ с ID ord-12345abc успешно создан..."`
    * **Реализация:** Просто использовать стандартный логгер (SLF4j/Logback) для вывода этой информации.

5.  **Предоставление Базового REST API (Опционально, но полезно):**
    * **Задача:** Иметь простой способ проверить, что сервис работает.
    * **Эндпоинт:** Реализовать простой `GET` эндпоинт, например, `/notifications/health` или `/notifications/status`, который возвращает статус `{"status": "UP"}` или что-то подобное. Это не связано напрямую с логикой обработки уведомлений, но полезно для мониторинга.

**Что НЕ требуется от `notification-service` в рамках задания:**

* Реальная интеграция с email/SMS/push сервисами.
* Хранение истории уведомлений в базе данных.
* Сложная логика выбора канала (предпочтения пользователя).
* Обработка шаблонов уведомлений.
* Повторные попытки отправки уведомлений (хотя в реальной системе это важно).

**Техническая Реализация (Кратко):**

* Использовать Spring Boot.
* Настроить `RestTemplate` или `WebClient` (рекомендуется) для синхронных вызовов к `user-service`.
* Настроить консьюмер Kafka или RabbitMQ (как описано в ТЗ).
* Реализовать `@Service` или `@Component`, содержащий основную логику (получение сообщения -> вызов user-service -> логирование имитации отправки).



### 3.2. Реализация API Gateway (Spring Cloud Gateway)

1.  **Создание проекта:** Создать новый Spring Boot проект с использованием Maven или Gradle для `api-gateway`.
2.  **Добавление зависимостей:** Подключить необходимую зависимость `spring-cloud-starter-gateway`. Убедиться в наличии `spring-boot-starter-webflux`, так как Spring Cloud Gateway основан на нем.
3.  **Конфигурация:** Настроить `api-gateway` в файле `application.yml` (или `.properties`):
    * Указать порт, на котором будет работать шлюз (например, 8080).
    * Настроить **маршруты (routes)** для перенаправления запросов к существующим микросервисам (`order-service` и `notification-service`).
        * Пример: Запросы, приходящие на шлюз по пути `/api/orders/**`, должны перенаправляться на `order-service` (например, работающий на `http://localhost:8081`).
        * Пример: Запросы, приходящие на шлюз по пути `/api/notifications/**`, должны перенаправляться на `notification-service` (например, работающий на `http://localhost:8082`).
    * Использовать **предикаты (predicates)** для определения условий маршрутизации (например, `Path`).
    * Использовать **фильтры (filters)** для модификации запросов/ответов (например, `RewritePath` для удаления префикса `/api/orders` перед перенаправлением запроса).
4.  **Тестирование:** Запустить все сервисы (`order-service`, `notification-service`, `api-gateway`) и проверить корректность маршрутизации через шлюз с использованием HTTP-клиента (Postman, curl).

### 3.3. Реализация Асинхронного Взаимодействия (Kafka ИЛИ RabbitMQ)

**Необходимо выбрать и реализовать ОДИН из следующих вариантов (A или B).**

**Сценарий:** При успешном создании нового заказа в `order-service` (через REST вызов, приходящий через API Gateway), `order-service` должен асинхронно отправить сообщение брокеру. `notification-service` должен получить это сообщение и обработать его (например, вывести в лог информацию о необходимости отправить уведомление).

#### 3.3.A. Вариант с Apache Kafka

1.  **Настройка Kafka:** Подготовить `docker-compose.yml` для запуска Kafka и Zookeeper.
2.  **Зависимости:**
    * В `order-service` добавить зависимость `spring-kafka`.
    * В `notification-service` добавить зависимость `spring-kafka`.
3.  **Конфигурация:**
    * Настроить подключение к Kafka (адрес `bootstrap-servers`) в `application.yml` обоих сервисов.
    * Определить имя топика (например, `order_created_topic`).
4.  **Реализация Продюсера (`order-service`):**
    * Создать сервис или компонент, отвечающий за отправку сообщений.
    * Использовать `KafkaTemplate` для отправки сообщения в заданный топик после успешного создания заказа.
    * Сообщение должно содержать идентификатор заказа и, возможно, другую релевантную информацию (например, в формате JSON).
5.  **Реализация Консьюмера (`notification-service`):**
    * Создать компонент-слушатель (listener).
    * Использовать аннотацию `@KafkaListener` для подписки на заданный топик.
    * Реализовать логику обработки полученного сообщения (десериализация, логирование). Указать `groupId`.
6.  **Тестирование:** Запустить все компоненты (сервисы, Kafka, Zookeeper). Создать заказ через API Gateway. Убедиться, что `order-service` отправил сообщение, а `notification-service` его получил и обработал (проверить логи).

#### 3.3.B. Вариант с RabbitMQ

1.  **Настройка RabbitMQ:** Подготовить `docker-compose.yml` для запуска RabbitMQ.
2.  **Зависимости:**
    * В `order-service` добавить зависимость `spring-boot-starter-amqp`.
    * В `notification-service` добавить зависимость `spring-boot-starter-amqp`.
3.  **Конфигурация:**
    * Настроить подключение к RabbitMQ (адрес, порт, учетные данные) в `application.yml` обоих сервисов.
    * Определить имя обменника (exchange, например, `order_exchange`) и тип (например, `fanout` или `direct`).
    * Определить имя очереди (queue, например, `notification_queue`).
4.  **Реализация Продюсера (`order-service`):**
    * Создать сервис или компонент, отвечающий за отправку сообщений.
    * Использовать `RabbitTemplate` для отправки сообщения в заданный обменник (с указанием ключа маршрутизации, если используется не `fanout` exchange) после успешного создания заказа.
    * Сообщение должно содержать идентификатор заказа и, возможно, другую релевантную информацию (например, в формате JSON).
5.  **Реализация Консьюмера (`notification-service`):**
    * Объявить очередь и привязку (binding) к обменнику программно (используя `Queue`, `Exchange`, `Binding` бины) или через конфигурацию.
    * Создать компонент-слушатель.
    * Использовать аннотацию `@RabbitListener` для подписки на заданную очередь.
    * Реализовать логику обработки полученного сообщения (десериализация, логирование).
6.  **Тестирование:** Запустить все компоненты (сервисы, RabbitMQ). Создать заказ через API Gateway. Убедиться, что `order-service` отправил сообщение, а `notification-service` его получил и обработал (проверить логи, можно также посмотреть через UI RabbitMQ).


## 4. Требования к реализации

* **API Gateway:**
    * Корректная настройка проекта Spring Cloud Gateway.
    * Успешная маршрутизация запросов к двум или более нижестоящим сервисам.
    * Использование предикатов и фильтров для управления маршрутами.
* **Асинхронное Взаимодействие (выбранный вариант):**
    * Корректная настройка продюсера и отправка сообщения при наступлении события.
    * Корректная настройка консьюмера, получение и обработка сообщения.
    * Наличие конфигурации для запуска брокера через Docker Compose.
    * Обоснование выбора (если требуется) и понимание работы выбранного брокера.
* **Общие Критерии:**
    * Качество кода (читаемость, структура, комментарии).
    * Корректное использование конфигурационных файлов.
    * Наличие и полнота `README.md` и инструкций по запуску.
    * Работоспособность решения в соответствии с заданием.
