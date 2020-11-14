package me.chongwish.jjvm;

import java.util.Stack;

import lombok.Getter;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea.Method;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea.RuntimeConstantPool;

/**
 * A frame is used to store data and partial results, as well as to perform dynamic linking, return values for methods, and dispatch exceptions.
 */
@Getter
final class Frame {
    private OperandStack operandStack;
    private LocalVariable localVariable;
    private ThreadResource threadResource;
    private Method method;
    private Bytecode bytecode;

    /**
     * Runtime Constant reference of current method
     */
    private RuntimeConstantPool runtimeConstantPool;

    public Frame(Method method, ThreadResource threadResource) {
        operandStack = new OperandStack();
        localVariable = new LocalVariable(method.getMaxLocals());
        this.method = method;
        this.bytecode = new Bytecode(method.getCode());
        this.runtimeConstantPool = MethodArea.findRuntimeConstantPool(method.getClazz().getClassName());
        this.threadResource = threadResource;
    }

    /**
     * Operand Stack
     */
    final public static class OperandStack {
        /**
         * Stack stack can store a value of type boolean, byte, char, short, int, float, reference in a slot, a value of type long or double in two slots.
         */
        private Stack<Object> stack = new Stack<>();

        public void push(Object value) {
            if (value instanceof Integer) {
                push((int) value);
            } else if (value instanceof Float) {
                push((float) value);
            } else if (value instanceof Long) {
                push((long) value);
            } else if (value instanceof Double) {
                push((double) value);
            } else {
                stack.push(value);
            }
        }

        public void push(int value) {
            stack.push(value);
        }

        public void push(float value) {
            push(Float.floatToRawIntBits(value));
        }

        public void push(long value) {
            push((int) value);
            push((int) (value >> 32));
        }

        public void push(double value) {
            push(Double.doubleToRawLongBits(value));
        }

        public Object current() {
            return stack.peek();
        }

        public Object bottom(int index) {
            return stack.get(stack.size() - 1 - index);
        }

        public Object pop() {
            return stack.pop();
        }

        public int popInt() {
            return (int) pop();
        }

        public float popFloat() {
            return Float.intBitsToFloat(popInt());
        }

        public long popLong() {
            long higher = popInt();
            long lower = popInt();
            return (higher << 32) | lower & 0x0ffffffffL;
        }

        public double popDouble() {
            return Double.longBitsToDouble(popLong());
        }

        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }

    /**
     * Local Variable Array
     */
    final public static class LocalVariable {
        /**
         * Array variables can store a value of type boolean, byte, char, short, int, float, reference in a slot, a value of type long or double in two slots.
         */
        private Object[] variables;

        public LocalVariable(int size) {
            variables = new Object[size];
        }

        public void set(int i, Object value) {
            if (value instanceof Integer) {
                set(i, (int) value);
            } else if (value instanceof Float) {
                set(i, (float) value);
            } else if (value instanceof Long) {
                set(i, (long) value);
            } else if (value instanceof Double) {
                set(i, (double) value);
            } else {
                variables[i] = value;
            }
        }

        public void set(int i, int value) {
            variables[i] = value;
        }

        public void set(int i, float value) {
            set(i, Float.floatToRawIntBits(value));
        }

        public void set(int i, long value) {
            set(i, (int) value);
            set(i + 1, (int) (value >> 32));
        }

        public void set(int i, double value) {
            set(i, Double.doubleToRawLongBits(value));
        }

        public Object get(int i) {
            return variables[i];
        }

        public int getInt(int i) {
            return (int) variables[i];
        }

        public float getFloat(int i) {
            return Float.intBitsToFloat(getInt(i));
        }

        public long getLong(int i) {
            long lower = getInt(i);
            long higher = getInt(i + 1);
            return (higher << 32) | lower & 0x0ffffffffL;
        }

        public double getDouble(int i) {
            return Double.longBitsToDouble(getLong(i));
        }
    }

}
