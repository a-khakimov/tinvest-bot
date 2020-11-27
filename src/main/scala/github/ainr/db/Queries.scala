package github.ainr.db

import doobie.implicits.toSqlInterpolator

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
}
