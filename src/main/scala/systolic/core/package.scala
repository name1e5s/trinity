package systolic

import chisel3._

package object core {
  def ELEMENT_COUNT = 8
  def ELEMENT_WIDTH = 8

  object ElementType {
    def apply() = UInt(ELEMENT_WIDTH.W)
  }

  object ElementCmd {
    def width = 2

    def X = 0.U(width.W)
    def IO = 1.U(width.W)
    def COMPUTE = 2.U(width.W)
    def CLEAR = 3.U(width.W)

    def apply() = UInt(width.W)
  }
}
