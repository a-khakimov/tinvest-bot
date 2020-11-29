package github.ainr.tinvest4s.models

case class TInvestError(trackingId: String, status: String, payload: Payload)
case class Payload(message: Option[String], code: Option[String])

case class EmptyPayload()
case class EmptyResponse(trackingId: String, status: String, payload: EmptyPayload)
