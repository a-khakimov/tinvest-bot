package github.ainr.tinvest4s.websocket.request


case class CandleRequest(event: String, figi: String, interval: String, request_id: Option[String] = None)
  extends TInvestWSRequest