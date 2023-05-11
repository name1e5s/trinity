package trinity.core.execute

import chisel3._
import chisel3.util._
import trinity.core.Constants._
import trinity.core.FnOp
import trinity.core.FuncOpConversions._
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
  def AUIPC = 11.Op

  def ADDW = 12.Op
  def SUBW = 13.Op
  def SLLW = 14.Op
  def SRLW = 15.Op
  def SRAW = 16.Op

  def apply() = FnOp()
}

class AluIO extends Bundle {
  val op = Input(AluOp())
  val pc = Input(UInt(xLen.W))
  val src1 = Input(UInt(xLen.W))
  val src2 = Input(UInt(xLen.W))
  val result = Output(UInt(xLen.W))
}

class Alu extends TrinityModule {
  val io = IO(new AluIO)

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
    op(AluOp.AUIPC) -> (io.pc + src2),
    op(AluOp.ADDW) -> SignExtension(src1W + src2W),
    op(AluOp.SUBW) -> SignExtension(src1W - src2W),
    op(AluOp.SLLW) -> SignExtension((src1W << src2(4, 0))(wLen - 1, 0)),
    op(AluOp.SRLW) -> SignExtension((src1W >> src2(4, 0))(wLen - 1, 0)),
    op(AluOp.SRAW) -> SignExtension((src1W.asSInt >> src2(4, 0))(wLen - 1, 0))
  )

  io.result := Mux1H(resultTable)
  log(
    "op: %d pc: %x src: %x %x result: %x",
    io.op,
    io.pc,
    src1,
    src2,
    Mux1H(resultTable)
  )
}
