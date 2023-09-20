CC		:= 	riscv64-unknown-elf-gcc

CFLAGS 	:= 	-mcmodel=medany -static -std=gnu99 -O2 \
	-ffast-math -fno-common -fno-builtin-printf \
	-fno-tree-loop-distribute-patterns -static \
	-nostdlib -nostartfiles -Tlink.ld -I./include

OBJDUMP := 	riscv64-unknown-elf-objdump

SRC 	:=	main.S \
			isa/add.o \
			isa/addi.o

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