
# Курсовой проект.

Тема: **StopLoss, TakeProfit для инвестиций**

```
 - Предполагает погружение в предметную область
 - Нужно иметь брокерский счет в Тинькофф.Инвестиции (или завести счет там)
 - Разработка алгоритма StopLoss/TakeProfit
 - Интеграция с OpenApi Тинькофф.Инвестиции (https://tinkoffcreditsystems.github.io/invest-openapi/)
 - Уведомление пользователей через telegram
```

# Описание проекта

Простой бот для взаимодействия с Тинькофф Инвестициями с возможностью выполнения заявок на покупку акций с заданными значениями StopLoss и TakeProfit по наступлению которых будет сделана заявка на продажу.

# Стек используемых технологий

* cats
* doobie - для работы с базой данных
* postgres - база данных
* http4s - для выполнения http-запросов
* circe - энкодер/декодер json
* telegramium - для телеграм бота

# Как работает приложение

В первую очередь это бот. 
Для взаимодействия с ботом ему нужно написать сообщение, бот выполнит некоторые действия и отправит ответ. 
На данный момент поддерживается некоторый список базовых команд.

* [x] `/portfolio` - Портфель
* [x] `/etfs` - Получение списка ETF
* [x] `/currencies` - Получение списка валютных пар
* [x] `/orderbook.figi.depth` - Получение стакана по `FIGI`
* [x] `/cancelOrder.orderId` - Отмена заявки по `OrderId`
* [x] `/limitOrderBuy.figi.lots.price` - Лимитная заявка на покупку
* [x] `/limitOrderSell.figi.lots.price` - Лимитная заявка на продажу
* [x] `/marketOrderBuy.figi.lots` - Рыночная заявка на покупку
* [x] `/marketOrderSell.figi.lots` - Рыночная заявка на продажу

И дополнительные команды.

* [x] `/marketOrderBuy.figi.lots.stoploss.takeprofit` - Рыночная заявка на покупку с указанными значениями StopLoss и TakeProfit.   
* [x] `/activeOperations` - Список активных операций
* [x] `/stopOperations` - Отменить все активные операции

![](https://habrastorage.org/webt/8q/uw/z4/8quwz4blc-4rnl9o2swrmhlbv-c.png)

# Из каких компонентов состоит

<!--
@startuml Diagram

title Component Diagram

package "tinvest-bot" {
  component [Core] as core
  component [TgBot] as tgBot
  component [NotificationRepo] as notificationRepo
  component [Notifier] as notifier
  component [DbAccess] as dbAccess
  component [WSHandler] as wsHandler
}

package "tinvest4s" {
  component [WebSocket Client] as streamingClient
  component [REST Client] as restClient
}

note left of tinvest4s
  Отдельный проект
  для взаимодействия
  с OpenApi
end note


note right of core
  Обрабатывает сообщения пользователя,
  выполняет определенные действия,
  формирует ответы
end note

note left of notifier
  Выгружает уведомления
  из NotificationRepo
  и отправляет пользователям
end note

note right of wsHandler
  Получает Streaming события
  от WebSocket-клиента
  и выполняет обработку
  (тут реализованы StopLoss и TakeProfit)
end note

database "PostgreSQL" {
  [candles]
  [operations]
  [notifications]
}

cloud {
  [Telegram]
}

cloud {
  [Тинькофф.Инвестиции]
}

tinvest4s -up-> Тинькофф.Инвестиции
tgBot -up-> Telegram

core -up-> restClient
core -up-> streamingClient
core .down.> dbAccess
wsHandler .down.> dbAccess
wsHandler .left.> notificationRepo : push

notificationRepo -down-> dbAccess
notifier -down-> notificationRepo : pull()
notifier -up-> tgBot : send()

dbAccess -down-> PostgreSQL
streamingClient -right-> wsHandler : handle

tgBot -up-> core : handleTgMessage
@enduml
-->

![](Diagram.svg)

# Демонстрация работы приложения

# Интересные моменты в процессе разработки
