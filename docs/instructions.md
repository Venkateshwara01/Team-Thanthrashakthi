# Instruction Pipeline & Supported Set
## 3-Stage Pipeline Model
###  FETCH: Retrieves the instruction line indexed by the Program Counter (PC) from ProgramMemory.
### DECODE: Cleans assembly text, identifies opcode mnemonics, and extracts operands.
### EXECUTE: Evaluates the operation, updates target registers, recomputes status flags (CY, AC, OV, P), and advances the PC.
### Supported Instruction Set 
(Mandatory 8 Opcodes)

# Instruction Pipeline & Supported Set

## 3-Stage Pipeline Model

### FETCH
Retrieves the instruction line indexed by the Program Counter (PC) from Program Memory.

### DECODE
Cleans assembly text, identifies opcode mnemonics, and extracts operands.

### EXECUTE
Evaluates the operation, updates target registers, recomputes status flags (CY, AC, OV, P), and advances the PC.

### Supported Instruction Set

(Mandatory 8 Opcodes)

| Mnemonic | Operands | Description & Operation |
|----------|----------|-------------------------|
| MOV | A, #imm / B, #imm / A, B | Data transfer into Accumulator or Register B |
| ADD | A, B | Addition: ACC = ACC + B (Updates CY, AC, P) |
| SUB | A, B | Subtraction with Borrow: ACC = ACC - B - CY |
| ANL | A, B | Bitwise logical AND: ACC = ACC & B |
| INC | A | Increments Accumulator by 1 |
| DEC | A | Decrements Accumulator by 1 |
| SJMP | address | Short jump to the specified address |
| END | None | Terminates CPU pipeline execution |
