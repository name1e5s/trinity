package trinity.core.decode.isa

import chisel3._
import chisel3.util._
import trinity.util.Converters._
import trinity.core.FnType
import trinity.core.execute.AluOp

object InstType {
  def X = 0.U
  def R = 1.U
  def I = 2.U
  def S = 3.U
  def B = 4.U
  def U = 5.U
  def J = 6.U

  def apply() = UInt(4.W)
}

object SrcType {
  def REG = 0.U
  def IMM = 1.U
  def PC = 2.U

  def apply() = UInt(2.W)
}

object InstructionConstants {
  def Y = BitPat("b1")
  def N = BitPat("b0")

  def illegal: List[BitPat] =
    List(InstType.X, FnType.ALU, AluOp.ADD, SrcType.REG, SrcType.REG, N)
}

abstract class InstructionTable {
  import InstructionConstants.{Y, N, illegal}
}
