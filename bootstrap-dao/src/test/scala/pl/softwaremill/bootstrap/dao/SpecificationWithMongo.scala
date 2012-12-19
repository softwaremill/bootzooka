package pl.softwaremill.bootstrap.dao

import org.specs2.mutable.Specification
import org.specs2.specification.{Step, Fragments}
import com.mongodb.casbah.{MongoDB, MongoConnection}

trait SpecificationWithMongo extends Specification {

  val mongoPort = 24567

  protected var mongoRunner: MongoRunner = null
  protected implicit var mongoConn: MongoDB = null

  // here we reorder specification fragments to wrap all tests with MongoDB start and stop
  // we also make tests sequential to avoid problems with concurrent tests execution
  override def map(fs: => Fragments ) = sequential ^ Step(startMongo) ^ fs ^ Step(stopMongo)

  def startMongo {
    mongoRunner = MongoRunner.run(mongoPort, verbose = true)
    mongoConn = MongoConnection("localhost", mongoPort)("bootstrap")
  }

  def stopMongo {
    mongoRunner.stop()
  }

}
