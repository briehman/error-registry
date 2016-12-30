package com.briehman.errorregistry.dispatcher

import akka.actor.{Actor, ActorSystem, Props}
import com.briehman.errorregistry.interactor.ReceiveErrorInteractor
import com.briehman.errorregistry.message.AppErrorMessage
import com.briehman.errorregistry.web.{CustomTimestampSerializer, DateSerializer, UriSerializer}
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.{Amqp, ConnectionOwner, Consumer}
import com.rabbitmq.client.ConnectionFactory
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats, _}

import scala.concurrent.duration._

class RabbitMqReceiveErrorDispatcher(system: ActorSystem,
                                     connFactory: ConnectionFactory,
                                     receiveInteractor: ReceiveErrorInteractor) {
  private val ERROR_EXCHANGE: String = "app_error"

  implicit val jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  private val conn = system.actorOf(ConnectionOwner.props(connFactory, 1 second))

  // create an actor that will receive AMQP deliveries
  private val errorExchangeParameters = ExchangeParameters(ERROR_EXCHANGE, passive = false, "fanout", durable = true, autodelete = false)

  private val listener = system.actorOf(Props(new Actor {
    def receive = {
      case Delivery(consumerTag, envelope, properties, body) =>
        val bodyJson = parse(new String(body))
        val errorMessage = bodyJson.extract[AppErrorMessage]
        sender ! Ack(envelope.getDeliveryTag)
        receiveInteractor.receiveError(errorMessage)
    }
  }))

  private val consumer = ConnectionOwner.createChildActor(conn, Consumer.props(
    listener = Some(listener),
    init = List(
      DeclareExchange(errorExchangeParameters),
      AddBinding(
        Binding(
          errorExchangeParameters,
          QueueParameters("", passive = false, durable = false, exclusive = false, autodelete = true),
          ""
        )
      )
    ),
    channelParams = None,
    autoack = false))

  def start(): Unit = {
    // wait till everyone is actually connected to the broker
    Amqp.waitForConnection(system, consumer).await()
  }

  def stop(): Unit = {
    system.stop(listener)
    system.stop(conn)
  }
}
