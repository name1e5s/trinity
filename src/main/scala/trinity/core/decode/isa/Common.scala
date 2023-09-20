package trinity.core.decode.isa

import chisel3._
import chisel3.util._
import trinity.Constants._
import trinity.core.execute.fn.AluOp
import trinity.core.execute.fn.{FnOp, FnType}
import trinity.util.{DecodeLogic, SignExtension}
import trinity.util.Converters._

object InstType {
  def width = 4

  def X = 0.U(width.W)
  def R = 1.U(width.W)
  def I = 2.U(width.W)
  def S = 3.U(width.W)
  def B = 4.U(width.W)
  def U = 5.U(width.W)
  def J = 6.U(width.W)

  def apply() = UInt(width.W)

  def writeGpr(value: UInt) = value =/= S || value =/= B || value =/= X
}

object SrcType {
  def width = 2

  def REG = 0.U(width.W)
  def IMM = 1.U(width.W)
  def PC = 2.U(width.W)

  def apply() = UInt(width.W)
  def apply(instType: UInt) = {
    val table: List[(BitPat, List[BitPat])] = List(
      BitPat(InstType.I) -> List(REG, IMM),
      BitPat(InstType.U) -> List(PC, IMM),
      BitPat(InstType.J) -> List(PC, IMM)
    )
    val default: List[BitPat] = List(REG, REG)
    val src1 :: src2 :: Nil = DecodeLogic(instType, default, table)
    (src1, src2)
  }
  def usesRs2(instType: UInt) = {
    val p = apply(instType)
    p._2 === REG
  }
}

abstract class InstructionTable {
  val table: List[(BitPat, List[BitPat])]
}

class MicroOp extends Bundle {
  val instType = InstType()
  val fnType = FnType()
  val fnOp = FnOp()

  val src1Type = SrcType()
  val src2Type = SrcType()
}

object MicroOp {
  def apply() = new MicroOp
}

object IsaMicroOpDecoder {
  def illegal: List[BitPat] =
    List(InstType.X, FnType.ALU, AluOp.ADD)

  def apply[T <: InstructionTable](inst: UInt, table: T) = {
    val instType :: fnType :: fnOp :: Nil =
      DecodeLogic(inst, illegal, table.table)
    val op = Wire(MicroOp())
    op.instType := instType
    op.fnType := fnType
    op.fnOp := fnOp

    val srcType = SrcType(instType)
    op.src1Type := srcType._1
    op.src2Type := srcType._2

    op
  }
}

class CommonInfo extends Bundle {
  val rs1 = UInt(gprAddressWidth.W)
  val rs2 = UInt(gprAddressWidth.W)
  val rd = UInt(gprAddressWidth.W)
  val immediate = UInt(xLen.W)
}

object IsaCommonDecoder {
  def apply(inst: UInt, instType: UInt) = {
    val rs1 = inst(19, 15)
    val rs2 = inst(24, 20)
    val rd = inst(11, 7)
    val ohType = UIntToOH(instType)
    val immediateTable = List(
      ohType(InstType.X) -> 0.U,
      ohType(InstType.R) -> 0.U,
      ohType(InstType.I) -> SignExtension(inst(31, 20), xLen),
      ohType(InstType.S) -> SignExtension(inst(31, 25) ## inst(11, 7), xLen),
      ohType(InstType.B) -> SignExtension(
        inst(31) ## inst(7) ## inst(30, 25) ## inst(11, 8) ## false.B,
        xLen
      ),
      ohType(InstType.U) -> SignExtension(inst(31, 12) ## 0.U(12.W), xLen),
      ohType(InstType.J) -> SignExtension(inst(31) ## inst(19, 12) ## inst(20) ## inst(30, 21) ## false.B, xLen)
    )
    val immediate = Mux1H(immediateTable)
    val result = Wire(new CommonInfo)
    result.rs1 := rs1
    result.rs2 := rs2
    result.rd := rd
    result.immediate := immediate

    result
  }
}
