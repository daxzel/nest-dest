package com.daxzel.nestdest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

class WebServer(val context: akka.actor.ActorContext) {
  val config = ConfigFactory.load()

  val routes = {
    get {
      pathSingleSlash {
        getFromResource("web-app/index.html")
      } ~
        pathPrefix("resources") {
          getFromResourceDirectory("web-app/resources")
        } ~
        pathPrefix("api/updateSettings") {
          parameters(('heatingPrice.as[Int], 'coolingPrice.as[Int])) { (heatingPrice, coolingPrice) => {
            println(heatingPrice)
            println(coolingPrice)
            complete("test" + println("test"))
          }
          }
        }
      //        pathPrefix("api/updateSettings" / IntNumber / IntNumber) { (heatingPrice, coolingPrice) => {
      //          println(heatingPrice)
      //          println(coolingPrice)
      //          complete("test" + println("test"))
      //        }
    }
  }

  implicit val system: ActorSystem = context.system
  implicit val materializer = ActorMaterializer()

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}