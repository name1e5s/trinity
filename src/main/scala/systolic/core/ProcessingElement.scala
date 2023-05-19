package systolic.core

import chisel3._
import chisel3.util._

class ProcessingElementIO extends Bundle {
  val cmd = Input(ElementCmd())
  val src1 = Input(ElementType())
  val src2 = Input(ElementType())
  val res1 = Output(ElementType())
  val res2 = Output(ElementType())
}

class ProcessingElement extends Module {
  val io = IO(new ProcessingElementIO)

  val regSrc1 = Reg(ElementType())
  val regSrc2 = Reg(ElementType())
  val regAcc = Reg(ElementType())

  io.res1 := 0.U
  io.res2 := 0.U

  switch(io.cmd) {
    is(ElementCmd.IO) {
      regAcc := io.src1
      io.res1 := regAcc
    }
    is(ElementCmd.COMPUTE) {
      regSrc1 := io.src1
      io.res1 := regSrc1

      regSrc2 := io.src2
      io.res2 := regSrc2

      regAcc := regAcc + io.src1 * io.src2
    }
    is(ElementCmd.CLEAR) {
      regSrc1 := 0.U
      regSrc2 := 0.U
      regAcc := 0.U
    }
  }
}
