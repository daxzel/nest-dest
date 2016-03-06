package com.daxzel.nestdest

import java.time.LocalDateTime

import akka.actor.{Actor, Props}
import com.daxzel.nestdest.HeaterState.HeaterState

import scala.collection.mutable
import scala.concurrent.ExecutionContext

case class HeaterInfo(previousState: HeaterState, lastUpdateDate: LocalDateTime)

case class UpdateStatusTick()

object CalculatorActor {
  def props(): Props = Props(new CalculatorActor())
}

class CalculatorActor() extends Actor {

  var heatersInfo = Map[String, HeaterInfo]()

  val utilityCosts = mutable.HashMap[HeaterState, Int]()

  //todo: add GUI for that
  utilityCosts += (HeaterState.Heating -> 30)
  utilityCosts += (HeaterState.Cooling -> 10)

  def secondsPast(firstDate: LocalDateTime, secondDate: LocalDateTime): Int =
    secondDate.getSecond - firstDate.getSecond

  def calculateSpending(
                         utilityCosts: mutable.HashMap[HeaterState, Int],
                         oldHeaterInfo: Option[HeaterInfo],
                         updateDateTime: LocalDateTime): Int =
    oldHeaterInfo match {
      case Some(HeaterInfo(HeaterState.Off, _)) => 0
      case Some(heaterInfo) => utilityCosts.get(heaterInfo.previousState) match {
        case Some(cost) => cost * secondsPast(heaterInfo.lastUpdateDate, updateDateTime)
        case _ => throw new RuntimeException("Utilities cost should be filled")
      }
      case _ => 0
    }

  def receive = {
    case ThermostatHeaterStateUpdate(id: String, status: HeaterState) => {
      val newDateTime = LocalDateTime.now()
      context.parent ! ChargesUpdate(calculateSpending(utilityCosts, heatersInfo.get(id), newDateTime))
      heatersInfo += (id -> HeaterInfo(status, newDateTime))
    }
    case UtilitiesCostUpdate(cost: Int, heaterState: HeaterState) => {
      utilityCosts += (heaterState -> cost)
    }
    case _: UpdateStatusTick => {
      val newDateTime = LocalDateTime.now()
      heatersInfo = heatersInfo.transform((id, heaterInfo) => {
        print("calculating new value...")
        context.parent ! ChargesUpdate(calculateSpending(utilityCosts, Option[HeaterInfo](heaterInfo), newDateTime))
        HeaterInfo(heaterInfo.previousState, newDateTime)
      })
    }
  }

  import scala.concurrent.duration._
  import ExecutionContext.Implicits.global

  context.system.scheduler.schedule(5 seconds, 5 seconds, context.self, UpdateStatusTick())
}