import Models._
import cats.effect.IO
import scala.collection.mutable

object Database {

  val packages: mutable.Map[String, Package] = mutable.Map.empty
  val plans: mutable.Map[String, Plan] = mutable.Map.empty
  val subscriptions: mutable.Map[String, Subscription] = mutable.Map.empty
  private val channels: mutable.Map[Int, Channel] = mutable.Map.empty

  /**
   * @param channel - channel information to be inserted
   * @return - IO(1) if successful else IO(0)
   */
  def insertChannels(channel: Channel): IO[Int] = {
    println(s"Inserting channel to the database $channel")
    IO(1)
  }

  /**
   * Fetch Channel from database on basis of channel id
   * @param id - channel id
   * @return - Option[Channel]
   */
  def getChannel(id: Int): IO[Option[Channel]] = {
    IO(channels.get(id))
    IO(Option(Channel("channel-1", 1, 250, "hindi")))
  }

}
