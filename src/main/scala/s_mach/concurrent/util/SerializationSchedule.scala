/*
                    ,i::,
               :;;;;;;;
              ;:,,::;.
            1ft1;::;1tL
              t1;::;1,
               :;::;               _____        __  ___              __
          fCLff ;:: tfLLC         / ___/      /  |/  /____ _ _____ / /_
         CLft11 :,, i1tffLi       \__ \ ____ / /|_/ // __ `// ___// __ \
         1t1i   .;;   .1tf       ___/ //___// /  / // /_/ // /__ / / / /
       CLt1i    :,:    .1tfL.   /____/     /_/  /_/ \__,_/ \___//_/ /_/
       Lft1,:;:       , 1tfL:
       ;it1i ,,,:::;;;::1tti      s_mach.concurrent
         .t1i .,::;;; ;1tt        Copyright (c) 2014 S-Mach, Inc.
         Lft11ii;::;ii1tfL:       Author: lance.gatlin@gmail.com
          .L1 1tt1ttt,,Li
            ...1LLLL...
*/
package s_mach.concurrent.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic._
import scala.concurrent.duration._
import scala.collection.JavaConverters._

object SerializationSchedule {
  sealed trait Event[ID] {
    def id: ID
    def elapsed_ns: Long
  }
  case class StartEvent[ID](
    id: ID,
    elapsed_ns: Long
  ) extends Event[ID]
  case class EndEvent[ID](
    id: ID,
    elapsed_ns: Long,
    start: StartEvent[ID]
  ) extends Event[ID]
}

/**
 * A case class for determining the ordering of instantaneous and
 * time-spanning real-time events
 * @tparam ID event identifier type
 */
case class SerializationSchedule[ID]() {
  import SerializationSchedule._

  val startTime_ns = System.nanoTime()

  private[this] val _startEvents = new ConcurrentHashMap[ID, StartEvent[ID]]()
  private[this] val _endEvents = new ConcurrentHashMap[ID, EndEvent[ID]]()

  private[this] val cacheValid = new AtomicBoolean(true)
  private[this] var cachedEvents = Vector[Event[ID]]()

  private[this] val _debug = new AtomicReference({ id:ID => () })

  def debug(__debug: ID => Unit) = _debug.getAndSet(__debug)

  /**
   * Register an instantaneous event
   * @param id id of event
   * @return the elapsed duration since the schedule was created
   * */
  def addEvent(id: ID) : FiniteDuration = addStartEvent(id)

  /**
   * Register the start of a time-spanning event
   * @param id id of event
   * @return the elapsed duration since the schedule was created
   */
  def addStartEvent(id: ID) : FiniteDuration = {
    _debug.get.apply(id)
    val elapsed_ns = System.nanoTime() - startTime_ns
    if(_startEvents.put(id, StartEvent(id, elapsed_ns)) != null)
      throw new IllegalArgumentException(s"Start event $id already exists!")
    cacheValid.getAndSet(false)
    elapsed_ns.nanos
  }

  /**
   * Register the end of a time-spanning event
   * @param id id of event
   * @return the elapsed duration since the schedule was created
   */
  def addEndEvent(id: ID) : FiniteDuration = {
    _debug.get.apply(id)
    val elapsed_ns = System.nanoTime() - startTime_ns
    val startEvent = Option(_startEvents.get(id)).getOrElse {
      throw new IllegalArgumentException(s"Event $id was never started!")
    }
    if(_endEvents.put(id, EndEvent(id, elapsed_ns, startEvent)) != null) {
      throw new IllegalArgumentException(s"End event $id already exists!")
    }
    cacheValid.getAndSet(false)
    elapsed_ns.nanos
  }

  /** @return a Vector of events ordered by their occurrence */
  def orderedEvents : Vector[Event[ID]] = {
    cachedEvents.synchronized {
      if(cacheValid.get == false) {
        cachedEvents =
          {
            _startEvents.values().asScala ++ _endEvents.values().asScala
          }.toList.sortBy(_.elapsed_ns).toVector
      }
      cachedEvents
    }
  }

  def eventMap : Map[ID, Event[ID]] = orderedEvents.map(e => (e.id, e)).toMap

  override def toString = {
    val events = orderedEvents.map {
      case StartEvent(id, _) => s"Start('$id')"
      case EndEvent(id,_,_) => s"End('$id')"
    }
    s"SerializationSchedule(${events.mkString(",")}})"
  }

  /** @return an unordered Vector of start events */
  def startEvents : Vector[StartEvent[ID]] =
    _startEvents.values().asScala.toVector

  /** @return an unordered Vector of end events */
  def endEvents : Vector[EndEvent[ID]] = _endEvents.values().asScala.toVector

  /**
   * @throws IllegalArgumentException if id1 or id2 doesn't exist OR if id2 is
   *         not a time-spanning event
   * @return TRUE if the event id1 happened before event id2
   * */
  def happensBefore(id1: ID, id2: ID) : Boolean = {
    val id2_start = Option(_startEvents.get(id2)).getOrElse {
      throw new IllegalArgumentException(s"No such start event $id2!")
    }
    Option(_endEvents.get(id1)) match {
      case Some(id1_end) =>
        id1_end.elapsed_ns < id2_start.elapsed_ns
      case None =>
        val id1_start = Option(_startEvents.get(id1)).getOrElse {
          throw new IllegalArgumentException(s"No such start event $id1!")
        }
        id1_start.elapsed_ns < id2_start.elapsed_ns
    }
  }

  /**
   * @throws IllegalArgumentException if id1 or id2 doesn't exist OR if id2 is
   *         not a time-spanning event
   * @return TRUE if the event id1 happened during time-spanning event id2
   * */
  def happensDuring(id1: ID, id2: ID) : Boolean = {
    val id2_end = Option(_endEvents.get(id2)).getOrElse {
      throw new IllegalArgumentException(s"No such end event $id2!")
    }

    val id2_range = id2_end.start.elapsed_ns to id2_end.elapsed_ns
    Option(_endEvents.get(id1)) match {
      case Some(id1_end) =>
        val id1_range = id1_end.start.elapsed_ns to id1_end.elapsed_ns
        id2_range.contains(id1_end.elapsed_ns) ||
        id2_range.contains(id1_end.start.elapsed_ns) ||
        id1_range.contains(id2_end.elapsed_ns) ||
        id1_range.contains(id2_end.start.elapsed_ns)
      case None =>
        val id1_start = Option(_startEvents.get(id1)).getOrElse {
          throw new IllegalArgumentException(s"No such start event $id1!")
        }
        id2_range.contains(id1_start.elapsed_ns)
    }
  }

}
