package trinity.core.memory

import chisel3._
import chisel3.util._
import trinity.core.ControlFlowBundle
import trinity.core.execute.fn.FnType
import trinity.core.memory.mfn.{Dummy, Lsu, LsuIO}
import trinity.util.{PipelineStageIO, TrinityModule}

class MemoryOperatorIO extends PipelineStageIO(new ControlFlowBundle)

class MemoryOperatorExtraIO extends Bundle {
  val lsuIO = new LsuIO
  val allReady = Output(Bool())
}

class MemoryOperator extends TrinityModule {
  val io = IO(new MemoryOperatorIO)
  val extra = IO(new MemoryOperatorExtraIO)

  val dummy = Module(new Dummy)
  val lsu = Module(new Lsu)
  val mfu = Seq(lsu, dummy)

  val microOp = io.in.bits.microOp

  extra.lsuIO <> lsu.extra

  val allReady = mfu.map(_.io.in.ready).reduce(_ && _)
  extra.allReady := allReady

  val opUseMfu = mfu
    .map(p => (io.in.bits.microOp.fnType === p.io.id) && p.io.id =/= FnType.ALU)
    .reduce(_ || _)
  io.in.ready := allReady
  mfu.foreach { p =>
    p.io.flush := io.flush
    p.io.in.bits := io.in.bits
    p.io.in.valid := io.in.valid && allReady && opUseMfu && microOp.fnType === p.io.id
    p.io.out.ready := io.out.ready
  }
  dummy.io.in.valid := io.in.valid && allReady && !opUseMfu

  io.out.valid := mfu.map(_.io.out.valid).reduce(_ || _)
  io.out.bits := MuxCase(
    dummy.io.out.bits,
    mfu.map { p =>
      val out = p.io.out
      out.valid -> out.bits
    }
  )
}
