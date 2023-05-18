package trinity.bus

import chisel3._
import chisel3.util._
import trinity.Constants._

package object cachebus {
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

  class CacheBusReqBase extends Bundle {
    val rw = Output(CacheBusRw())
    val addr = Output(UInt(addrWidth.W))
    val size = Output(CacheBusSize())

    val wdata = Output(UInt(dataWidth.W))
    val wmask = Output(UInt(dataBytes.W))

    override def toPrintable: Printable = {
      p"rw = $rw, addr = 0x${Hexadecimal(addr)}, size = $size, " +
        p"wmask = 0x${Hexadecimal(wmask)}, wdata = 0x${Hexadecimal(wdata)}"
    }
  }

  object CacheBusReqBase {
    def apply() = new CacheBusReqBase
  }

  class CacheBusReq[T <: Bundle](gen: => T) extends Bundle {
    val base = Output(new CacheBusReqBase)
    val extra = Output(gen)
  }

  object CacheBusReq {
    def apply[T <: Bundle](gen: => T) = new CacheBusReq(gen)
  }

  class CacheBusRespBase extends Bundle {
    val rdata = Output(UInt(dataWidth.W))

    override def toPrintable: Printable = {
      p"rdata = 0x${Hexadecimal(rdata)}"
    }
  }

  object CacheBusRespBase {
    def apply() = new CacheBusRespBase
  }

  class CacheBusResp[T <: Bundle](gen: => T) extends Bundle {
    val base = new CacheBusRespBase
    val extra = Output(gen)
  }

  object CacheBusResp {
    def apply[T <: Bundle](gen: => T) = new CacheBusResp(gen)
  }

  class CacheBusBase extends Bundle {
    val req = Decoupled(CacheBusReqBase())
    val resp = Flipped(Decoupled(CacheBusRespBase()))

    def isWrite = req.valid && req.bits.rw === CacheBusRw.W

    def isRead = req.valid && req.bits.rw === CacheBusRw.R
  }

  object CacheBusBase {
    def apply() = new CacheBusBase
  }

  class CacheBus[T <: Bundle](gen: => T) extends Bundle {
    val req = Decoupled(CacheBusReq(gen))
    val resp = Flipped(Decoupled(CacheBusResp(gen)))

    def isWrite = req.valid && req.bits.base.rw === CacheBusRw.W

    def isRead = req.valid && req.bits.base.rw === CacheBusRw.R
  }

  object CacheBus {
    def apply[T <: Bundle](gen: => T) = new CacheBus(gen)
  }

}
