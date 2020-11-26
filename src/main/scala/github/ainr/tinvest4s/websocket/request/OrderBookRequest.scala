package github.ainr.tinvest4s.websocket.request

case class OrderBookRequest(event: String, figi: String, depth: Int, request_id: Option[String] = None)
  extends TInvestWSRequest
