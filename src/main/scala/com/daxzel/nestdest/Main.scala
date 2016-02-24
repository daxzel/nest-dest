package com.daxzel.nestdest

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main {
  def main(args: Array[String]) {

    val conf = ConfigFactory.load()
    val firebaseURL = conf.getString("nest.firebase")
    val accessToken = conf.getString("nest.accessToken")

    val system = ActorSystem("UtilitiesCostCalculation")
    system.actorOf(Props(new MainActor(firebaseURL, accessToken)))
  }
}
