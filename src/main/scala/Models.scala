import cats.effect.IO
import org.http4s.EntityEncoder
import org.http4s._
import org.http4s.circe._
import io.circe.generic.auto._
import cats.effect.IO
import io.circe.Encoder

object Models {

  case class Channel(channel_name: String, channel_id: Int, channel_cost: Double, channel_language: String)
  object Channel {
//    implicit val channelEncoder: EntityEncoder[IO, Channel] = jsonEncoderOf[IO, Channel]
//    implicit val HelloEncoder: Encoder[Channel] =
//      Encoder.instance { (channel: Channel) =>
//        json"""{"channel": ${Channel}}"""
//      }
  }

  case class Package(package_name: String, channels: List[Channel], package_cost: Double)

  case class Plan(plan_name: String, plan_duration: String, plan_cost: Double)

  case class Subscription(subscription_name: String, default_package: Package, additional_channels: List[Channel], subscription_duration: String)


}
