package roma

import freechips.rocketchip.util.{ElaborationArtefacts, HasRocketChipStageUtils}
import org.chipsalliance.cde.config.Config


abstract class BaseGenerator(args: Array[String]) extends HasRocketChipStageUtils {
  val opts = args.grouped(2).collect { case Array(k, v) => k -> v}.toMap
  
  val configName: String = opts.getOrElse("--config", "default")
  
  val packageName = this.getClass.getPackage.getName
  val baseDir = System.getProperty("user.dir")
  val targetDir = s"${baseDir}/outfeed/${packageName}.${configName}"
  
  println(s"Config Name is $configName")
  val config: Config
  val harness: BaseHarness
  val design = s"$packageName.$configName.TestHarness"
  
  //circt.stage.ChiselStage.emitSystemVerilog(
  //  harness,
  //  Array("--target-dir", targetDir,
  //    "--infer-rw", s"${design}",
  //    "--repl-seq-mem", s"-c:${design}:-o:${design}.sram.conf",
  //    "--output-annotation-file", s"${design}")
  //)
  new chisel3.stage.ChiselStage().emitVerilog(
    harness,
    Array("--target-dir", targetDir,
      "--infer-rw", s"${design}",
      "--repl-seq-mem", s"-c:${design}:-o:${design}.sram.conf",
      "-fct", "firrtl.passes.InlineInstances",
      "--output-annotation-file", s"${design}",
      "-o", s"${design}.v")
  )
  
  ElaborationArtefacts.files.foreach { case (extension, contents) =>
    val f_name = s"$packageName.$configName.${extension}"
    writeOutputFile(targetDir, f_name, contents())
  }
  
  import sys.process._
  val proc = Seq(
    s"${baseDir}/scripts/gen-post-processing.sh ${baseDir} ${targetDir} ${design}",
  )
  
  proc.foreach{ p =>
    require(p.! == 0, "Failed post-processing")
  }
}
