package db

import cats.effect.IO
import controller.Routes.{addDaysInTimestamp, calPackageCost, getLeftOverDays}
import models.Models.{Channel, Package, PackageDetails, PlanDetails, Subscription, Subscriptions, User}

import java.util.UUID

case class Database() {

  private var planDetails: List[PlanDetails] = List(PlanDetails(1, 1))
  private var packageDetails = PackageDetails("basic-plan", 1, List(1, 2, 3))
  private var packages: List[Package] = List(Package(packageDetails, calPackageCost))
  private var channels = List(Channel(1, "HBO", 5.0, "english"), Channel(2, "Star Plus", 3.0, "hindi"), Channel(3, "Discovery", 2.0, "english"))
  private var subscriptions: List[Subscriptions] = List(Subscriptions(UUID.fromString("9949e207-52e9-4d62-a52a-59fc72ac7cc3"), List.empty[Subscription]))
  private var users: List[User] = List(User(UUID.fromString("9949e207-52e9-4d62-a52a-59fc72ac7cc3"), "user1", 100000.0, false))

  def getUserDetails(userId: UUID): Option[User] = users.find(user => user.userId == userId)

  def updateUserDetails(updatedUser: User): IO[List[User]] = IO(users.filter(user => user.userId == updatedUser.userId) :+ updatedUser)

  def insertChannels(channel: List[Channel]): IO[Int] = {
    println(s"Inserting channel to the database $channel")
    channels = channels ++ channel
    IO(1)
  }

  def getAllChannels(): IO[List[Channel]] = IO(channels)

  def insertPackage(pkg: Package): IO[Int] = {
    packages = packages :+ pkg
    IO(1)
  }

  def getAllPackages(): IO[List[Package]] = IO(packages)

  def insertPlan(plan: PlanDetails): IO[Int] = {
    planDetails = planDetails :+ plan
    IO(1)
  }

  def getPlanDetails(): IO[List[PlanDetails]] = IO(planDetails)

  def insertSubscriptions(subscription: Subscriptions): IO[Int] = {
    subscriptions = subscriptions :+ subscription
    IO(1)
  }

  def getAllSubscription(): IO[List[Subscriptions]] = IO(subscriptions)

  /**
   * This method will perform upsert with subscription.
   * 1. if user subscribe to new plan then it will be inserted to db
   * 2. if user resubscribe to old plan. In that it will extended or renewed based on the current left over days
   * 3. if plan is not mentioned that means no need to change anything
   *
   * @param userId
   * @param subscriptionReq list of all plans need to subscribe
   * @return list of all plans that user subscribed
   */

  def upsertSubscription(userId: UUID, subscriptionReq: List[Subscription]): IO[List[Subscriptions]] = {
    subscriptions.find(subs => subs.userId == userId) match {
      case Some(allExistingSubs) =>
        val newPlanIds = subscriptionReq.map(_.planID) diff allExistingSubs.subscriptions.map(_.planID)
        val oldPlanIds = allExistingSubs.subscriptions.map(_.planID) intersect subscriptionReq.map(_.planID)
        println(s"newPlanIds ${newPlanIds} oldPlanIds ${oldPlanIds}")

        val unChangedPlans = allExistingSubs.subscriptions.filterNot(subs => subscriptionReq.map(_.planID).contains(subs.planID))
        println(s"unChangedPlanss are::: $unChangedPlans")
        val newPlansToAdd = subscriptionReq.filter(subscription => newPlanIds.contains(subscription.planID))
        println(s"newPlansToAdd plans are::: $newPlansToAdd")
        val oldPlansToUpdate = allExistingSubs.subscriptions.filter(subs => oldPlanIds.contains(subs.planID)).map { subs =>
          val addDays = getLeftOverDays(subs.validTill)
          val newExpiry = if (addDays < 1) {
            subscriptionReq.find(_.planID == subs.planID).map(_.validTill).getOrElse(throw new Exception(""))
          } else {
            addDaysInTimestamp(addDays, subs.validTill)
          }
          subs.copy(validTill = newExpiry)
        }
        println(s"oldPlansToUpdate plans are::: $oldPlansToUpdate")
        val updateSubs = unChangedPlans ++ newPlansToAdd ++ oldPlansToUpdate
        println(s"all updated plans for user ${userId} are::: $updateSubs")
        subscriptions = subscriptions.filterNot(_.userId == userId) :+ Subscriptions(userId, updateSubs)
        IO(subscriptions)
      case None => subscriptions = subscriptions :+ Subscriptions(userId, subscriptionReq)
        IO(subscriptions)

    }
  }

  def getSubscriptionsByUser(userId: UUID): IO[Option[Subscriptions]] = IO(subscriptions.find(_.userId == userId))

}
