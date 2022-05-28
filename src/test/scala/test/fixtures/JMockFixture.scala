package test.fixtures

import org.jmock.Mockery

trait JMockFixture { self: munit.FunSuite =>

  val context: Fixture[Mockery] = new Fixture[Mockery]("jmock"):

    private var mockery: Mockery = _

    override def apply(): Mockery = mockery

    override def beforeEach(context: BeforeEach): Unit =
      mockery = new Mockery
      configureJMock(mockery)

    override def afterEach(context: AfterEach): Unit =
      mockery.assertIsSatisfied()

  def configureJMock(mockery: Mockery): Unit = ()
}
