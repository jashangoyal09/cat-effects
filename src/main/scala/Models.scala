
object Models {

  case class Channel(channel_name: String, channel_id: Int, channel_cost: Double, channel_language: String)

  case class Package(package_name: String, channels: List[Channel], package_cost: Double)

  case class Plan(plan_name: String, plan_duration: String, plan_cost: Double)

  case class Subscription(subscription_name: String, default_package: Package, additional_channels: List[Channel], subscription_duration: String)

}
