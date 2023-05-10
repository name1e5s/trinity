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

object FuncType {
  def ALU = 0.U
  def MDU = 1.U
  def LSU = 2.U

  def apply() = UInt(2.W)
}

object FuncOp {
  def apply() = UInt(6.W)
}

class RegisterInfo extends Bundle {
  val index = UInt(gprAddressWidth.W)
  val data = UInt(xLen.W)
}

class ControlFlowBundle extends Bundle {
  val instruction = new InstructionBundle

  val funcType = FuncType()
  val funcOp = FuncOp()

  val rs1 = new RegisterInfo
  val rs2 = new RegisterInfo
  val rd = new RegisterInfo
  val immediate = UInt(xLen.W)
}
