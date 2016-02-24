package com.daxzel.nestdest

import akka.actor.{Actor, Props}
import com.firebase.client.Firebase.AuthListener
import com.firebase.client.{DataSnapshot, Firebase, FirebaseError, ValueEventListener}

import scala.collection.mutable.HashMap

object NestActor {
  def props(accessToken: String, firebaseURL: String): Props = Props(new NestActor(accessToken, firebaseURL))
}

class NestActor(accessToken: String, firebaseURL: String) extends Actor {
  val fb = new Firebase(firebaseURL)

  val hvacStates = HashMap[String, HashMap[String, String]]()
  val structureStates = HashMap[String, String]()
  val structMap = HashMap[String, String]()

  // authenticate with our current credentials
  fb.auth(accessToken, new AuthListener {
    def onAuthError(e: FirebaseError) {
      println("fb auth error: " + e)
    }

    def onAuthSuccess(a: AnyRef) {
      println("fb auth success: " + a)
      // when we've successfully authed, add a change listener to the whole tree
      fb.addValueEventListener(new ValueEventListener {
        def onDataChange(snapshot: DataSnapshot) {
          // when data changes we send our receive block an update
          self ! snapshot
        }

        def onCancelled(err: FirebaseError) {
          // on an err we should just bail out
          self ! err
        }
      })
    }

    def onAuthRevoked(e: FirebaseError) {
      println("fb auth revoked: " + e)
    }
  })

  def receive = {
    case s: DataSnapshot =>
      try {
        import scala.collection.JavaConversions._

        // this looks scary, but because processing is single threaded here we're ok
        structMap.clear()
        // process structure specific data
        val structures = s.child("structures")
        if (structures != null && structures.getChildren != null) {
          structures.getChildren.foreach { struct =>
            // update our map of struct ids -> struct names for lookup later
            val structName = struct.child("name").getValue.toString
            structMap += (struct.getName() -> structName)
            // now compare states and send an update if they changed
            val structState = struct.child("away").getValue.toString
            val oldState = structureStates.getOrElse(structName, "n/a")
            structureStates += (structName -> structState)
            if (!oldState.equals("n/a") && !oldState.equals(structState)) {
              context.parent ! StructureUpdate(structName, structState)
            }
          }
        } else {
          // having no structures would be weird, but warn
          println("no structures? children=" + s.getChildren.map(_.getName).mkString(", "))
        }
        val therms = s.child("devices").child("thermostats")
        if (therms != null && therms.getChildren != null) {
          therms.getChildren.foreach { therm =>
            val structId = therm.child("structure_id").getValue.toString
            val thermId = therm.getName
            val hvacState = therm.child("hvac_state").getValue.toString

            def diffAndSend(stateMap: HashMap[String, HashMap[String, String]],
                            statusType: String,
                            status: String) {
              if (stateMap.get(structId).isEmpty) {
                stateMap += (structId -> HashMap[String, String]())
              }
              val oldState = stateMap(structId).getOrElse(thermId, "n/a")
              if (!oldState.equals("n/a") && !oldState.equals(status)) {
                context.parent ! ThermostatHeaterStateUpdate(thermId, HeaterState.get(status))
              }
              stateMap(structId) += (thermId -> status)
            }

            diffAndSend(hvacStates, "hvac_state", hvacState)
          }
        }
      } catch {
        case e: Exception => {
          println("uhoh " + e)
          e.printStackTrace()
        }
      }

    case e: FirebaseError => {
      println("got firebase error " + e)
    }
  }


}
