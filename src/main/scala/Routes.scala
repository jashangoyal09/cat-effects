import Models.Channel
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.io._

import scala.collection.mutable

object Routes {
  implicit val channelEncoder: EntityEncoder[IO, Channel] = jsonEncoderOf[IO, Channel]
  implicit val channelEncoder1: EntityEncoder[IO, mutable.Map[Int, Channel]] = jsonEncoderOf[IO, mutable.Map[Int, Channel]]
  implicit val decoder: EntityDecoder[IO, Channel] = jsonOf[IO, Channel]
  implicit val decoder1: EntityDecoder[IO, mutable.Map[Int, Channel]] = jsonOf[IO, mutable.Map[Int, Channel]]
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  //  val channel = Channel("channel_name",123,20.0,"en")
  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / id =>
      val channel: IO[Option[Channel]] = Database.getChannel(id.toInt)
      Ok(channel)
    case req@POST -> Root / "channels" =>
      for {
        // Decode a User request
        channel <- req.as[Channel]
        // Encode a hello response
      } yield {
        println(s"\n\n\n${channel}\n\n")
        val res: IO[mutable.Map[Int, Channel]] = Database.insertChannels(channel)
        Ok(res.asJson)
      }
  }
}
