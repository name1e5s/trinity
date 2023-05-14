package trinity.core.execute.fn

import chisel3._
import chisel3.util._
import trinity.core.Constants._
import trinity.core.execute.fn.FuncOpConversions._
import trinity.util._

object AluOp {
  def ADD = 0.Op
  def SUB = 1.Op
  def SLT = 2.Op
  def SLTU = 3.Op
  def AND = 4.Op
  def OR = 5.Op
  def XOR = 6.Op
  def SLL = 7.Op
  def SRL = 8.Op
  def SRA = 9.Op
  def LUI = 10.Op

  def ADDW = 12.Op
  def SUBW = 13.Op
  def SLLW = 14.Op
  def SRLW = 15.Op
  def SRAW = 16.Op

  def apply() = FnOp()
}

class Alu extends FnModule {
  override def id: UInt = FnType.ALU

  val op = UIntToOH(io.op)
  val src1 = io.src1
  val src2 = io.src2

  val src1W = src1(wLen - 1, 0)
  val src2W = src2(wLen - 1, 0)

  val resultTable = Seq(
    op(AluOp.ADD) -> (src1 + src2),
    op(AluOp.SUB) -> (src1 - src2),
    op(AluOp.SLT) -> Mux(src1.asSInt < src2.asSInt, 1.U, 0.U),
    op(AluOp.SLTU) -> Mux(src1 < src2, 1.U, 0.U),
    op(AluOp.AND) -> (src1 & src2),
    op(AluOp.OR) -> (src1 | src2),
    op(AluOp.XOR) -> (src1 ^ src2),
    op(AluOp.SLL) -> (src1 << src2(5, 0))(xLen - 1, 0).asUInt,
    op(AluOp.SRL) -> (src1 >> src2(5, 0))(xLen - 1, 0).asUInt,
    op(AluOp.SRA) -> (src1.asSInt >> src2(5, 0))(xLen - 1, 0).asUInt,
    op(AluOp.LUI) -> src2,
    op(AluOp.ADDW) -> SignExtension(src1W + src2W),
    op(AluOp.SUBW) -> SignExtension(src1W - src2W),
    op(AluOp.SLLW) -> SignExtension((src1W << src2(4, 0))(wLen - 1, 0)),
    op(AluOp.SRLW) -> SignExtension((src1W >> src2(4, 0))(wLen - 1, 0)),
    op(AluOp.SRAW) -> SignExtension((src1W.asSInt >> src2(4, 0))(wLen - 1, 0))
  )

  val result = Mux1H(resultTable)
  io.result.bits := result
  io.result.valid := true.B
  log(
    "op: %d src: %x %x result: %x",
    io.op,
    src1,
    src2,
    result
  )
}
