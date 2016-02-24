package com.daxzel.nestdest

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main {
  def main(args: Array[String]) {

    val conf = ConfigFactory.load()
    val firebaseURL = conf.getString("firebase")
    val accessToken = conf.getString("accessToken")

    val system = ActorSystem("nestCostCalculation")
    system.actorOf(Props(new MainActor(firebaseURL, accessToken)))
  }
}
