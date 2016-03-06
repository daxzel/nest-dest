package com.daxzel.nestdest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Flow}
import com.typesafe.config.ConfigFactory

class WebServer(val context: akka.actor.ActorContext) {
  val config = ConfigFactory.load()

  val greeterWebSocketService =
    Flow[Message].collect {
      case tm: TextMessage ⇒ TextMessage(Source.single("Hello ") ++ tm.textStream)
    }

  val routes = {
    get {
      pathSingleSlash {
        getFromResource("web-app/index.html")
      } ~
        pathPrefix("resources") {
          getFromResourceDirectory("web-app/resources")
        } ~
        pathPrefix("api" / "updateSettings") {
          parameters(('heatingPrice.as[Int], 'coolingPrice.as[Int])) { (heatingPrice, coolingPrice) => {
            println(heatingPrice)
            println(coolingPrice)
            complete("test" + println("test"))
          }
          }
        }

      pathPrefix("api" / "websocket") {
        handleWebsocketMessages(greeterWebSocketService)
      }
    }
  }

  implicit val system: ActorSystem = context.system
  implicit val materializer = ActorMaterializer()

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}