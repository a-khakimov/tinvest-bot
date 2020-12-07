![Scala CI](https://github.com/a-khakimov/tinvest-bot/workflows/Scala%20CI/badge.svg?branch=main)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=a-khakimov_tinvest-bot&metric=ncloc)](https://sonarcloud.io/dashboard?id=a-khakimov_tinvest-bot)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=a-khakimov_tinvest-bot&metric=code_smells)](https://sonarcloud.io/dashboard?id=a-khakimov_tinvest-bot)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=a-khakimov_tinvest-bot&metric=coverage)](https://sonarcloud.io/dashboard?id=a-khakimov_tinvest-bot)

# tinvest-bot

Бот, для автоматизации выполнения заявок на покупки и продажи акций по заранее заданным значениям StopLoss/TakeProfit.

## Стек используемых технологий

* cats
* doobie
* postgres
* http4s
* circe
* telegramium

## Как собрать и запустить проект

* Нужно иметь брокерский счет в [Тинькофф.Инвестиции](https://www.tinkoff.ru/invest/), если есть, то получить в [личном кабинете](https://www.tinkoff.ru/invest/) токен для авторизации 
* Зарегистрировать telegram бота и получить токен для авторизации (для торговли на бирже или в песочнице)
* Подготовить базу данных - создать пользователя и таблицы (*TODO: Следует автоматизировать этот этап*)

```sql
CREATE TABLE candles (id SERIAL, time TEXT, interval TEXT, figi TEXT, open REAL, close REAL, hight REAL, low REAL, volume REAL);

CREATE TABLE operations (id SERIAL, figi TEXT, stopLoss REAL, takeProfit REAL,
                         time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         operationStatus TEXT, orderId TEXT, orderStatus TEXT, orderOperation TEXT,
                         requestedLots INT, executedLots INT, tgUserId bigint);

CREATE TABLE notifications (id SERIAL, userId INT, message TEXT);
```
* В файле конфигурации `application.conf` задать токены для авторизации telegram-бота и в OpenApi Тинькофф.Инвестиций, и имя/пароль для подключения к базе данных
* Запустить проект 
```bash
$ sbt run
```  

## Требования

Бот должен:
* Выполнять мониторинг стоимости заданных пользователем акций
* Выполнять заявки для покупки или продажи акций по значениям StopLoss и TakeProfit
* Уведомлять пользователя через telegram о выполняемых действиях

## Функциональность

И так... Тут нужно подумать!

Во-первых, бот должен знать что происходит со стоимостью акций, а значит должен уметь получать эту информацию.

Так же бот должен как то реагировать на события StopLoss и TakeProfit, а именно уметь делать заявки о покупке или продаже акций.

На данном этапе можно реализовать StopLoss и TakeProfit для торговли в лонг. В таком случае, события событие StopLoss будет происходить при падении стоимости акции до заданного уровня и, соответственно, событие TakeProfit будет происходить при росте стоимости акции до заданного уровня.

## Взаимодействие с пользователем

Для взаимодействия с пользователем можно рассмотреть два варианта:
* Первый способ взаимодействия можно сделать через http-api
* Второй способ - через telegram-бота (уведомления и команды)

На данном этапе, думаю, стоит реализовать второй способ, поскольку он проще и быстрее для тестирования и проверки.

## Взаимодействие через telegram

### Базовые команды

* [x] `/portfolio` - Портфель
* [x] `/etfs` - Получение списка ETF
* [x] `/currencies` - Получение списка валютных пар
* [x] `/orderbook.figi.depth` - Получение стакана по `FIGI`
* [x] `/cancelOrder.orderId` - Отмена заявки по `OrderId`
* [x] `/limitOrderBuy.figi.lots.price` - Лимитная заявка на покупку
* [x] `/limitOrderSell.figi.lots.price` - Лимитная заявка на продажу
* [x] `/marketOrderBuy.figi.lots` - Рыночная заявка на покупку
* [x] `/marketOrderSell.figi.lots` - Рыночная заявка на продажу

### Дополнительные команды

* [x] `/marketOrderBuy.figi.lots.stoploss.takeprofit` - Рыночная заявка на покупку с указанными значениями `stoploss` и `takeprofit`. `stoploss` и `takeprofit` имеют тип `Double`. Например, команда `/marketOrderBuy.BBG009S39JX6.10.100,01.200,02` выполнит покупку 10 лотов акций `BBG009S39JX6` со значением `stoploss=10.100` и `takeprofit=200,02`. При этом значение stoploss не должна превышать значение текущей стоимости акции и, соответственно, значение `takeprofit` должна превышать текущую стоимость акции. Команда вернет информацию о покупке и id операции.   
* [x] `/activeOperations` - Список активных операций
* [x] `/stopOperations` - Отменить все активные операции
* [ ] `/completedOperations` - Получить список последних завершенных операций
* [ ] `/stopOperations.id` - Отменить операцию по id

Под операцией подразумевается процесс подписки на отслеживание стоимости акции по `figi`, которая завершится по достижению значений `stoploss` и `takeprofit` или по команде `stop`.

## Дополнительно

Модуль **tinvest4s** для работы с [api тинькофф инвестиций](https://tinkoffcreditsystems.github.io/invest-openapi/) переехал в отдельный репозиторий [tinvest4s](https://github.com/a-khakimov/tinvest4s)
