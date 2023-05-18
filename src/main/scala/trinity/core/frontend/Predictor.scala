package trinity.core.frontend

import chisel3._
import chisel3.util._
import trinity.util._
import trinity.Constants._

class PredictorIO extends Bundle {
  val pc = Flipped(Valid(UInt(xLen.W)))
  val out = Valid(UInt(xLen.W))
}

class Predictor extends TrinityModule {
  val io = IO(new PredictorIO)

  log(p"pc_valid: ${io.pc.valid}, pc: 0x${Hexadecimal(io.pc.bits)}")

  io.out.valid := false.B
  io.out.bits := DontCare
}
