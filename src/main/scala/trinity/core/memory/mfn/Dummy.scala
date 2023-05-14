package trinity.core.memory.mfn

import chisel3._
import trinity.core.ControlFlowBundle
import trinity.core.execute.fn._
import trinity.util.PipelineStage

class Dummy extends MfnModule {
  override def id: UInt = FnType.ALU

  val stage = Module(PipelineStage(new ControlFlowBundle))
  stage.io.flush := io.flush
  stage.io.in <> io.in
  stage.io.out <> io.out
}
