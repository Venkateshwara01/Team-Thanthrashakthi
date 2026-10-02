package memory;

public class FifoQueue {
    private final int capacity;
    private final int[] elements;
    private int front = 0;
    private int rear = -1;
    private int size = 0;

    public FifoQueue(int capacity) {
        this.capacity = capacity;
        this.elements = new int[capacity];
    }

    public boolean enqueue(int value) {
        if (isFull()) return false;
        rear = (rear + 1) % capacity;
        elements[rear] = value & 0xFF;
        size++;
        return true;
    }

    public int dequeue() {
        if (isEmpty()) return -1;
        int val = elements[front];
        front = (front + 1) % capacity;
        size--;
        return val;
    }

    public boolean isFull() { return size == capacity; }
    public boolean isEmpty() { return size == 0; }
    public int getSize() { return size; }

    public void reset() {
        front = 0;
        rear = -1;
        size = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            int idx = (front + i) % capacity;
            sb.append(String.format("0x%02X", elements[idx]));
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
