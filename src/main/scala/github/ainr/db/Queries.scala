package github.ainr.db

import doobie.implicits.toSqlInterpolator
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.domain.{BotOperation, Notification}
import github.ainr.tinvest4s.websocket.response.CandlePayload

// https://medium.com/rahasak/doobie-and-cats-effects-d01230be5c38

/*
* Список запросов для создания таблиц:
* CREATE TABLE candles (id SERIAL, time TEXT, interval TEXT, figi TEXT, open REAL, close REAL, hight REAL, low REAL, volume REAL);
*
* CREATE TABLE operations (id SERIAL, figi TEXT, stopLoss REAL, takeProfit REAL,
*                          time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
*                          operationStatus TEXT, orderId TEXT, orderStatus TEXT, orderOperation TEXT,
*                          requestedLots INT, executedLots INT, tgUserId bigint);
* operationStatus = { "Active", "Stop", "Completed" }
*
* CREATE TABLE notifications (id SERIAL, userId INT, message TEXT);
*
* TODO: довести до ума базу данных
* */


object Queries {

  def insertCandle(c: CandlePayload): doobie.Update0 = {
    sql"""
         |INSERT INTO candles ( time, interval, figi, open, close, hight, low, volume )
         |VALUES (${c.time}, ${c.interval}, ${c.figi}, ${c.o}, ${c.c}, ${c.h}, ${c.l}, ${c.v})
       """
      .stripMargin
      .update
  }

  def insertOperation(o: BotOperation): doobie.Update0 = {
    sql"""
         |INSERT INTO operations ( figi, stopLoss, takeProfit, operationStatus, orderId, orderStatus, orderOperation, requestedLots, executedLots, tgUserId )
         |VALUES (${o.figi}, ${o.stopLoss}, ${o.takeProfit}, ${o.operationStatus}, ${o.orderId}, ${o.orderStatus}, ${o.orderOperation}, ${o.requestedLots}, ${o.executedLots}, ${o.tgUserId})
       """
      .stripMargin
      .update
  }

  def updateOperationStatus(id: Int, status: OperationStatus): doobie.Update0 = {
    sql"""
         |UPDATE operations SET operationStatus = $status WHERE ID = $id;
       """
      .stripMargin
      .update
  }

  def getOperationsByStatus(operationStatus: OperationStatus): doobie.ConnectionIO[List[BotOperation]] = {
    sql"""
         |SELECT id, figi, stopLoss, takeProfit, operationStatus, orderId, orderStatus, orderOperation, requestedLots, executedLots, tgUserId
         |FROM operations
         |WHERE operationStatus = $operationStatus
       """
      .stripMargin
      .query[BotOperation].to[List]
  }

  def getNotifications: doobie.ConnectionIO[List[Notification]] = {
    sql"""
         |DELETE FROM notifications RETURNING userId, message
       """
      .stripMargin
      .query[Notification].to[List]
  }

  def insertNotification(notification: Notification): doobie.Update0 = {
    sql"""
         |INSERT INTO notifications (userId, message) VALUES (${notification.userId}, ${notification.message})
       """
      .stripMargin
      .update
  }
}

