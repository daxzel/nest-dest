package com.daxzel.nestdest

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

trait Service {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter

  val routes = {
    logRequestResult("akka-http-nest-dest") {
      pathPrefix("ip") {
        (get & path(Segment)) { ip =>
          complete {
            null
          }
        }
      }
    }
  }
}

object WebPageActor {
  def props(): Props = Props(new WebPageActor())
}

class WebPageActor extends Actor with Service {

  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  def receive = {
    case _ => null
  }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}