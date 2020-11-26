package github.ainr.tinvest4s.websocket.response


case class WrongResponse(event: String,
                         time: String, // RFC3339Nano
                         playload: WrongResponsePlayload)

case class WrongResponsePlayload(error: String,
                                 request_id: Option[String])
