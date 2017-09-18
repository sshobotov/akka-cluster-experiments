package labs.troy

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

import scala.concurrent.Future
import scala.concurrent.duration._

import model.SessionData

object Application {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("sessions")

    val sender = new SessionDataBufferedSender(
      maxBufferedMessages = 10000,
      maxBufferedBytes    = 20 * 1024 * 1024,
      maxIdleDuration     = 10.minutes
    ) with MessageSender with MessageTransformer {

      override def send(messages: Seq[SessionData]): Unit = ???

      override def transformMessage(message: SessionData): Future[SessionData] = ???
    }
    val maxNumberOfShards = 1000

    ClusterSharding(system)
      .start(
        typeName    = "SessionDataBufferedSender",
        entityProps = Props(sender),
        settings    = ClusterShardingSettings(system),

        extractEntityId = {
          case payload @ SessionData(shopToken) => (shopToken, payload)
        },
        extractShardId  = {
          case SessionData(shopToken) =>
            (shopToken.hashCode % maxNumberOfShards).toString
        })
  }

}
