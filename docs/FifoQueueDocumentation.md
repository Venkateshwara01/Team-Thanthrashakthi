# Queue Documentation

## 1. What It Is

 The Queue is a 3-slot temporary storage buffer used in the microcontroller simulator.

It follows the First-In, First-Out (FIFO) principle, meaning the first value added to the queue is the first value removed.

## 2. Queue Instructions
   
|Instruction      	 |           Description                                                                       |
|-------------------|---------------------------------------------------------------------------------------------|
|ENQ A             	| Adds the value currently stored in the Accumulator (ACC) to the back of the queue           |
|DEQ                |	Removes the value from the front of the queue and places it back into the Accumulator (ACC) |

### 3. Error Protection

The queue includes protection against invalid operations:

- Full Queue: The queue can store a maximum of 3 values. If an attempt is made to add a 4th value, the addition is safely blocked.
- Empty Queue: If an attempt is made to remove a value when the queue is empty, the operation is safely stopped without causing the simulator to crash.

### 4. Proof of Work – Console Output

The following execution demonstrates how the queue works:

1. MOV A, #10  --> ACC = 0x0A | Queue = []
2. ENQ A       --> ACC = 0x0A | Queue = [0x0A]
3. MOV A, #20  --> ACC = 0x14 | Queue = [0x0A]
4. ENQ A       --> ACC = 0x14 | Queue = [0x0A, 0x14]
5. DEQ         --> ACC = 0x0A | Queue = [0x14]
   
