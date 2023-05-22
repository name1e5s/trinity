package trinity.sim

import chisel3._
import chisel3.stage.ChiselGeneratorAnnotation
import trinity.core.Core

class SimCore extends Module {
  val core = Module(new Core)
  val simRam = Module(new SimRam)

  core.io.icache <> simRam.io.icache
  core.io.dcache <> simRam.io.dcache
}

object SimCore {
  def main(args: Array[String]): Unit = {
    (new chisel3.stage.ChiselStage).execute(
      Array("-X", "verilog"),
      Seq(
        ChiselGeneratorAnnotation(() => new SimCore)
      )
    )
  }
}
