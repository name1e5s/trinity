package trinity.core.decode

import chisel3._
import chisel3.util._
import trinity.core._
import trinity.core.decode.isa.{
  InstType,
  IsaCommonDecoder,
  IsaMicroOpDecoder,
  Rv64ITable,
  SrcType
}
import trinity.core.execute.fn.FnType
import trinity.util.TrinityModule

class DecoderIO extends Bundle {
  val in = Flipped(Decoupled(new ControlFlowBundle))
  val out = Decoupled(new ControlFlowBundle)
  val read = Flipped(Vec(2, new RegisterFileReadPort))
  val mfuAllReady = Input(Bool())
  val feedBack = Flipped(Vec(2, Valid(new ControlFlowBundle)))
}

class Decoder extends TrinityModule {
  val io = IO(new DecoderIO)

  io.in <> io.out

  val out = io.out.bits
  val read = io.read

  val microOp =
    IsaMicroOpDecoder(io.in.bits.instruction.instruction, new Rv64ITable)
  out.microOp := microOp

  val commonInfo =
    IsaCommonDecoder(io.in.bits.instruction.instruction, microOp.instType)
  out.rs1.addr := commonInfo.rs1
  out.rs2.addr := commonInfo.rs2
  out.rd.addr := commonInfo.rd
  out.immediate := commonInfo.immediate

  val hasHazard = !io.mfuAllReady || io.feedBack
    .map { p =>
      val instType = p.bits.microOp.instType
      val fnType = p.bits.microOp.fnType
      val rd = p.bits.rd.addr
      val rs1Hazard = rd === commonInfo.rs1
      val rs2Hazard = SrcType.usesRs2(microOp.instType) && rd === commonInfo.rs2
      val hasHazard = rs1Hazard || rs2Hazard
      val result =
        io.in.valid && p.valid && FnType.isMfn(fnType) && InstType.writeGpr(
          instType
        ) && hasHazard
      result
    }
    .reduce(_ || _)

  log(p"io.mfuAllReady ${io.mfuAllReady}")

  io.in.ready := io.out.ready && !hasHazard
  io.out.valid := io.in.valid && !hasHazard

  log(
    "in_ready: %d in_valid: %d out_ready: %d out_valid: %d hazard: %d",
    io.in.ready,
    io.in.valid,
    io.out.ready,
    io.out.valid,
    hasHazard
  )

  read(0).addr := commonInfo.rs1
  read(1).addr := commonInfo.rs2
  out.rs1.data := read(0).data
  out.rs2.data := read(1).data
  log(
    "pc: %x ins: %x rs1: %d %x rs2: %d %x rd: %d imm: %x",
    out.instruction.pc,
    out.instruction.instruction,
    out.rs1.addr,
    out.rs1.data,
    out.rs2.addr,
    out.rs2.data,
    out.rd.addr,
    out.immediate
  )
}
