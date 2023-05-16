package trinity.core.execute

import chisel3._
import chisel3.util._
import trinity.core.Constants._
import trinity.util.TrinityModule

package object fn {
  object FnType {
    def width = 2

    def ALU = 0.U(width.W)

    def BRU = 1.U(width.W)

    def MDU = 2.U(width.W)

    def LSU = 3.U(width.W)

    def apply() = UInt(width.W)
    def isMfn(fnType: UInt) =
      Seq(FnType.LSU, FnType.MDU).map(_ === fnType).reduce(_ || _)
  }

  object FnOp {
    def opWidth = 6

    def apply() = UInt(opWidth.W)
  }

  object FuncOpConversions {

    implicit class fromBigIntToFuncOp(value: BigInt) {
      def Op: UInt = value.U(FnOp.opWidth.W) // scalastyle:ignore method.name
    }

    implicit class fromIntToFuncOp(int: Int) extends fromBigIntToFuncOp(int)

    implicit class fromLongToFuncOp(long: Long) extends fromBigIntToFuncOp(long)
  }

  class FnIO extends Bundle {
    val id = Output(FnType())
    val op = Input(FnOp())
    val src1 = Input(UInt(xLen.W))
    val src2 = Input(UInt(xLen.W))
    val result = Valid(UInt(xLen.W))
  }

  abstract class FnModule extends TrinityModule {
    def id: UInt
    val io = IO(new FnIO)

    io.id := id
  }
}
