package com.daxzel.nestdest

import akka.actor.{Actor, Props}

object UtilitiesCostCounterActor {
  def props(): Props = Props(new UtilitiesCostCounterActor())
}

class UtilitiesCostCounterActor extends Actor {

  var calculatedCost: Long = 0

  def receive = {
    case ChargesUpdate(cost) =>
      calculatedCost += cost
      context.parent ! SummaryChargesUpdated(calculatedCost)
  }

}
