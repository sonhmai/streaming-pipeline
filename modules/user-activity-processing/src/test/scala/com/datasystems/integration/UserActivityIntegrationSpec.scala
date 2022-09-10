package com.datasystems.integration

import com.datasystems.{BaseSpec, DockerKafka}
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.scalatest.BeforeAndAfter

class UserActivityIntegrationSpec extends BaseSpec with BeforeAndAfter {

  val flinkCluster = new MiniClusterWithClientResource(
    new MiniClusterResourceConfiguration.Builder()
      .setNumberSlotsPerTaskManager(2)
      .setNumberTaskManagers(1)
      .build()
  )

  before {
    flinkCluster.before()
    DockerKafka.start()
  }

  after {
    flinkCluster.after()
    DockerKafka.stop()
  }

  "flink and kafka should work" in {
    // TODO - how to start a flink app to test locally or in CI?
  }
}
