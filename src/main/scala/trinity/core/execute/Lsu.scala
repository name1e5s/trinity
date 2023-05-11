package trinity.core.execute

import chisel3._
import trinity.core.FnOp
import trinity.core.FuncOpConversions._

object LsuOp {
  def LB = 0.Op
  def LH = 1.Op
  def LW = 2.Op
  def LD = 3.Op
  def LBU = 4.Op
  def LHU = 5.Op
  def LWU = 6.Op

  def SB = 8.Op
  def SH = 9.Op
  def SW = 10.Op
  def SD = 11.Op

  def apply() = FnOp()
}

class Lsu {}
