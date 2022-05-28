package test.fixtures

abstract class JMockSuite extends munit.FunSuite with JMockFixture:

  override def munitFixtures: Seq[Fixture[_]] = super.munitFixtures ++ Seq(context)
