package me.chongwish.jjvm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FrameTest {
    @Test
    public void testOperandStack() {
        Frame.OperandStack stack = new Frame.OperandStack();
        stack.push(null);
        stack.push(100);
        stack.push(-100);
        stack.push(2997924580l);
        stack.push(-2997924580l);
        stack.push(3.1415926f);
        stack.push(2.71828182845);
        stack.push(true);
        stack.push(false);
        stack.push("abc");
        FrameTest t = new FrameTest();
        stack.push(t);

        assertEquals(stack.pop(), t);
        assertEquals(stack.pop(), "abc");
        assertEquals(stack.pop(), false);
        assertEquals(stack.pop(), true);
        assertEquals(stack.popDouble(), 2.71828182845, 0.00000000000001);
        assertEquals(stack.popFloat(), 3.1415926f, 0.0000000001f);
        assertEquals(stack.popLong(), -2997924580l);
        assertEquals(stack.popLong(), 2997924580l);
        assertEquals(stack.pop(), -100);
        assertEquals(stack.pop(), 100);
        assertEquals(stack.pop(), null);
    }

    @Test
    public void testLocalVariable() {
        Frame.LocalVariable variable = new Frame.LocalVariable(100);
        variable.set(1, 100);
        variable.set(2, -100);
        variable.set(3, 2997924580l);
        variable.set(5, -2997924580l);
        variable.set(7, 3.1415926f);
        variable.set(8, 2.71828182845);
        variable.set(10, true);
        variable.set(11, false);
        variable.set(12, "abc");
        FrameTest t = new FrameTest();
        variable.set(13, t);
        variable.set(14, null);

        assertEquals(variable.get(13), t);
        assertEquals(variable.get(12), "abc");
        assertEquals(variable.get(11), false);
        assertEquals(variable.get(10), true);
        assertEquals(variable.getDouble(8), 2.71828182845, 0.00000000000001);
        assertEquals(variable.getFloat(7), 3.1415926f, 0.0000000001f);
        assertEquals(variable.getLong(5), -2997924580l);
        assertEquals(variable.getLong(3), 2997924580l);
        assertEquals(variable.get(2), -100);
        assertEquals(variable.getInt(1), 100);
        assertEquals(variable.get(14), null);
    }
}
