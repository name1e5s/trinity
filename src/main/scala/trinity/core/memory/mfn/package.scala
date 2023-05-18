package trinity.core.memory

import chisel3._
import chisel3.util._
import trinity.Constants._
import trinity.core.ControlFlowBundle
import trinity.core.execute.fn._
import trinity.util.TrinityModule

package object mfn {
  class MfnIO extends Bundle {
    val id = Output(FnType())
    val flush = Input(Bool())
    val in = Flipped(Decoupled(new ControlFlowBundle))
    val out = Decoupled(new ControlFlowBundle)
  }

  abstract class MfnModule extends TrinityModule {
    def id: UInt
    val io = IO(new MfnIO)

    io.id := id
  }
}
