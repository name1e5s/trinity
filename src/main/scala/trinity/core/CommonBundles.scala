package trinity.core

import chisel3._
import chisel3.util._
import trinity.util._
import trinity.core.Constants._

class InstructionBundle extends Bundle {
  val pc = UInt(xLen.W)
  val instruction = UInt(instructionWidth.W)

  val predictedNextPc = UInt(xLen.W)
  val redirection = Valid(UInt(xLen.W))

  val isBranchInstruction = Bool()
}
