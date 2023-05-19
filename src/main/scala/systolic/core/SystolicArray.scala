package systolic.core

import chisel3._
import chisel3.experimental.prefix

class SystolicArrayIO extends Bundle {
  val cmd = Input(ElementCmd())
  val src1 = Input(Vec(ELEMENT_COUNT, ElementType()))
  val src2 = Input(Vec(ELEMENT_COUNT, ElementType()))
  val result = Output(Vec(ELEMENT_COUNT, ElementType()))
}

class SystolicArray extends Module {
  val io = IO(new SystolicArrayIO)

  val pe = (0 until ELEMENT_COUNT).flatMap { i =>
    (0 until ELEMENT_COUNT).map { j =>
      (i, j) -> Module(new ProcessingElement).suggestName(s"pe_${i}_${j}")
    }
  }.toMap
  pe.foreach { case ((i, j), m) =>
    m.io.cmd := io.cmd

    if (j == 0) {
      m.io.src1 := io.src1(i)
    }
    if (i == 0) {
      m.io.src2 := io.src2(j)
    }

    val right = (i, j + 1)
    if (pe.contains(right)) {
      val rPe = pe(right)
      rPe.io.src1 := m.io.res1
    } else {
      io.result(i) := m.io.res1
    }

    val down = (i + 1, j)
    if (pe.contains(down)) {
      val dPe = pe(down)
      dPe.io.src2 := m.io.res2
    }
  }
}
