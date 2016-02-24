package com.daxzel.nestdest

import akka.actor.Actor
import com.daxzel.nestdest.HeaterState.HeaterState

case class StructureUpdate(name: String, state: String)

case class ThermostatHeaterStateUpdate(id: String, statusType: HeaterState)

object HeaterState extends Enumeration {
  type HeaterState = Value
  val Off, Heating, Cooling = Value

  def get(state: String): HeaterState = state match {
    case "off" => Off
    case "heating" => Heating
    case "cooling" => Cooling
  }
}

class MainActor(firebaseURL: String, accessToken: String) extends Actor {
  val calculatorActor = context.actorOf(CalculatorActor.props())
  val nestActor = context.actorOf(NestActor.props(accessToken, firebaseURL))

  def receive = {
    case message => println("Received " + message)
  }
}
