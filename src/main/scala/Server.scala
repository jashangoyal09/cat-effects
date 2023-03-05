import cats.data.Kleisli
import cats.effect._
import controller.Routes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response, server}

object Server extends IOApp {

  val httpApp: Kleisli[IO, Request[IO], Response[IO]] = server.Router(
    "/" -> Routes.routes
  ).orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8081, "localhost")
      .withHttpApp(httpApp)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}
