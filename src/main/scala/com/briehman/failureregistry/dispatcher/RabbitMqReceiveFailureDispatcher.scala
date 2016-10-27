package com.briehman.failureregistry.dispatcher

import akka.actor.{Actor, ActorSystem, Props}
import com.briehman.failureregistry.interactor.ReceiveFailureInteractor
import com.briehman.failureregistry.message.FailureMessage
import com.briehman.failureregistry.web.{CustomTimestampSerializer, DateSerializer, UriSerializer}
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.{Amqp, ConnectionOwner, Consumer}
import com.rabbitmq.client.ConnectionFactory
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats, _}

import scala.concurrent.duration._

class RabbitMqReceiveFailureDispatcher(system: ActorSystem,
                                       connFactory: ConnectionFactory,
                                       receiveInteractor: ReceiveFailureInteractor) {
  private val FAILURE_EXCHANGE: String = "failure"

  implicit val jsonFormats: Formats = DefaultFormats + DateSerializer + CustomTimestampSerializer + UriSerializer

  private val conn = system.actorOf(ConnectionOwner.props(connFactory, 1 second))

  // create an actor that will receive AMQP deliveries
  private val failureExchangeParameters = ExchangeParameters(FAILURE_EXCHANGE, passive = false, "fanout", durable = true, autodelete = false)

  private val listener = system.actorOf(Props(new Actor {
    def receive = {
      case Delivery(consumerTag, envelope, properties, body) =>
        val bodyJson = parse(new String(body))
        val failureMessage = bodyJson.extract[FailureMessage]
        sender ! Ack(envelope.getDeliveryTag)
        receiveInteractor.receiveFailure(failureMessage)
    }
  }))

  private val consumer = ConnectionOwner.createChildActor(conn, Consumer.props(
    listener = Some(listener),
    init = List(
      DeclareExchange(failureExchangeParameters),
      AddBinding(
        Binding(
          failureExchangeParameters,
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
