package models

import java.sql.Timestamp
import java.util.UUID

object Models {

  sealed trait PlanType

  case object Monthly extends PlanType
  case object BiAnnual extends PlanType
  case object Annual extends PlanType

  case class PackageDetails(pkgName: String, packageId: Int, channelIds: List[Int])

  case class Package(packageDetails: PackageDetails, packageCost: List[Channel] => Double)

  case class Channel(channelId: Int, channelName: String, channelCost: Double, channelLanguage: String)

  case class PlanDetails(planId: Int, packageId: Int)

  case class Plan[T <: PlanType](planDetails: PlanDetails, planCost: (Package, List[Channel], T) => Double, planDuration: T => Int)

  case class Subscription(subscriptionId: Int, planID: Int, validTill: Timestamp)

  case class SubscriptionReq(subscriptionId: Int, planID: Int)

  case class Subscriptions(userId: UUID, subscriptions: List[Subscription] = List.empty)

  case class User(userId: UUID, name: String, balance: Double, isActive: Boolean = false)

}
