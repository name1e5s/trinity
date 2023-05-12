package trinity.core.decode.isa

import chisel3._
import chisel3.util._
import trinity.util.Converters._
import trinity.core.FnType
import trinity.core.execute.AluOp

object InstType {
  def width = 4

  def X = 0.U(width.W)
  def R = 1.U(width.W)
  def I = 2.U(width.W)
  def S = 3.U(width.W)
  def B = 4.U(width.W)
  def U = 5.U(width.W)
  def J = 6.U(width.W)

  def apply() = UInt(width.W)

  def writeGpr(value: UInt) = value =/= S || value =/= B
}

object SrcType {
  def width = 2

  def REG = 0.U(width.W)
  def IMM = 1.U(width.W)
  def PC = 2.U(width.W)

  def apply() = UInt(width.W)
}

object InstructionConstants {
  def illegal: List[BitPat] =
    List(InstType.X, FnType.ALU, AluOp.ADD, SrcType.REG, SrcType.REG, N)
}

abstract class InstructionTable {
  val table: List[(BitPat, List[BitPat])]
}
