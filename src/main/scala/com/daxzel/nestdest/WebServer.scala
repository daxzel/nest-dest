package com.daxzel.nestdest

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream._
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration

class WebServer(val context: akka.actor.ActorContext) {
  val config = ConfigFactory.load()

  val greeterWebSocketService: Graph[FlowShape[Message, Message], Any]
  = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val source = Source
      .tick[TextMessage](FiniteDuration(5, TimeUnit.SECONDS), FiniteDuration(5, TimeUnit.SECONDS), TextMessage("2324"))

    val statsSource = builder.add(source)
    val webSocketInlet = builder.add(Flow[Message].collect[TextMessage]({
      case message: TextMessage => message
    }))

    val merge = builder.add(Merge[TextMessage](2))

    val map = builder.add(Flow[TextMessage]
      .map[TextMessage]({ x: TextMessage => x}))

    statsSource ~> merge
    webSocketInlet ~> merge ~> map
    FlowShape[Message, TextMessage](webSocketInlet.in, map.out)
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
        } ~ pathPrefix("api" / "websocket") {
          handleWebsocketMessages(Flow.fromGraph(greeterWebSocketService))
      }
    }
  }

  implicit val system: ActorSystem = context.system
  implicit val materializer = ActorMaterializer()

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}