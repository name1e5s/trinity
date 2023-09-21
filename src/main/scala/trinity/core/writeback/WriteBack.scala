package trinity.core.writeback

import chisel3._
import chisel3.util._
import trinity.core._
import trinity.core.decode.isa.InstType
import trinity.util.TrinityModule

class WriteBackIO extends Bundle {
  val in = Flipped(Decoupled(new ControlFlowBundle))
  val write = Flipped(new RegisterFileWritePort)
}

class WriteBack extends TrinityModule {
  val io = IO(new WriteBackIO)

  val in = io.in.bits
  io.in.ready := true.B
  io.write.valid := io.in.valid && InstType.writeGpr(in.microOp.instType)
  io.write.addr := in.rd.addr
  io.write.data := in.rd.data

  log("valid: %d, addr: %d data: %x", io.write.valid, in.rd.addr, in.rd.data)
}
