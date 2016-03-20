package com.daxzel.nestdest

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.routing.{ActorRefRoutee, Routee}
import akka.stream._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory

class WebServer(val context: akka.actor.ActorContext, mainActor: ActorRef) {
  val config = ConfigFactory.load()
  val webPageActor = context.actorOf(RouterActor.props())

  val greeterWebSocketService: Graph[FlowShape[Message, Message], Any]
            = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val source = Source.actorPublisher[SummaryChargesUpdated](WebPageActor.props(webPageActor))
      .map(update => TextMessage(update.cost.toString))

    val statsSource = builder.add(source)
    val webSocketInlet = builder.add(Flow[Message].collect[TextMessage]({
      case message: TextMessage => message
    }))

    val merge = builder.add(Merge[TextMessage](2))

    val map = builder.add(Flow[TextMessage]
      .map[TextMessage]({ x: TextMessage => x }))

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
            mainActor ! UtilitiesCostUpdate(heatingPrice, HeaterState.Heating)
            mainActor ! UtilitiesCostUpdate(coolingPrice, HeaterState.Cooling)
            complete("complete")
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

object WebPageActor {
  def props(router: ActorRef): Props = Props(new WebPageActor(router))
}

class WebPageActor(router: ActorRef) extends ActorPublisher[SummaryChargesUpdated] {

  override def preStart() {
    router ! AddRoutee(ActorRefRoutee(self))
  }

  override def postStop(): Unit = {
    router ! RemoveRoutee(ActorRefRoutee(self))
  }


  def receive = {
    case summaryChargesUpdated: SummaryChargesUpdated =>
      onNext(summaryChargesUpdated)
  }
}

object RouterActor {
  def props(): Props = Props(new RouterActor())
}

class RouterActor extends Actor {
  var webPageActors = Set[Routee]()

  def receive = {
    case ar: AddRoutee => webPageActors = webPageActors + ar.actor
    case rr: RemoveRoutee => webPageActors = webPageActors - rr.actor
    case msg => webPageActors.foreach(_.send(msg, sender))
  }
}

case class AddRoutee(actor: Routee)

case class RemoveRoutee(actor: Routee)
