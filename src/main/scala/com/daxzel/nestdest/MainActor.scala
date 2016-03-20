package com.daxzel.nestdest

import akka.actor.Actor
import com.daxzel.nestdest.HeaterState.HeaterState

case class StructureUpdate(name: String, state: String)

case class ThermostatHeaterStateUpdate(id: String, statusType: HeaterState)

case class ThermostatHeaterStateInit(id: String, statusType: HeaterState)

case class ChargesUpdate(cost: Int)

case class SummaryChargesUpdated(cost: Long)

case class UtilitiesCostUpdate(cost: Int, heaterState: HeaterState)

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
  val utilitiesActor = context.actorOf(UtilitiesCostCounterActor.props())
  val nestActor = context.actorOf(NestActor.props(accessToken, firebaseURL))
  val webServer = new WebServer(context, self)

  def receive = {
    case stateUpdateMessage: ThermostatHeaterStateUpdate => calculatorActor ! stateUpdateMessage
    case thermostatHeaterStateInit: ThermostatHeaterStateInit => calculatorActor ! thermostatHeaterStateInit
    case chargesUpdate: ChargesUpdate => utilitiesActor ! chargesUpdate
    case utilitiesCostUpdate: UtilitiesCostUpdate => calculatorActor ! utilitiesCostUpdate
    case summaryChargesUpdated: SummaryChargesUpdated => webServer.webPageActor ! summaryChargesUpdated
    case message => println("Received " + message)
  }
}
