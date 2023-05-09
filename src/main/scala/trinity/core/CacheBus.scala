package trinity.core

import chisel3._
import chisel3.util._
import trinity.core.Constants._

object CacheBusRw {
  def R = false.B
  def W = true.B

  def apply() = Bool()
}

object CacheBusSize {
  def B = 0.U
  def H = 1.U
  def W = 2.U
  def D = 3.U

  def apply() = UInt(3.W)
}

class CacheBusReq[T <: Bundle](gen: => T) extends Bundle {
  val rw = Output(CacheBusRw())
  val addr = Output(UInt(addrWidth.W))
  val size = Output(CacheBusSize())

  val wdata = Output(UInt(dataWidth.W))
  val wmask = Output(UInt(dataBytes.W))

  val extra = Output(gen)

  override def toPrintable: Printable = {
    p"rw = $rw, addr = 0x${Hexadecimal(addr)}, size = $size, " +
      p"wmask = 0x${Hexadecimal(wmask)}, wdata = 0x${Hexadecimal(wdata)}, " +
      p"extra = $extra"
  }
}

object CacheBusReq {
  def apply[T <: Bundle](gen: => T) = new CacheBusReq(gen)
}

class CacheBusResp[T <: Bundle](gen: => T) extends Bundle {
  val rw = Output(CacheBusRw())
  val rdata = Output(UInt(dataWidth.W))

  val extra = Output(gen)

  override def toPrintable: Printable = {
    p"rw = $rw, rdata = 0x${Hexadecimal(rdata)}, " +
      p"extra = $extra"
  }
}

object CacheBusResp {
  def apply[T <: Bundle](gen: => T) = new CacheBusResp(gen)
}

class CacheBus[T <: Bundle](gen: => T) extends Bundle {
  val req = Decoupled(CacheBusReq(gen))
  val resp = Flipped(Decoupled(CacheBusResp(gen)))

  def isWrite = req.valid && req.bits.rw === CacheBusRw.W
  def isRead = req.valid && req.bits.rw === CacheBusRw.R
}

object CacheBus {
  def apply[T <: Bundle](gen: => T) = new CacheBus(gen)
}
