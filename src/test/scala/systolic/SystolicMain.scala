package systolic

import chisel3._
import chisel3.stage.ChiselGeneratorAnnotation

class MatMul extends Systolic {
  val N1, N2, N3 = 1

  val i = Iterator(0, N1)
  val j = Iterator(0, N2)
  val k = Iterator(0, N3)

  val A = Input(i, k)
  val B = Input(k, j)
  val C = Output(i, j)

  val a, b = Local(16)
  val c = Local(32)

  // Inputs
  a(i, 0, k) := A(i, k)
  b(0, j, k) := B(k, j)
  c(i, j, 0) := 0

  // Calculations
  a(i, j, k) := a(i, j - 1, k)
  b(i, j, k) := b(i - 1, j, k)
  c(i, j, k) := c(i, j, k - 1) + (a(i, j - 1, k) * b(i - 1, j, k))

  // Outputs
  C(i, j) := c(i, j, N3)

  // Space-time transformation
  // Output-stationary
  val mesh = spaceTimeTransform(Seq(Seq(1, 0, 0), Seq(0, 1, 0), Seq(1, 1, 1)))
}

object SystolicMain {
  def main(args: Array[String]): Unit = {
    (new chisel3.stage.ChiselStage).execute(
      Array("-X", "verilog"),
      Seq(
        ChiselGeneratorAnnotation(() => (new MatMul).mod)
      )
    )
  }
}
