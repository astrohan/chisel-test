package roma

import chisel3._
import org.chipsalliance.cde.config.Parameters

class BaseHarness(implicit val p: Parameters) extends Module
{
  lazy val io = IO(new Bundle{
    val success = Output(Bool())
  })
  
  // TODO::Multiple clock domain
  io.success := false.B // default value
}
