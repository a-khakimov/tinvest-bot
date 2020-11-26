package github.ainr.tinvest4s.websocket.request

case class InstrumentInfoRequest(event: String, figi: String, request_id: Option[String] = None)
  extends TInvestWSRequest
