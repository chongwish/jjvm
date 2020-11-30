package me.chongwish.jjvm;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;

/**
 * Class Bytecode is a encapsulation of bytecode. It make the operation of bytecode like the way to operate class ByteBuffer.
 */
final class Bytecode {
    /**
     * bytecode
     */
    private byte[] code;

    /**
     * The index of bytecode
     */
    @Getter
    @Setter
    private int pc;

    public Bytecode(byte[] code) {
        this(code, 0);
    }

    public Bytecode(byte[] code, int pc) {
        this.code = code;
        this.pc = pc;
    }

    /**
     * Get a 8-bit value from bytecode, and the index plus 1.
     */
    public byte get() {
        return code[pc++];
    }

    /**
     * Get a 8-bit unsigned value from bytecode, and the index plus 1.
     */
    public int getU1() {
        int a = code[pc++];
        return a & 0xff;
    }

    /**
     * Get a 16-bit value from bytecode, and the index plus 2.
     */
    public int getShort() {
        int result = peekChar();
        pc += 2;
        return result;
    }

    /**
     * Get a 16-bit unsigned value from bytecode.
     */
    public int peekShort() {
        int a = code[pc];
        int b = code[pc + 1];
        return a << 8 | b;
    }

    /**
     * Get a 16-bit unsigned value from bytecode, and the index plus 2.
     */
    public int getChar() {
        int result = peekChar();
        pc += 2;
        return result;
    }

    /**
     * Get a 16-bit unsigned value from bytecode.
     */
    public int peekChar() {
        int a = code[pc];
        int b = code[pc + 1] & 0xff;
        return a << 8 | b;
    }

    /**
     * Get a 32-bit unsigned value from bytecode, and the index plus 4.
     */
    public int getInt() {
        int result = peekInt();
        pc += 4;
        return result;
    }

    /**
     * Get a 32-bit unsigned value from bytecode.
     */
    public int peekInt() {
        int a = code[pc];
        int b = code[pc + 1];
        int c = code[pc + 2];
        int d = code[pc + 3] & 0xff;
        return a << 24 | b << 16 | c << 8 | d;
    }

    /**
     * Offset the index of bytecode.
     */
    public void jump(Supplier<Integer> s) {
        pc += s.get() - 1;
    }

    /**
     * 16-bit Offset the index of bytecode.
     */
    public void jump() {
        jump(this::peekChar);
    }

    /**
     * EOF helper function.
     */
    public boolean eof() {
        return pc >= code.length - 1;
    }
}
