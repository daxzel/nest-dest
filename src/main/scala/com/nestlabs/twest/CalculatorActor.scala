package com.nestlabs.twest

import akka.actor.{Actor, Props}

/**
  * Created by Tsarevskiy
  */
object CalculatorActor {
  def props(): Props = Props(new CalculatorActor())
}

class CalculatorActor() extends Actor {

  def receive = {
    case ThermostatStateUpdate(location: String, statusType: String, status: String) =>
      println("hey")
  }

}