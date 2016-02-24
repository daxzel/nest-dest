package com.daxzel.nestdest

import java.time.LocalDateTime

import akka.actor.{Actor, Props}
import com.daxzel.nestdest.HeaterState.HeaterState

import scala.collection.mutable

case class HeaterInfo(previousState: HeaterState, lastUpdateDate: LocalDateTime)

object CalculatorActor {
  def props(): Props = Props(new CalculatorActor())
}

class CalculatorActor() extends Actor {

  val heatersInfo = mutable.HashMap[String, HeaterInfo]()

  val utilityCosts = mutable.HashMap[HeaterState, Int]()

  def secondsPast(firstDate: LocalDateTime, secondDate: LocalDateTime): Int =
    secondDate.getSecond - firstDate.getSecond

  def calculateSpending(
                         utilityCosts: mutable.HashMap[HeaterState, Int],
                         oldHeaterInfo: Option[HeaterInfo],
                         status: HeaterState,
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
      calculateSpending(utilityCosts, heatersInfo.get(id), status, LocalDateTime.now())
    }
  }

}