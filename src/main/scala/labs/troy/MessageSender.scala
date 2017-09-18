package labs.troy

import model.SessionData

trait MessageSender {
  def send(messages: Seq[SessionData]): Unit
}
