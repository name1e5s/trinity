package trinity.core.execute.fn

import chisel3._
import chisel3.util._
import trinity.Constants._
import trinity.core.execute.fn.FuncOpConversions._
import trinity.util.TrinityModule

object BruOp {
  def J = 0.Op
  def JR = 1.Op
  def EQ = 2.Op
  def NE = 3.Op
  def LT = 4.Op
  def GE = 5.Op
  def LTU = 6.Op
  def GEU = 7.Op

  def apply() = FnOp()
}

class BruIO extends Bundle {
  val valid = Input(Bool())
  val isBranch = Input(Bool())
  val pc = Input(UInt(xLen.W))

  val offset = Input(UInt(xLen.W))
  val nextPc = Input(UInt(xLen.W))
  val redirect = Valid(UInt(xLen.W))
}

class Bru extends FnModule {
  override def id: UInt = FnType.BRU

  val extra = IO(new BruIO)

  val op = UIntToOH(io.op)
  val src1 = io.src1
  val src2 = io.src2

  val sequencePc = extra.pc + 4.U
  val branchPc = extra.pc + extra.offset
  val jrPc = io.src1 + extra.offset
  val jumpPc = Mux(op(BruOp.JR), jrPc, branchPc)

  val eq = src1 === src2
  val ne = !eq
  val lt = src1.asSInt < src2.asSInt
  val ge = !lt
  val ltu = src1 < src2
  val geu = !ltu

  val jumpTable = Array(
    op(BruOp.J) -> true.B,
    op(BruOp.JR) -> true.B,
    op(BruOp.EQ) -> eq,
    op(BruOp.NE) -> ne,
    op(BruOp.LT) -> lt,
    op(BruOp.GE) -> ge,
    op(BruOp.LTU) -> ltu,
    op(BruOp.GEU) -> geu
  )
  val shouldJump = Mux1H(jumpTable) && extra.isBranch

  io.result.bits := sequencePc
  io.result.valid := true.B

  val realNextPc = Mux(shouldJump, jumpPc, sequencePc)
  extra.redirect.bits := realNextPc
  extra.redirect.valid := realNextPc =/= extra.nextPc && extra.valid

  log(
    "pc: %x, op: %d, src: %x %x, jump: %d redirect: %d %x",
    extra.pc,
    io.op,
    src1,
    src2,
    shouldJump,
    extra.redirect.valid,
    extra.redirect.bits
  )
}
