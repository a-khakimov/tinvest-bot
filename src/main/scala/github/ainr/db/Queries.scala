package github.ainr.db

import doobie.implicits.toSqlInterpolator
import github.ainr.tinvest4s.websocket.response.CandlePayload

// https://medium.com/rahasak/doobie-and-cats-effects-d01230be5c38

object Queries {

  def getById(id: Int): doobie.Query0[String] = {
    sql"""
         |SELECT name FROM users
         |WHERE id = $id
       """
      .stripMargin
      .query[String]
  }

  def insertCandle(c: CandlePayload): doobie.Update0 = {
    sql"""
         |INSERT INTO candles ( time, interval, figi, open, close, hight, low, volume )
         |VALUES (${c.time}, ${c.interval}, ${c.figi}, ${c.o}, ${c.c}, ${c.h}, ${c.l}, ${c.v})
       """
      .stripMargin
      .update
  }
}

