CC		:= 	riscv64-unknown-elf-gcc

CFLAGS 	:= 	-mcmodel=medany -static -std=gnu99 -O2 \
	-ffast-math -fno-common -fno-builtin-printf \
	-fno-tree-loop-distribute-patterns -static \
	-nostdlib -nostartfiles -Tlink.ld -I./include

OBJDUMP := 	riscv64-unknown-elf-objdump

SRC 	:=	main.S 			\
			isa/1_add.o 	\
			isa/2_addi.o 	\
			isa/3_addiw.o 	\
			isa/4_addw.o 	\
			isa/5_and.o 	\
			isa/6_andi.o 	\
			isa/7_or.o 		\
			isa/8_ori.o 	\
			isa/9_sll.o 	\
			isa/10_slli.o 	\
			isa/11_sllw.o 	\
			isa/12_slliw.o  \
			isa/13_slt.o 	\
			isa/14_slti.o 	\
			isa/15_sltiu.o 	\
			isa/16_sltu.o 	\
			isa/17_sra.o 	\
			isa/18_srai.o 	\
			isa/19_sraiw.o 	\
			isa/20_sraw.o 	\
			isa/21_srl.o 	\
			isa/22_srli.o 	\
			isa/23_srliw.o 	\
			isa/24_srlw.o 	\
			isa/25_sub.o 	\
			isa/26_subw.o	\
			isa/27_xor.o 	\
			isa/28_xori.o	\
			isa/29_lui.o	\
			isa/30_beq.o	\
			isa/31_bge.o	\
			isa/32_bgeu.o	\
			isa/33_blt.o	\
			isa/34_bltu.o	\
			isa/35_bne.o	\
			isa/36_jal.o	\
			isa/37_jalr.o

TARGET_DIR 	:= target

TARGET_ELF 	:= test.elf

TARGET_DMP 	:= test.S

all: $(SRC)
	mkdir -p $(TARGET_DIR)
	$(CC) $(CFLAGS) $(SRC) -o $(TARGET_DIR)/$(TARGET_ELF)
	$(OBJDUMP) -d $(TARGET_DIR)/$(TARGET_ELF) > $(TARGET_DIR)/$(TARGET_DMP)

%.o: %.S
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -rf isa/*.o
	rm -rf $(TARGET_DIR)