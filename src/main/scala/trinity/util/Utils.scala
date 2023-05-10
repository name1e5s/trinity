package trinity.util

import chisel3._
import chisel3.util._
import chisel3.experimental.AffectsChiselPrefix

object Converters {
  implicit def uIntToBitPat(x: UInt) = BitPat(x)
}

object SignExtension {
  def apply[T <: Bits](data: T, width: Int = 64) = {
    val result = Wire(SInt(width.W))
    result := data.asSInt
    result.asUInt
  }
}

class SimpleCounter(range: Int) extends AffectsChiselPrefix {
  assert(isPow2(range), "range must be a pow of 2")

  val counter = RegInit(0.U((log2Ceil(range) + 1).W))
  val done = counter.head(1).asBool

  counter := counter + !done.asUInt
}

object SimpleCounter {
  def apply(range: Int) = new SimpleCounter(range)
}

object SimpleGlobalTimer {
  def apply() = {
    val counter = RegInit(0.U(32.W))
    counter := counter + 1.U
    counter
  }
}

object BinaryMuxLookUp {
  def apply[S <: Bits, T <: Data](sel: S, in: Seq[(BitPat, T)]): (Bool, T) = {
    val map = in.map(p => (p._1 === sel.asUInt, p._2))
    (map.map(_._1).reduce(_ || _), Mux1H(map))
  }
}

object BinaryMuxLookUpWithDefault {
  def apply[S <: Bits, T <: Data](
      sel: S,
      default: T,
      in: Seq[(BitPat, T)]
  ): T = {
    val (valid, result) = BinaryMuxLookUp(sel, in)
    Mux(valid, result, default)
  }
}
