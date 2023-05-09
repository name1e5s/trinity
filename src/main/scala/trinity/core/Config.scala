package trinity.core

import chisel3._
import chisel3.util._

object Constants {
  val hartIdLen = 2

  val xLen = 64
  val wLen = 32

  val pcInitVector = 0x8000_0000L

  val instructionWidth = 32
  val instructionBytes = instructionWidth / 8

  val gprNum = 32
  val gprAddressWidth = log2Ceil(gprNum)

  val addrWidth = xLen
  val dataWidth = xLen
  val dataBytes = xLen / 8
}
