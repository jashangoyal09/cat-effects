package service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import db.Database
import models.Models.{Annual, BiAnnual, Channel, Monthly, Package, PackageDetails, PlanDetails}
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers._


class ServicesSpec extends AnyFunSuite with MockitoSugar with should.Matchers with Services {

  val mockedDatabase: Database = mock[db.Database]
  override val database: Database = mockedDatabase

  def correctDB(): Unit = {
    val planDetails = PlanDetails(1, 1)
    val planDetails2 = PlanDetails(2, 3)

    val packageDetails = PackageDetails("basic-pkg", 1, List(1, 2, 3))
    val packageDetails2 = PackageDetails("basic-pkg", 3, List(2, 3))
    val packages = List(Package(packageDetails, calPackageCost), Package(packageDetails2, calPackageCost))
    val channels = List(Channel(1, "channel1", 1.0, "hindi"), Channel(2, "channel2", 2.0, "english"), Channel(1, "channel1", 4.0, "hindi"))

    when(mockedDatabase.getPlanDetails()).thenReturn(IO(List(planDetails, planDetails2)))
    when(mockedDatabase.getAllPackages()).thenReturn(IO(packages))
    when(mockedDatabase.getAllChannels()).thenReturn(IO(channels))

  }

  test("Get all Packages") {
    correctDB
    val result = getPackages.unsafeRunSync
    println(result)
    val expectedResult = List((PackageDetails("basic-pkg", 1, List(1, 2, 3)), 7.0), (PackageDetails("basic-pkg", 3, List(2, 3)), 2.0))
    result should contain theSameElementsAs expectedResult
  }

  test("Get Packages with package id") {
    correctDB
    val result = getPackage(1).unsafeRunSync
    println(result)
    val expectedResult: Option[(PackageDetails, Double)] = Some((PackageDetails("basic-pkg", 1, List(1, 2, 3)), 7.0))
    assert(result === expectedResult)
  }

  test("Should not get Packages with wrond package id") {
    correctDB
    val result = getPackage(10).unsafeRunSync
    println(result)
    val expectedResult: Option[(PackageDetails, Double)] = None
    assert(result === expectedResult)
  }

  test("Get no package cost if channel ids are wrong") {
    val planDetails = PlanDetails(1, 1)
    val planDetails2 = PlanDetails(2, 3)

    val packageDetails = PackageDetails("basic-pkg", 1, List(11, 12, 13))
    val packageDetails2 = PackageDetails("basic-pkg", 3, List(12, 13))
    val packages = List(Package(packageDetails, calPackageCost), Package(packageDetails2, calPackageCost))
    val channels = List(Channel(1, "channel1", 1.0, "hindi"), Channel(2, "channel2", 2.0, "english"), Channel(1, "channel1", 4.0, "hindi"))

    when(mockedDatabase.getPlanDetails()).thenReturn(IO(List(planDetails, planDetails2)))
    when(mockedDatabase.getAllPackages()).thenReturn(IO(packages))
    when(mockedDatabase.getAllChannels()).thenReturn(IO(channels))
    val result = getPackages.unsafeRunSync
    println(result)
    val expectedResult = List((PackageDetails("basic-pkg", 1, List(11, 12, 13)), 0.0), (PackageDetails("basic-pkg", 3, List(12, 13)), 0.0))
    result should contain theSameElementsAs expectedResult
  }

  test("Successfully get all monthly plans") {
    correctDB()
    val result = getPlans(Monthly).unsafeRunSync
    val expectedResult = List((PlanDetails(1, 1), 210.0), (PlanDetails(2, 3), 60.0))
    result should contain theSameElementsAs expectedResult
  }

  test("Successfully get all BiAnnual plans") {
    correctDB()
    val result = getPlans(BiAnnual).unsafeRunSync
    val expectedResult = List((PlanDetails(1, 1), 1260.0), (PlanDetails(2, 3), 360.0))
    result should contain theSameElementsAs expectedResult
  }

  test("Successfully get all Yearly plans") {
    correctDB()
    val result = getPlans(Annual).unsafeRunSync
    val expectedResult = List((PlanDetails(1, 1), 2555.0), (PlanDetails(2, 3), 730.0))
    result should contain theSameElementsAs expectedResult
  }

  test("Not get any plan if package not exist") {
    val planDetails = PlanDetails(1, 10)
    val planDetails2 = PlanDetails(2, 13)
    val packageDetails = PackageDetails("basic-pkg", 1, List(1, 2, 3))
    val packageDetails2 = PackageDetails("basic-pkg", 3, List(2, 3))
    val packages = List(Package(packageDetails, calPackageCost), Package(packageDetails2, calPackageCost))
    val channels = List(Channel(1, "channel1", 20.0, "english"), Channel(2, "channel2", 10.0, "english"), Channel(1, "channel1", 40.0, "english"))

    when(mockedDatabase.getPlanDetails()).thenReturn(IO(List(planDetails, planDetails2)))
    when(mockedDatabase.getAllPackages()).thenReturn(IO(packages))
    when(mockedDatabase.getAllChannels()).thenReturn(IO(channels))

    val result = getPlans(Monthly).unsafeRunSync
    result should contain theSameElementsAs List()
  }

  test("Not get any plan if channels not exist") {
    val planDetails = PlanDetails(1, 10)
    val planDetails2 = PlanDetails(2, 13)
    val packageDetails = PackageDetails("basic-pkg", 1, List(11, 12, 13))
    val packageDetails2 = PackageDetails("basic-pkg", 3, List(12, 13))
    val packages = List(Package(packageDetails, calPackageCost), Package(packageDetails2, calPackageCost))
    val channels = List(Channel(1, "channel1", 20.0, "english"), Channel(2, "channel2", 10.0, "english"), Channel(1, "channel1", 40.0, "english"))

    when(mockedDatabase.getPlanDetails()).thenReturn(IO(List(planDetails, planDetails2)))
    when(mockedDatabase.getAllPackages()).thenReturn(IO(packages))
    when(mockedDatabase.getAllChannels()).thenReturn(IO(channels))

    val result = getPlans(Monthly).unsafeRunSync
    result should contain theSameElementsAs List()
  }

  test("Get plans with validate package ids") {
    val planDetails = PlanDetails(1, 1)
    val planDetails2 = PlanDetails(2, 13)
    val packageDetails = PackageDetails("basic-pkg", 1, List(1, 2, 3))
    val packageDetails2 = PackageDetails("basic-pkg", 3, List(12, 13))
    val packages = List(Package(packageDetails, calPackageCost), Package(packageDetails2, calPackageCost))
    val channels = List(Channel(1, "channel1", 20.0, "english"), Channel(2, "channel2", 10.0, "english"), Channel(1, "channel1", 40.0, "english"))

    when(mockedDatabase.getPlanDetails()).thenReturn(IO(List(planDetails, planDetails2)))
    when(mockedDatabase.getAllPackages()).thenReturn(IO(packages))
    when(mockedDatabase.getAllChannels()).thenReturn(IO(channels))

    val result = getPlans(Monthly).unsafeRunSync
    result should contain theSameElementsAs List((PlanDetails(1, 1), 2100.0))
  }

}
