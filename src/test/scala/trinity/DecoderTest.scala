package trinity

import chisel3._
import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.util.BitPat
import chisel3.util.experimental.decode._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

class DecoderTableSpec extends AnyFreeSpec with ChiselScalatestTester {
  /*
   * Assuming a simple ALU instruction scheme
   *      | Name Width Register
   * addb | 0 0 XX
   * addw | 0 1 XX
   * subb | 1 0 XX
   * subw | 1 1 XX
   *
   * Then the operator type and width can be generated by its name
   */
  object Op {
    val add = "0"
    val sub = "1"
  }

  case class Insn(val op: String, val wide: Boolean) extends DecodePattern {
    override def bitPat: BitPat = BitPat(
      "b" + op + (if (wide) "1" else "0") + "??"
    )
  }

  object IsWideOp extends BoolDecodeField[Insn] {
    override def name = "iswide"

    override def genTable(i: Insn): BitPat = {
      if (i.wide) y else n
    }
  }

  object IsAddOp extends DecodeField[Insn, UInt] {
    override def chiselType = UInt(4.W)
    override def name = "isadd"

    override def genTable(i: Insn): BitPat = {
      if (i.op == Op.add) BitPat.Y(4) else BitPat.N(4)
    }
  }

  class ExampleALUDecoder extends Module {
    val io = IO(new Bundle {
      val inst = Input(UInt(4.W))
      val isWideOp = Output(Bool())
      val isAddOp = Output(UInt(4.W))
    })

    val allInstructions = Seq(
      Insn(Op.add, true),
      Insn(Op.add, false),
      Insn(Op.sub, true),
      Insn(Op.sub, false)
    )

    val decodeTable = new DecodeTable(allInstructions, Seq(IsWideOp, IsAddOp))
    val decodedBundle = decodeTable.decode(io.inst)
    io.isWideOp := decodedBundle(IsWideOp)
    io.isAddOp := decodedBundle(IsAddOp)
  }

  "DecoderTable should elaborate a decoder" in {
    (new chisel3.stage.ChiselStage).execute(
      Array("-X", "verilog"),
      Seq(ChiselGeneratorAnnotation(() => new ExampleALUDecoder))
    )
  }

  "DecoderTable should decode every field" in {
    test(new ExampleALUDecoder) { dut =>
      val input = "b0100".U(4.W)
      dut.io.inst.poke(input)
      dut.io.isAddOp.expect(15)
    }
  }
}