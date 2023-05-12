package trinity.core

import chisel3._
import chisel3.util._
import trinity.util._
import trinity.core.Constants._

class InstructionBundle extends Bundle {
  val pc = UInt(xLen.W)
  val predictedNextPc = UInt(xLen.W)

  val instruction = UInt(instructionWidth.W)
}

object FnType {
  def width = 2

  def ALU = 0.U(width.W)
  def BRU = 1.U(width.W)
  def MDU = 2.U(width.W)
  def LSU = 3.U(width.W)

  def apply() = UInt(width.W)
}

object FnOp {
  def opWidth = 6

  def apply() = UInt(opWidth.W)
}

object FuncOpConversions {

  implicit class fromBigIntToFuncOp(value: BigInt) {
    def Op: UInt = value.U(FnOp.opWidth.W) // scalastyle:ignore method.name
  }

  implicit class fromIntToFuncOp(int: Int) extends fromBigIntToFuncOp(int)

  implicit class fromLongToFuncOp(long: Long) extends fromBigIntToFuncOp(long)
}

class RegisterInfo extends Bundle {
  val index = UInt(gprAddressWidth.W)
  val data = UInt(xLen.W)
}

class ControlFlowBundle extends Bundle {
  val instruction = new InstructionBundle

  val fnType = FnType()
  val fnOp = FnOp()

  val rs1 = new RegisterInfo
  val rs2 = new RegisterInfo
  val rd = new RegisterInfo
  val immediate = UInt(xLen.W)
}
