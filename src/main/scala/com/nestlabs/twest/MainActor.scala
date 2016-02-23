package com.nestlabs.twest

import akka.actor.Actor

case class StructureUpdate(name: String, state: String)

case class ThermostatStateUpdate(structId: String, location: String, statusType: String, status: String)

class MainActor(firebaseURL: String, accessToken: String) extends Actor {
  val calculatorActor = context.actorOf(CalculatorActor.props())
  val nestActor = context.actorOf(NestActor.props(accessToken, firebaseURL))

  def receive = {
    case message => println("Received " + message)
  }
}
