package labs.troy

import model.SessionData

import scala.concurrent.Future

trait MessageTransformer {
  def transformMessage(message: SessionData): Future[SessionData]
}
