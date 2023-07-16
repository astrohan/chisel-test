package roma

import org.chipsalliance.cde.config.{Config, Parameters}


object ExampleMain extends App { new ExampleGenerator(args) }
class ExampleGenerator(args: Array[String]) extends BaseGenerator(args) {
  override lazy val harness = new BaseHarness()(config)
  override lazy val config = configName match {
    case _ => {
      println(s"${configName} is wrong config name");
      new Config(Parameters.empty)
    }
  }
}
