package trinity.core.execute.fn

import chisel3._
import trinity.core.CacheBusRw
import trinity.core.Constants._
import trinity.core.execute.fn.FuncOpConversions._

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

  def rw(op: UInt) = Mux(op(3), CacheBusRw.W, CacheBusRw.R)
  def size(op: UInt) = op(1, 0)
}

class AddressInfo extends Bundle {
  val addr = UInt(xLen.W)
  val data = UInt(xLen.W)
}

class AguIO extends Bundle {
  val immediate = Input(UInt(xLen.W))
  val info = Output(new AddressInfo)
}

class Agu extends FnModule {
  override def id: UInt = FnType.LSU

  val extra = IO(new AguIO)

  io.result.bits := 0.U
  io.result.valid := false.B
  extra.info.addr := io.src1 + extra.immediate
  extra.info.data := io.src2
}
