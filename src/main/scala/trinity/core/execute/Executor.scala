package trinity.core.execute

import chisel3._
import chisel3.util._
import trinity.core._
import trinity.core.Constants._
import trinity.core.decode.isa.SrcType
import trinity.core.execute.fn._
import trinity.util.TrinityModule

class ExecutorIO extends Bundle {
  val in = Flipped(Decoupled(new ControlFlowBundle))
  val out = Decoupled(new ControlFlowBundle)
  val redirect = Valid(UInt(xLen.W))
}

class Executor extends TrinityModule {
  val io = IO(new ExecutorIO)

  io.in <> io.out

  val alu = Module(new Alu)
  val bru = Module(new Bru)
  val agu = Module(new Agu)
  val fu = Seq(alu, bru, agu)

  val in = io.in.bits
  val out = io.out.bits

  agu.extra.immediate := in.immediate
  bru.extra.pc := in.instruction.pc
  bru.extra.offset := in.immediate
  bru.extra.nextPc := in.instruction.predictedNextPc
  bru.extra.isBranch := in.microOp.fnType === FnType.BRU

  val srcMap = List(
    SrcType.IMM -> in.immediate,
    SrcType.PC -> in.instruction.pc
  )
  val src1 = MuxLookup(in.microOp.src1Type, in.rs1.data)(srcMap)
  val src2 = MuxLookup(in.microOp.src2Type, in.rs2.data)(srcMap)
  fu.foreach { p =>
    p.io.op := in.microOp.fnOp
    p.io.src1 := src1
    p.io.src2 := src2
  }
  val result =
    MuxCase(0.U, fu.map(p => (p.io.id === in.microOp.fnType, p.io.result)))
  out.rd.data := result
  out.addressInfo := agu.extra.info
  io.redirect := bru.extra.redirect
}
