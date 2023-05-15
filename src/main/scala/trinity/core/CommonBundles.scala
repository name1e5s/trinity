package trinity.core

import chisel3._
import trinity.core.Constants._
import trinity.core.decode.isa.MicroOp
import trinity.core.execute.fn.AddressInfo

class InstructionBundle extends Bundle {
  val pc = UInt(xLen.W)
  val predictedNextPc = UInt(xLen.W)

  val instruction = UInt(instructionWidth.W)
}

class RegisterInfo extends Bundle {
  val addr = UInt(gprAddressWidth.W)
  val data = UInt(xLen.W)
}

class ControlFlowBundle extends Bundle {
  val instruction = new InstructionBundle
  val microOp = MicroOp()

  val rs1 = new RegisterInfo
  val rs2 = new RegisterInfo
  val rd = new RegisterInfo
  val immediate = UInt(xLen.W)

  val addressInfo = new AddressInfo

  override def toPrintable: Printable = p"<ControlFlowBundle>"
}
