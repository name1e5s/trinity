package trinity.util

import chisel3._
import chisel3.util._

class PipelineStageIO[T <: Data](gen: => T) extends Bundle {
  val flush = Input(Bool())
  val in = Flipped(Decoupled(gen))
  val out = Decoupled(gen)
}

class PipelineStage[T <: Data](gen: => T) extends TrinityModule {
  val io = IO(new PipelineStageIO(gen))

  val valid = RegInit(false.B)
  val data = Reg(gen)

  io.in.ready := io.out.ready || io.flush
  io.out.valid := valid
  io.out.bits := data

  when(!valid || io.out.fire || io.flush) {
    valid := io.in.fire && !io.flush
    data := io.in.bits
  }
}

object PipelineStage {
  def apply[T <: Data](gen: => T) = new PipelineStage(gen)
}
