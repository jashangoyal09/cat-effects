package service

import cats.effect.IO
import cats.implicits._
import db.Database
import models.Models._

import java.sql.Timestamp
import java.util.{Calendar, UUID}

trait Services {
  val database: Database = Database()

  import database._

  val getPlanDuration: PlanType => Int = {
    case Annual => 365
    case BiAnnual => 180
    case Monthly => 30
  }

  def addNewChannels(channels: List[Channel]): IO[Int] = insertChannels(channels)

  /**
   * This method will subscribe to a plan if user has enough funds
   *
   * @param userId          userid from DB
   * @param subscriptionReq plan request to subscribe
   * @param planType        subscription plan with duration i.e. monthly, bi-annual or annual
   * @return list of all users subscriptions
   */
  def processSubscriptionReq(userId: UUID, subscriptionReq: SubscriptionReq, planType: PlanType): IO[List[Subscriptions]] = {
    val planIO: IO[Option[(PlanDetails, Double)]] = getPlan(subscriptionReq.planID, planType)
    planIO.flatMap {
      case Some(plan) =>
        println(s"Got plan $plan to subscribe...")
        getUserDetails(userId) match {
          case Some(user) => if (user.balance < plan._2) {
            throw new Exception("Insufficient balance")
          } else {
            println(s"Got user details $user...")
            val deductedAmtUsr = user.copy(balance = user.balance - plan._2)
            updateUserDetails(deductedAmtUsr).flatMap { users =>
              println("subscribing to new plan...")
              subscribePlan(userId, List(Subscription(subscriptionReq.subscriptionId, subscriptionReq.planID, addDaysInTimestamp(getPlanDuration(planType), timestampNow))))
            }
          }
          case None => throw new Exception(s"No User found with user id ${userId}")
        }
      case None => throw new Exception(s"No plan found with plan id ${subscriptionReq.planID}")
    }
  }

  private def subscribePlan(userId: UUID, subscription: List[Subscription]): IO[List[Subscriptions]] = {
    upsertSubscription(userId, subscription)
  }

  def getPlan[T <: PlanType](planId: Int, planType: T): IO[Option[(PlanDetails, Double)]] = {
    println("Getting All plans")
    val allPlans: IO[List[Plan[T]]] = getAllPlans()
    allPlans.flatMap { allPlans =>
      allPlans.find(_.planDetails.planId == planId).map { plan =>
        println(s"Got plan details $plan with planId $planId")
        getPackageFromId(plan.planDetails.packageId).flatMap { pkgs =>
          pkgs.map { pkg =>
            println(s"Got package $pkg with package id ${plan.planDetails.packageId}")
            val channels = getChannelsFromIds(pkg.packageDetails.channelIds)
            channels.map { channels =>
              (plan.planDetails, plan.planCost(pkg, channels, planType))
            }
          }.sequence
        }
      }.sequence.map(_.flatten)
    }
  }

  private def getChannelsFromIds(channelIds: List[Int]): IO[List[Channel]] = {
    val channelsIO = getChannels()
    channelsIO.map { channels =>
      channels.filter { channel =>
        channelIds.contains(channel.channelId)
      }
    }
  }

  def getChannels(): IO[List[Channel]] = getAllChannels()

  def getPackageFromId(pkgId: Int): IO[Option[Package]] = {
    val pkgsIO = getAllPackages()
    pkgsIO.map { pkgs =>
      pkgs.find(_.packageDetails.packageId == pkgId)
    }
  }

  private def getAllPlans[T <: PlanType](): IO[List[Plan[T]]] = {
    val planDetails = getPlanDetails()
    planDetails.map { planDetails =>
      planDetails.map { planDetail =>
        Plan(planDetail, calPlanCost, getPlanDuration)
      }
    }
  }

  def calPlanCost: (Package, List[Channel], PlanType) => Double = (pkg: Package, channels: List[Channel], planType: PlanType) =>
    pkg.packageCost(channels) * getPlanDuration(planType)

  def addDaysInTimestamp(days: Int, timestamp: Timestamp): Timestamp = {
    val cal = Calendar.getInstance()
    cal.setTime(timestamp)
    cal.add(Calendar.DAY_OF_WEEK, days)
    new Timestamp(cal.getTime().getTime())
  }

  def timestampNow: Timestamp = {
    val date = Calendar.getInstance()
    new Timestamp(date.getTimeInMillis)
  }

  def getSubscription(userId: UUID): IO[Option[Subscriptions]] = {
    getSubscriptionsByUser(userId)
  }

  def addNewPackage(packageReq: PackageDetails): IO[Int] = {
    val pkg = Package(packageReq, calPackageCost)
    insertPackage(pkg)
  }

  def calPackageCost: List[Channel] => Double = (channels: List[Channel]) => channels.map(_.channelCost).sum

  def getPackage(pkgId: Int): IO[Option[(PackageDetails, Double)]] = {
    val allPackages = getAllPackages()
    allPackages.flatMap { filteredPkgs =>
      filteredPkgs.find(_.packageDetails.packageId == pkgId).map { pkgs =>
        val channels: IO[List[Channel]] = getChannelsFromIds(pkgs.packageDetails.channelIds)
        channels.map { channels =>
          (pkgs.packageDetails, pkgs.packageCost(channels))
        }
      }.sequence
    }
  }

  def getPackages(): IO[List[(PackageDetails, Double)]] = {
    val allPackages = getAllPackages()
    allPackages.flatMap { packages =>
      packages.map { pkg =>
        val channels: IO[List[Channel]] = getChannelsFromIds(pkg.packageDetails.channelIds)
        val channelsWithCost = channels.map { channels =>
          (pkg.packageDetails, pkg.packageCost(channels))
        }
        channelsWithCost
      }.sequence
    }
  }

  def addPlan(planDetails: PlanDetails): IO[Int] = {
    insertPlan(planDetails)
  }

  def getPlans[T <: PlanType](planType: T): IO[List[(PlanDetails, Double)]] = {
    val allPlans: IO[List[Plan[T]]] = getAllPlans()
    allPlans.flatMap { allPlans =>
      val allPlansWithCost = allPlans.map { plans =>
        getPackageFromId(plans.planDetails.packageId)
          .flatMap { pkgs =>
            pkgs.map { pkg =>
              val channels = getChannelsFromIds(pkg.packageDetails.channelIds)
              channels.map { channels =>
                (plans.planDetails, plans.planCost(pkg, channels, planType))
              }
            }.sequence
          }
      }.sequence
      allPlansWithCost.map(_.flatten)
    }
  }

  def getPlans[T <: PlanType](): IO[List[Plan[T]]] = getAllPlans()

  def getLeftOverDays(expiryDate: Timestamp): Int = {
    val diff = expiryDate.getTime - timestampNow.getTime
    ((diff / (1000 * 60 * 60 * 24)) % 365).toInt
  }

  def getPlanCost: (List[Channel], PlanType) => Double = (channels: List[Channel], planType: PlanType) => {
    val days = planType match {
      case Annual => 365
      case BiAnnual => 180
      case Monthly => 30
    }
    val channelsCost = channels.map(_.channelCost).sum
    channelsCost * days
  }
}
