import Models.Channel
import cats.effect.IO
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io._

object Routes {

  val helloWorldService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" =>
      Ok("hello")
    case GET -> Root / "hello" / id =>
      Database.getChannel(id.toInt).flatMap(Ok(_))
    case req@POST -> Root / "channels" =>
      req.as[Channel].flatMap(channel => Database.insertChannels(channel).flatMap(Ok(_)))
    case _ =>
      IO(Response(Status.Ok))
  }

}
