package trinity.core.frontend

import chisel3._
import chisel3.util._
import trinity.core.{CacheBus, CacheBusRw, CacheBusSize, InstructionBundle}
import trinity.util._
import trinity.core.Constants._

class ICacheExtra extends Bundle {
  val pc = UInt(xLen.W)
  val nextPc = UInt(xLen.W)
  val epoch = UInt(1.W) // 1 bit is enough for now
}

class FetcherIO extends Bundle {
  val cache = CacheBus(new ICacheExtra)
  val instruction = Decoupled(new InstructionBundle)

  val mispredictRedirection = Flipped(Valid(UInt(xLen.W)))
  val exceptionRedirection = Flipped(Valid(UInt(xLen.W)))
}

class Fetcher extends TrinityModule {
  val io = IO(new FetcherIO)

  val predictor = Module(new Predictor)

  val redirection = Mux(
    io.exceptionRedirection.valid,
    io.exceptionRedirection,
    io.mispredictRedirection
  )

  // pc info
  val pc = RegInit(pcInitVector.U(xLen.W))
  val pcChange = redirection.valid || io.cache.req.fire
  val sequenceNextPc = pc + 4.U
  val nextPc = Mux(
    redirection.valid,
    redirection.bits,
    Mux(predictor.io.out.valid, predictor.io.out.bits, sequenceNextPc)
  )

  when(pcChange) {
    pc := nextPc
  }

  // predictor next value
  predictor.io.pc.bits := nextPc
  predictor.io.pc.valid := pcChange

  // epoch
  val epoch = RegInit(0.U(1.W))
  when(redirection.valid) {
    epoch := epoch + 1.U
  }

  log(
    p"pc: ${Hexadecimal(pc)}, pcChange: $pcChange, redirection: ${redirection.valid}, ${Hexadecimal(redirection.bits)}, " +
      p"nextPc: ${Hexadecimal(nextPc)}"
  )

  // to bus
  io.cache.req.valid := io.instruction.ready
  io.cache.req.bits.rw := CacheBusRw.R
  io.cache.req.bits.addr := Cat(pc(xLen - 1, 2), 0.U(2.W))
  io.cache.req.bits.size := CacheBusSize.W
  io.cache.req.bits.wdata := 0.U
  io.cache.req.bits.wmask := 0.U
  io.cache.req.bits.extra.pc := pc
  io.cache.req.bits.extra.nextPc := nextPc
  io.cache.req.bits.extra.epoch := epoch

  io.cache.resp.ready := io.instruction.ready || redirection.valid
  io.instruction.bits := DontCare
  io.instruction.bits.instruction := io.cache.resp.bits.rdata
  io.instruction.bits.pc := io.cache.resp.bits.extra.pc
  io.instruction.bits.predictedNextPc := io.cache.resp.bits.extra.nextPc

  io.instruction.valid := io.cache.resp.valid && !redirection.valid && epoch === io.cache.resp.bits.extra.epoch
}
