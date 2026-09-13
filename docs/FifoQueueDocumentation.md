# Queue Documentation

## 1. What It Is

 The Queue is a 3-slot temporary storage buffer used in the microcontroller simulator.

It follows the First-In, First-Out (FIFO) principle, meaning the first value added to the queue is the first value removed.

## 2. Queue Instructions
   
|Instruction      	 |           Description                                                                       |
|-------------------|---------------------------------------------------------------------------------------------|
|ENQ A             	| Adds the value currently stored in the Accumulator (ACC) to the back of the queue.          |
|DEQ                |	Removes the value from the front of the queue and places it back into the Accumulator (ACC).|
