package controller

import cats.effect.IO
import io.circe.Decoder.Result
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, HCursor, Json}
import models.Models._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.dsl.io._
import service.Services

import java.sql.Timestamp
import java.util.UUID
import scala.util.{Failure, Success, Try}

object Routes extends Services {

  implicit val uuidQueryParamDecoder: QueryParamDecoder[UUID] = QueryParamDecoder[String].map { uuid =>
    Try(UUID.fromString(uuid)) match {
      case Success(value) => value
      case Failure(exception) => throw new Exception(s"Got exception ex, ${exception.getMessage}")
    }
  }

  implicit val planTypeParamDecoder: QueryParamDecoder[PlanType] = QueryParamDecoder[String].map {
    case "Monthly" => Monthly
    case "BiAnnual" => BiAnnual
    case "Annual" => Annual
  }
  implicit val TimestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" =>
      Ok("hello")
    case GET -> Root / "channels" =>
      println(s"Getting all channels")
      getChannels.flatMap(Ok(_))
    case req@POST -> Root / "channels" =>
      println("Adding new Channel in DB...")
      req.as[List[Channel]].flatMap(channels => addNewChannels(channels)).flatMap(Ok(_))
    case req@POST -> Root / "package" =>
      req.as[PackageDetails].map(packageReq => addNewPackage(packageReq)).flatMap(Ok(_))
    case GET -> Root / "package" / id =>
      getPackage(id.toInt).flatMap {
        case Some(pkg) => Ok(pkg)
        case None => NotFound(s"No package with id $id found in the database")
      }
    case GET -> Root / "packages" =>
      getPackages().flatMap(Ok(_))
    case req@POST -> Root / "plan" =>
      req.as[PlanDetails].map(planReq => addPlan(planReq)).flatMap(Ok(_))
    case GET -> Root / "plan" / id :? PlanTypeQueryParamMatcher(planType) =>
      getPlan[PlanType](id.toInt, planType).flatMap {
        case Some(plan) => Ok(plan)
        case None => NotFound(s"No plan with id $id found in the database")
      }
    case GET -> Root / "plans" :? PlanTypeQueryParamMatcher(planType) =>
      getPlans(planType).flatMap(Ok(_))
    case GET -> Root / "subscription" :? UserIDQueryParamMatcher(userid) =>
      getSubscription(userid).flatMap {
        case Some(subscription) => Ok(subscription)
        case None => NotFound(s"No plan with id  found in the database")
      }
    case req@POST -> Root / "subscribe" :? UserIDQueryParamMatcher(userid) :? PlanTypeQueryParamMatcher(planType) =>
      println(s"Hitting subscribe post request $userid...")
      req.as[SubscriptionReq].flatMap { subscriptionReq =>
        println(s"Got subscription with $subscriptionReq")
        processSubscriptionReq(userid, subscriptionReq, planType).flatMap(Ok(_))
      }
  }

  object UserIDQueryParamMatcher extends QueryParamDecoderMatcher[UUID]("userid")

  object PlanTypeQueryParamMatcher extends QueryParamDecoderMatcher[PlanType]("plantype")

}
