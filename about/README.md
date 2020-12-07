
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

![Components](http://www.plantuml.com/plantuml/png/ZLN1Jjj04BtxArQSGAevzTG3WaAb7cXBIIGEY8FZR8o5uxMyctAeAa62MbLKYjHJJrJg5uIKb118-WkxVzIt4ziuXbQzPMVUl7blTjPPbyfBLQSTEIfK4MURefs8cCUAROLUa7fjnqay_yWBE5jIOVoEI-KsX5fYxnt6_0AylobIVi0yYLZA5rFruBbGb5FsrsBojL1XA_Gz5OfuboU2S74b-5W9JtCe7HOXMuqDt-TI4gIP7HOXUxMNNjoCCffZcPsS3myDFvKFxExnHatuHronpIX4X4YaIhdN3kE0OXMZkozgzJ8u1N66GqlOu2pYBSL4YnLjmQ0lJ5TVwP6-DL_qr9ph6wP_cnCzGsXYkXPnXLoVwQ6-rWCzrdV8sO8RSuhIeHwRFb3cbBr9UBoHX0wFcypsSxAsQHWSshxvrUaBVOaE0tsf1wQBbaFy7fakE40zG_8pwAUM65eGW3JqdW47F4djFx4yGzHI7iBxn803n0D2ZeZ5UXgnYcAgDsTw1l0TpYUcbzKJO6Hb5JugqojjXFuAyfzK_IltqDD3wZy3CId81eJ9NRFgDbASkfBk0MflSoj_SQxlAiAA-HPBHjA-KtdFVCg4rVBbmPXfoETW96dKdnKRv-e9X8-jT2069F7nMUVNIFedfWVeCYRNGv8kOM1vSlts8Y129Dj2IajPzuxuJYfQeLefEMfwocjuahEb7I5LaFBQs-tvwzZtoPmyiBz5mbEQulnOVj28OE7zI7IQyxewZxZzqbJZ-WVKJJ7X2GoScRDL_KrFwVPFQJGVaHvJKV5WcDj9t3NsNvKEVOsoYbm318YKpuEb9rgELbyvfLQRuZXUNRl_rXHt_S-KtLcagjyxzemb7NdeE0yIhYLpIsJvjXUPHxcYQ7cb12KVP1zPYSj5riaf2wRx2tOgdfbBEmrCuGL4X_JBMPWh3ITFrODNuFS2xgoZ9_vp_W00)

# Демонстрация работы приложения

# Интересные моменты в процессе разработки
