import Models._
import cats.effect.IO
import doobie.Update0

import scala.collection.mutable

object Database {

  val packages: mutable.Map[String, Package] = mutable.Map.empty
  val plans: mutable.Map[String, Plan] = mutable.Map.empty
  val subscriptions: mutable.Map[String, Subscription] = mutable.Map.empty
  private val channels: mutable.Map[Int, Channel] = mutable.Map.empty
  /**
   * @param channel
   * @return
   */
  def insertChannels(channel: Channel): IO[mutable.Map[Int, Channel]] = {

    //    channel.map { channel =>
    println("Inserting channel to the database")
    println("Inserted channel to the database")
    IO(channels.addOne(channels.keySet.max + 1, channel))
    //    }
  }

  def getChannel(id: Int): IO[Option[Channel]] = {
    IO(channels.get(id))
  }

}
