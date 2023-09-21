#include "riscv_test.h"

.section .text.init
.align 6
.globl _start
_start:
    INIT_XREG
    li  x30, 0
    li  x31, 0
    jal x31, test_1_add
    jal x31, test_2_addi
    jal x31, test_3_addiw
    jal x31, test_4_addw
    jal x31, test_5_and
    jal x31, test_6_andi
    jal x31, test_7_or
    jal x31, test_8_ori
    jal x31, test_9_sll
    jal x31, test_10_slli
    jal x31, test_11_sllw
    jal x31, test_12_slliw
    jal x31, test_13_slt
    jal x31, test_14_slti
    jal x31, test_15_sltiu
    jal x31, test_16_sltu
    jal x31, test_17_sra
    jal x31, test_18_srai
    jal x31, test_19_sraiw
    jal x31, test_20_sraw
    jal x31, test_21_srl
    jal x31, test_22_srli
    jal x31, test_23_srliw
    jal x31, test_24_srlw
    jal x31, test_25_sub
    jal x31, test_26_subw
    jal x31, test_27_xor
    jal x31, test_28_xori
    jal x31, test_29_lui
    jal x31, test_30_beq
    jal x31, test_31_bge
    jal x31, test_32_bgeu
    jal x31, test_33_blt
    jal x31, test_34_bltu
    jal x31, test_35_bne
    jal x31, test_36_jal
    jal x31, test_37_jalr
    lui a5, 0xffff
    sll a5, a5, 4
    sd x0, 16(a5)
    nop
    nop
    nop
    nop
    nop