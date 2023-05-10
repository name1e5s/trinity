package trinity.core.execute

import chisel3._
import trinity.core.FuncOp

object LsuOp {
  def LB = 0.U
  def LH = 1.U
  def LW = 2.U
  def LD = 3.U
  def LBU = 4.U
  def LHU = 5.U
  def LWU = 6.U

  def SB = 8.U
  def SH = 9.U
  def SW = 10.U
  def SD = 11.U

  def apply() = FuncOp()
}

class Lsu {}
