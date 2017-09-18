package labs.troy

import akka.actor.{ActorLogging, ReceiveTimeout}
import akka.persistence.PersistentActor

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

import model.SessionData

class SessionDataBufferedSender(
    maxBufferedMessages: Int,
    maxBufferedBytes: Long,
    maxIdleDuration: FiniteDuration) extends PersistentActor with ActorLogging {
  this: MessageSender with MessageTransformer =>

  implicit val ec = context.dispatcher

  val bufferedMessages = mutable.Buffer.empty[SessionData]
  var bufferedBytes: Long = 0
  var hasUpdates: Boolean = false

  context.setReceiveTimeout(maxIdleDuration)

  case class TransformedData(data: SessionData)
  case class UpdatedState(buffer: mutable.Buffer[SessionData])

  override def persistenceId: String = self.path.name

  override def receiveCommand: Receive = {
    case data: SessionData =>
      transformMessage(data)
        .onComplete {
          case Success(transformed) =>
            self ! TransformedData(transformed)

          case Failure(er) =>
            log.error(s"Unable to transform message $data, re-schedule", er)
            self ! data
        }

    case TransformedData(data) =>
      bufferedMessages += data
      bufferedBytes += data.sizeInBytes
      hasUpdates = true

      if (bufferedMessages.size == maxBufferedMessages || bufferedBytes >= maxBufferedBytes) {
        flush()
      }

    case ReceiveTimeout =>
      if (hasUpdates) {
        hasUpdates = false
      } else if (bufferedMessages.nonEmpty) {
        flush()
      }
  }

  override def receiveRecover: Receive = {
    case UpdatedState(buffer) =>
      bufferedMessages.prependAll(buffer)
      bufferedBytes += buffer.map(_.sizeInBytes).sum
      hasUpdates = true
  }

  private def flush() {
    log.debug("Flushing buffered messages")

    send(bufferedMessages)

    bufferedMessages.clear()
    bufferedBytes = 0
  }

}
