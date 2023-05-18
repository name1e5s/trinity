package trinity.core

import chisel3._
import chisel3.util._
import trinity.util._
import trinity.Constants._

class RegisterFileReadPort extends Bundle {
  val addr = Input(UInt(gprAddressWidth.W))
  val data = Output(UInt(xLen.W))
}

class RegisterFileWritePort extends Bundle {
  val valid = Input(Bool())
  val addr = Input(UInt(gprAddressWidth.W))
  val data = Input(UInt(xLen.W))
}

class RegisterBypassPort extends RegisterFileWritePort

class RegisterFileIO(val bypassPortLen: Int) extends Bundle {
  val read = Vec(2, new RegisterFileReadPort)
  val write = new RegisterFileWritePort
  val bypass = Vec(bypassPortLen, new RegisterBypassPort)
}

class RegisterFile(val bypassPortLen: Int) extends TrinityModule {
  val io = IO(new RegisterFileIO(bypassPortLen))

  val registerFile = Mem(gprNum, UInt(xLen.W))

  when(io.write.valid) {
    registerFile.write(io.write.addr, io.write.data)
  }

  io.read.foreach { p =>
    val addr = p.addr
    val data = registerFile.read(addr)
    val result = io.bypass.foldRight(data) { (bypass, data) =>
      Mux(bypass.valid && bypass.addr === addr, bypass.data, data)
    }
    p.data := Mux(addr.orR, result, 0.U)
  }
}
