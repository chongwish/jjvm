package me.chongwish.jjvm;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import me.chongwish.jjvm.RuntimeDataArea.Heap;
import me.chongwish.jjvm.RuntimeDataArea.JavaStack;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea;

/**
 * Implement the instruction of the jvm defined.
 * <p>
 * <b>Usage:</b>
 * <p>
 * {@code   Instruction.Set.get(instruction-code).apply(frame, bytecode)}
 * <p>
 * The specifection of instruction:
 * 
 * @link https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-6.html
 */
final class Instruction {
    /**
     * A map of the instruction set.
     */
    final public static Map<Integer, BiConsumer<Frame, Bytecode>> Set = new HashMap<>();

    static {
        // nop
        Set.put(0x00, (frame, bytecode) -> {
        });

        // aconst_null
        Set.put(0x01, Helper.pushValueToStack(null));

        // iconst_m1
        Set.put(0x02, Helper.pushValueToStack(-1));

        // iconst_0
        Set.put(0x03, Helper.pushValueToStack(0));

        // iconst_1
        Set.put(0x04, Helper.pushValueToStack(1));

        // iconst_2
        Set.put(0x05, Helper.pushValueToStack(2));

        // iconst_3
        Set.put(0x06, Helper.pushValueToStack(3));

        // iconst_4
        Set.put(0x07, Helper.pushValueToStack(4));

        // iconst_5
        Set.put(0x08, Helper.pushValueToStack(5));

        // lconst_0
        Set.put(0x09, Helper.pushValueToStack(0l));

        // lconst_1
        Set.put(0x0a, Helper.pushValueToStack(1l));

        // fconst_0
        Set.put(0x0b, Helper.pushValueToStack(0.0f));

        // fconst_1
        Set.put(0x0c, Helper.pushValueToStack(1.0f));

        // fconst_2
        Set.put(0x0d, Helper.pushValueToStack(2.0f));

        // dconst_0
        Set.put(0x0e, Helper.pushValueToStack(0.0));

        // dconst_1
        Set.put(0x0f, Helper.pushValueToStack(1.0));

        // bipush
        Set.put(0x10, (frame, bytecode) -> {
            frame.getOperandStack().push(bytecode.get());
        });

        // sipush
        Set.put(0x11, (frame, bytecode) -> {
            frame.getOperandStack().push(bytecode.getShort());
        });

        // ldc
        Set.put(0x12, Helper.LdcOperate(bytecode -> bytecode.getU1()));

        // ldc_w
        Set.put(0x13, Helper.LdcOperate(bytecode -> bytecode.getChar()));

        // ldc2_w
        Set.put(0x14, Helper.LdcOperate(bytecode -> bytecode.getChar()));

        // iload
        Set.put(0x15, Helper.pushToStackFromArray(bytecode -> bytecode.getU1(),
                (index, localVariable) -> localVariable.getInt(index)));

        // lload
        Set.put(0x16, Helper.pushToStackFromArray(bytecode -> bytecode.getU1(),
                (index, localVariable) -> localVariable.getLong(index)));

        // fload
        Set.put(0x17, Helper.pushToStackFromArray(bytecode -> bytecode.getU1(),
                (index, localVariable) -> localVariable.getFloat(index)));

        // dload
        Set.put(0x18, Helper.pushToStackFromArray(bytecode -> bytecode.getU1(),
                (index, localVariable) -> localVariable.getDouble(index)));

        // aload
        Set.put(0x19, Helper.pushToStackFromArray(bytecode -> bytecode.getU1(),
                (index, localVariable) -> localVariable.get(index)));

        // iload_0
        Set.put(0x1a,
                Helper.pushToStackFromArray(bytecode -> 0, (index, localVariable) -> localVariable.getInt(index)));

        // iload_1
        Set.put(0x1b,
                Helper.pushToStackFromArray(bytecode -> 1, (index, localVariable) -> localVariable.getInt(index)));

        // iload_2
        Set.put(0x1c,
                Helper.pushToStackFromArray(bytecode -> 2, (index, localVariable) -> localVariable.getInt(index)));

        // iload_3
        Set.put(0x1d,
                Helper.pushToStackFromArray(bytecode -> 3, (index, localVariable) -> localVariable.getInt(index)));

        // lload_0
        Set.put(0x1e,
                Helper.pushToStackFromArray(bytecode -> 0, (index, localVariable) -> localVariable.getLong(index)));

        // lload_1
        Set.put(0x1f,
                Helper.pushToStackFromArray(bytecode -> 1, (index, localVariable) -> localVariable.getLong(index)));

        // lload_2
        Set.put(0x20,
                Helper.pushToStackFromArray(bytecode -> 2, (index, localVariable) -> localVariable.getLong(index)));

        // lload_3
        Set.put(0x21,
                Helper.pushToStackFromArray(bytecode -> 3, (index, localVariable) -> localVariable.getLong(index)));

        // fload_0
        Set.put(0x22,
                Helper.pushToStackFromArray(bytecode -> 0, (index, localVariable) -> localVariable.getFloat(index)));

        // fload_1
        Set.put(0x23,
                Helper.pushToStackFromArray(bytecode -> 1, (index, localVariable) -> localVariable.getFloat(index)));

        // fload_2
        Set.put(0x24,
                Helper.pushToStackFromArray(bytecode -> 2, (index, localVariable) -> localVariable.getFloat(index)));

        // fload_3
        Set.put(0x25,
                Helper.pushToStackFromArray(bytecode -> 3, (index, localVariable) -> localVariable.getFloat(index)));

        // dload_0
        Set.put(0x26,
                Helper.pushToStackFromArray(bytecode -> 0, (index, localVariable) -> localVariable.getDouble(index)));

        // dload_1
        Set.put(0x27,
                Helper.pushToStackFromArray(bytecode -> 1, (index, localVariable) -> localVariable.getDouble(index)));

        // dload_2
        Set.put(0x28,
                Helper.pushToStackFromArray(bytecode -> 2, (index, localVariable) -> localVariable.getDouble(index)));

        // dload_3
        Set.put(0x29,
                Helper.pushToStackFromArray(bytecode -> 3, (index, localVariable) -> localVariable.getDouble(index)));

        // aload_0
        Set.put(0x2a, Helper.pushToStackFromArray(bytecode -> 0, (index, localVariable) -> localVariable.get(index)));

        // aload_1
        Set.put(0x2b, Helper.pushToStackFromArray(bytecode -> 1, (index, localVariable) -> localVariable.get(index)));

        // aload_2
        Set.put(0x2c, Helper.pushToStackFromArray(bytecode -> 2, (index, localVariable) -> localVariable.get(index)));

        // aload_3
        Set.put(0x2d, Helper.pushToStackFromArray(bytecode -> 3, (index, localVariable) -> localVariable.get(index)));

        // iaload
        Set.put(0x2e, Helper.arrayLoad(fields -> (int[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // laload
        Set.put(0x2f, Helper.arrayLoad(fields -> (long[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // faload
        Set.put(0x30, Helper.arrayLoad(fields -> (float[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // daload
        Set.put(0x31, Helper.arrayLoad(fields -> (double[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // aaload
        Set.put(0x32, Helper.arrayLoad(fields -> (Object[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // baload
        Set.put(0x33, Helper.arrayLoad(fields -> (int[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // caload
        Set.put(0x34, Helper.arrayLoad(fields -> (int[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // saload
        Set.put(0x35, Helper.arrayLoad(fields -> (int[])fields, (fields, index) -> fields.length > index,
                (fields, index) -> fields[index]));

        // istore
        Set.put(0x36, Helper.setToArrayFromStack(bytecode -> bytecode.getU1(), operandStack -> operandStack.popInt()));

        // lstore
        Set.put(0x37, Helper.setToArrayFromStack(bytecode -> bytecode.getU1(), operandStack -> operandStack.popLong()));

        // fstore
        Set.put(0x38,
                Helper.setToArrayFromStack(bytecode -> bytecode.getU1(), operandStack -> operandStack.popFloat()));

        // dstore
        Set.put(0x39,
                Helper.setToArrayFromStack(bytecode -> bytecode.getU1(), operandStack -> operandStack.popDouble()));

        // astore
        Set.put(0x3a, Helper.setToArrayFromStack(bytecode -> bytecode.getU1(), operandStack -> operandStack.pop()));

        // istore_0
        Set.put(0x3b, Helper.setToArrayFromStack(bytecode -> 0, operandStack -> operandStack.popInt()));

        // istore_1
        Set.put(0x3c, Helper.setToArrayFromStack(bytecode -> 1, operandStack -> operandStack.popInt()));

        // istore_2
        Set.put(0x3d, Helper.setToArrayFromStack(bytecode -> 2, operandStack -> operandStack.popInt()));

        // istore_3
        Set.put(0x3e, Helper.setToArrayFromStack(bytecode -> 3, operandStack -> operandStack.popInt()));

        // lstore_0
        Set.put(0x3f, Helper.setToArrayFromStack(bytecode -> 0, operandStack -> operandStack.popLong()));

        // lstore_1
        Set.put(0x40, Helper.setToArrayFromStack(bytecode -> 1, operandStack -> operandStack.popLong()));

        // lstore_2
        Set.put(0x41, Helper.setToArrayFromStack(bytecode -> 2, operandStack -> operandStack.popLong()));

        // lstore_3
        Set.put(0x42, Helper.setToArrayFromStack(bytecode -> 3, operandStack -> operandStack.popLong()));

        // fstore_0
        Set.put(0x43, Helper.setToArrayFromStack(bytecode -> 0, operandStack -> operandStack.popFloat()));

        // fstore_1
        Set.put(0x44, Helper.setToArrayFromStack(bytecode -> 1, operandStack -> operandStack.popFloat()));

        // fstore_2
        Set.put(0x45, Helper.setToArrayFromStack(bytecode -> 2, operandStack -> operandStack.popFloat()));

        // fstore_3
        Set.put(0x46, Helper.setToArrayFromStack(bytecode -> 3, operandStack -> operandStack.popFloat()));

        // dstore_0
        Set.put(0x47, Helper.setToArrayFromStack(bytecode -> 0, operandStack -> operandStack.popDouble()));

        // dstore_1
        Set.put(0x48, Helper.setToArrayFromStack(bytecode -> 1, operandStack -> operandStack.popDouble()));

        // dstore_2
        Set.put(0x49, Helper.setToArrayFromStack(bytecode -> 2, operandStack -> operandStack.popDouble()));

        // dstore_3
        Set.put(0x4a, Helper.setToArrayFromStack(bytecode -> 3, operandStack -> operandStack.popDouble()));

        // astore_0
        Set.put(0x4b, Helper.setToArrayFromStack(bytecode -> 0, operandStack -> operandStack.pop()));

        // astore_1
        Set.put(0x4c, Helper.setToArrayFromStack(bytecode -> 1, operandStack -> operandStack.pop()));

        // astore_2
        Set.put(0x4d, Helper.setToArrayFromStack(bytecode -> 2, operandStack -> operandStack.pop()));

        // astore_3
        Set.put(0x4e, Helper.setToArrayFromStack(bytecode -> 3, operandStack -> operandStack.pop()));

        // iastore
        Set.put(0x4f, Helper.arrayStore(operandStack -> operandStack.popInt(), fields -> (int[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // lastore
        Set.put(0x50, Helper.arrayStore(operandStack -> operandStack.popLong(), fields -> (long[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // fastore
        Set.put(0x51, Helper.arrayStore(operandStack -> operandStack.popFloat(), fields -> (float[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // dastore
        Set.put(0x52, Helper.arrayStore(operandStack -> operandStack.popDouble(), fields -> (double[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // aastore
        Set.put(0x53, Helper.arrayStore(operandStack -> operandStack.pop(), fields -> (Object[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // bastore
        Set.put(0x54, Helper.arrayStore(operandStack -> operandStack.popInt(), fields -> (int[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // castore
        Set.put(0x55, Helper.arrayStore(operandStack -> operandStack.popInt(), fields -> (int[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // sastore
        Set.put(0x56, Helper.arrayStore(operandStack -> operandStack.popInt(), fields -> (int[])fields,
                (fields, index) -> fields.length > index, (fields, map) -> fields[map.getKey()] = map.getValue()));

        // pop
        Set.put(0x57, (frame, bytecode) -> {
            frame.getOperandStack().pop();
        });

        // pop2
        Set.put(0x58, (frame, bytecode) -> {
            frame.getOperandStack().pop();
            frame.getOperandStack().pop();
        });

        // dup
        Set.put(0x59, (frame, bytecode) -> {
            frame.getOperandStack().push(frame.getOperandStack().current());
        });

        // dup_x1
        Set.put(0x5a, (frame, bytecode) -> {
            final Frame.OperandStack operandStack = frame.getOperandStack();
            final Object a = operandStack.pop();
            final Object b = operandStack.pop();
            operandStack.push(a);
            operandStack.push(b);
            operandStack.push(a);
        });

        // dup_x2
        Set.put(0x5b, (frame, bytecode) -> {
            final Frame.OperandStack operandStack = frame.getOperandStack();
            final Object a = operandStack.pop();
            final Object b = operandStack.pop();
            final Object c = operandStack.pop();
            operandStack.push(a);
            operandStack.push(c);
            operandStack.push(b);
            operandStack.push(a);
        });

        // dup2
        Set.put(0x5c, (frame, bytecode) -> {
            final Frame.OperandStack operandStack = frame.getOperandStack();
            final Object a = operandStack.pop();
            final Object b = operandStack.current();
            operandStack.push(a);
            operandStack.push(b);
            operandStack.push(a);
        });

        // dup2_x1
        Set.put(0x5d, (frame, bytecode) -> {
            final Frame.OperandStack operandStack = frame.getOperandStack();
            final Object a = operandStack.pop();
            final Object b = operandStack.pop();
            final Object c = operandStack.pop();
            operandStack.push(b);
            operandStack.push(a);
            operandStack.push(c);
            operandStack.push(b);
            operandStack.push(a);
        });

        // dup_x2
        Set.put(0x5e, (frame, bytecode) -> {
            final Frame.OperandStack operandStack = frame.getOperandStack();
            final Object a = operandStack.pop();
            final Object b = operandStack.pop();
            final Object c = operandStack.pop();
            final Object d = operandStack.pop();
            operandStack.push(b);
            operandStack.push(a);
            operandStack.push(d);
            operandStack.push(c);
            operandStack.push(b);
            operandStack.push(a);
        });

        // swap
        Set.put(0x5f, (frame, bytecode) -> {
            final Frame.OperandStack operandStack = frame.getOperandStack();
            final Object a = operandStack.pop();
            final Object b = operandStack.pop();
            operandStack.push(a);
            operandStack.push(b);
        });

        // iadd
        Set.put(0x60, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a + b));

        // ladd
        Set.put(0x61, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a + b));

        // fadd
        Set.put(0x62, Helper.arithmeticOperate(operandStack -> operandStack.popFloat(), (a, b) -> a + b));

        // dadd
        Set.put(0x63, Helper.arithmeticOperate(operandStack -> operandStack.popDouble(), (a, b) -> a + b));

        // isub
        Set.put(0x64, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a - b));

        // lsub
        Set.put(0x65, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a - b));

        // fsub
        Set.put(0x66, Helper.arithmeticOperate(operandStack -> operandStack.popFloat(), (a, b) -> a - b));

        // dsub
        Set.put(0x67, Helper.arithmeticOperate(operandStack -> operandStack.popDouble(), (a, b) -> a - b));

        // imul
        Set.put(0x68, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a * b));

        // lmul
        Set.put(0x69, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a * b));

        // fmul
        Set.put(0x6a, Helper.arithmeticOperate(operandStack -> operandStack.popFloat(), (a, b) -> a * b));

        // dmul
        Set.put(0x6b, Helper.arithmeticOperate(operandStack -> operandStack.popDouble(), (a, b) -> a * b));

        // idiv
        Set.put(0x6c, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a / b, a -> a == 0));

        // ldiv
        Set.put(0x6d, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a / b, a -> a == 0));

        // fdiv
        Set.put(0x6e, Helper.arithmeticOperate(operandStack -> operandStack.popFloat(), (a, b) -> a / b, a -> a == 0));

        // ddiv
        Set.put(0x6f, Helper.arithmeticOperate(operandStack -> operandStack.popDouble(), (a, b) -> a / b, a -> a == 0));

        // irem
        Set.put(0x70, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a % b, a -> a == 0));

        // lrem
        Set.put(0x71, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a % b, a -> a == 0));

        // frem
        Set.put(0x72, Helper.arithmeticOperate(operandStack -> operandStack.popFloat(), (a, b) -> a % b, a -> a == 0));

        // drem
        Set.put(0x73, Helper.arithmeticOperate(operandStack -> operandStack.popDouble(), (a, b) -> a % b, a -> a == 0));

        // ineg
        Set.put(0x74, Helper.unaryOperate(operandStack -> operandStack.popInt(), a -> -a));

        // lneg
        Set.put(0x75, Helper.unaryOperate(operandStack -> operandStack.popLong(), a -> -a));

        // fneg
        Set.put(0x76, Helper.unaryOperate(operandStack -> operandStack.popFloat(), a -> -a));

        // dneg
        Set.put(0x77, Helper.unaryOperate(operandStack -> operandStack.popDouble(), a -> -a));

        // ishl
        Set.put(0x78, Helper.arithmeticOperate(operandStack -> operandStack.popInt() & 0x1f,
                operandStack -> operandStack.popInt(), (a, b) -> a << b));

        // lshl
        Set.put(0x79, Helper.arithmeticOperate(operandStack -> operandStack.popInt() & 0x3f,
                operandStack -> operandStack.popLong(), (a, b) -> a << b));

        // ishr
        Set.put(0x7a, Helper.arithmeticOperate(operandStack -> operandStack.popInt() & 0x1f,
                operandStack -> operandStack.popInt(), (a, b) -> a >> b));

        // lshr
        Set.put(0x7b, Helper.arithmeticOperate(operandStack -> operandStack.popInt() & 0x3f,
                operandStack -> operandStack.popLong(), (a, b) -> a >> b));

        // iushr
        Set.put(0x7c, Helper.arithmeticOperate(operandStack -> operandStack.popInt() & 0x1f,
                operandStack -> operandStack.popInt(), (a, b) -> a >>> b));

        // lushr
        Set.put(0x7d, Helper.arithmeticOperate(operandStack -> operandStack.popInt() & 0x3f,
                operandStack -> operandStack.popLong(), (a, b) -> a >>> b));

        // iand
        Set.put(0x7e, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a & b));

        // land
        Set.put(0x7f, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a & b));

        // ior
        Set.put(0x80, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a | b));

        // lor
        Set.put(0x81, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a | b));

        // ixor
        Set.put(0x82, Helper.arithmeticOperate(operandStack -> operandStack.popInt(), (a, b) -> a ^ b));

        // lxor
        Set.put(0x83, Helper.arithmeticOperate(operandStack -> operandStack.popLong(), (a, b) -> a ^ b));

        // iinc
        Set.put(0x84, (frame, bytecode) -> {
            final int index = bytecode.getU1();
            final int value = bytecode.get();
            final Frame.LocalVariable localVariable = frame.getLocalVariable();
            localVariable.set(index, localVariable.getInt(index) + value);
        });

        // i2l
        Set.put(0x85, Helper.unaryOperate(operandStack -> operandStack.popInt(), a -> (long)a.intValue()));

        // i2f
        Set.put(0x86, Helper.unaryOperate(operandStack -> operandStack.popInt(), a -> (float)a.intValue()));

        // i2d
        Set.put(0x87, Helper.unaryOperate(operandStack -> operandStack.popInt(), a -> (double)a.intValue()));

        // l2i
        Set.put(0x88, Helper.unaryOperate(operandStack -> operandStack.popLong(), a -> (int)a.longValue()));

        // l2f
        Set.put(0x89, Helper.unaryOperate(operandStack -> operandStack.popLong(), a -> (float)a.longValue()));

        // l2d
        Set.put(0x8a, Helper.unaryOperate(operandStack -> operandStack.popLong(), a -> (double)a.longValue()));

        // f2i
        Set.put(0x8b, Helper.unaryOperate(operandStack -> operandStack.popFloat(), a -> (int)a.floatValue()));

        // f2l
        Set.put(0x8c, Helper.unaryOperate(operandStack -> operandStack.popFloat(), a -> (long)a.floatValue()));

        // f2d
        Set.put(0x8d, Helper.unaryOperate(operandStack -> operandStack.popFloat(), a -> (double)a.floatValue()));

        // d2i
        Set.put(0x8e, Helper.unaryOperate(operandStack -> operandStack.popDouble(), a -> (int)a.doubleValue()));

        // d2l
        Set.put(0x8f, Helper.unaryOperate(operandStack -> operandStack.popDouble(), a -> (long)a.doubleValue()));

        // d2f
        Set.put(0x90, Helper.unaryOperate(operandStack -> operandStack.popDouble(), a -> (float)a.doubleValue()));

        // i2b
        Set.put(0x91, (frame, bytecode) -> {
        });

        // i2c
        Set.put(0x92, (frame, bytecode) -> {
        });

        // i2s
        Set.put(0x93, (frame, bytecode) -> {
        });

        // lcmp
        Set.put(0x94, (frame, bytecode) -> {
            final Frame.OperandStack operandStack = frame.getOperandStack();
            final long a = operandStack.popLong();
            final long b = operandStack.popLong();
            operandStack.push(Long.compare(b, a));
        });

        // fcmpl
        Set.put(0x95, Helper.compareFromStack(-1, operandStack -> operandStack.popFloat(), a -> a.isNaN()));

        // fcmpg
        Set.put(0x96, Helper.compareFromStack(1, operandStack -> operandStack.popFloat(), a -> a.isNaN()));

        // dcmpl
        Set.put(0x97, Helper.compareFromStack(-1, operandStack -> operandStack.popDouble(), a -> a.isNaN()));

        // dcmpg
        Set.put(0x98, Helper.compareFromStack(1, operandStack -> operandStack.popDouble(), a -> a.isNaN()));

        // ifeq
        Set.put(0x99, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), a -> a == 0));

        // ifne
        Set.put(0x9a, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), a -> a != 0));

        // iflt
        Set.put(0x9b, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), a -> a < 0));

        // ifge
        Set.put(0x9c, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), a -> a >= 0));

        // ifgt
        Set.put(0x9d, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), a -> a > 0));

        // ifle
        Set.put(0x9e, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), a -> a <= 0));

        // if_icmpeq
        Set.put(0x9f, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), (a, b) -> a == b));

        // if_icmpne
        Set.put(0xa0, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), (a, b) -> a != b));

        // if_icmplt
        Set.put(0xa1, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), (a, b) -> a < b));

        // if_icmpge
        Set.put(0xa2, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), (a, b) -> a >= b));

        // if_icmpgt
        Set.put(0xa3, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), (a, b) -> a > b));

        // if_icmple
        Set.put(0xa4, Helper.jumpBranchByComparation(operandStack -> operandStack.popInt(), (a, b) -> a <= b));

        // if_acmpeq
        Set.put(0xa5, Helper.jumpBranchByComparation(operandStack -> operandStack.pop(), (a, b) -> a.equals(b)));

        // if_acmpne
        Set.put(0xa6, Helper.jumpBranchByComparation(operandStack -> operandStack.pop(), (a, b) -> !a.equals(b)));

        // goto
        Set.put(0xa7, (frame, bytecode) -> {
            bytecode.jump();
        });

        // @todo jsr
        Set.put(0xa8, Helper.Unsupport());

        // @todo ret
        Set.put(0xa9, Helper.Unsupport());

        // tableswitch
        Set.put(0xaa, (frame, bytecode) -> {
            int pc = bytecode.getPc();
            Helper.skipPadding(bytecode);
            final int index = frame.getOperandStack().popInt();
            int offset = bytecode.getInt();
            final int low = bytecode.getInt();
            final int high = bytecode.getInt();
            final int n = high - low + 1;
            final int array[] = new int[n];
            for (int i = 0; i < n; ++i) {
                array[i] = bytecode.getInt();
            }
            if (index >= low && index <= high) {
                offset = array[index - low];
            }
            bytecode.setPc(pc + offset - 1);
        });

        // lookupswitch
        Set.put(0xab, (frame, bytecode) -> {
            int pc = bytecode.getPc();
            Helper.skipPadding(bytecode);
            final int key = frame.getOperandStack().popInt();
            int offset = bytecode.getInt();
            final int n = bytecode.getInt();
            final int map[] = new int[2 * n];
            for (int i = 0; i < 2 * n; ++i) {
                map[i] = bytecode.getInt();
            }
            for (int i = 0; i < 2 * n; i += 2) {
                if (map[i] == key) {
                    offset = map[i + 1];
                    break;
                }
            }
            bytecode.setPc(pc + offset - 1);
        });

        // ireturn
        Set.put(0xac, Helper.returnOperate(operandStack -> operandStack.popInt()));

        // lreturn
        Set.put(0xad, Helper.returnOperate(operandStack -> operandStack.popLong()));

        // freturn
        Set.put(0xae, Helper.returnOperate(operandStack -> operandStack.popFloat()));

        // dreturn
        Set.put(0xaf, Helper.returnOperate(operandStack -> operandStack.popDouble()));

        // areturn
        Set.put(0xb0, Helper.returnOperate(operandStack -> operandStack.pop()));

        // return
        Set.put(0xb1, (frame, bytecode) -> {
            frame.getThreadResource().getJavaStack().pop();
        });

        // getstatic
        Set.put(0xb2, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            MethodArea.Field field = runtimeConstantPool.dereferenceField(index);

            Frame.OperandStack operandStack = frame.getOperandStack();

            switch (field.getDescriptor().charAt(0)) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                    operandStack.push(field.getIntValue());
                    break;
                case 'F':
                    operandStack.push(field.getFloatValue());
                    break;
                case 'J':
                    operandStack.push(field.getLongValue());
                    break;
                case 'D':
                    operandStack.push(field.getDoubleValue());
                    break;
                case 'L':
                case '[':
                    operandStack.push(field.getValue());
                    break;
            }
        });

        // putstatic
        Set.put(0xb3, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            MethodArea.Field field = runtimeConstantPool.dereferenceField(index);

            if (field.getClassfileField().isFinal()) {
                // @todo
            }

            Frame.OperandStack operandStack = frame.getOperandStack();

            switch (field.getDescriptor().charAt(0)) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                    field.setValue(operandStack.popInt());
                    break;
                case 'F':
                    field.setValue(operandStack.popFloat());
                    break;
                case 'J':
                    field.setValue(operandStack.popLong());
                    break;
                case 'D':
                    field.setValue(operandStack.popDouble());
                    break;
                case 'L':
                case '[':
                    field.setValue(operandStack.pop());
                    break;
            }
        });

        // getfield
        Set.put(0xb4, (frame, bytecode) -> {
            // ref
            final int refIndex = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            int[] ref = runtimeConstantPool.dereferenceReference(refIndex);

            // name and type
            String[] nameAndType = runtimeConstantPool.dereferenceNameAndType(ref[1]);

            Frame.OperandStack operandStack = frame.getOperandStack();

            Heap.Instance instance = (Heap.Instance)operandStack.pop();

            if (instance == null) {
                throw new RuntimeException(
                        "Can not get field[" + nameAndType[0] + "," + nameAndType[1] + "] from a null instance.");
            }
            MethodArea.Field field = instance.findField(nameAndType[0], nameAndType[1]);

            switch (nameAndType[1].charAt(0)) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                    operandStack.push(field.getIntValue());
                    break;
                case 'F':
                    operandStack.push(field.getFloatValue());
                    break;
                case 'J':
                    operandStack.push(field.getLongValue());
                    break;
                case 'D':
                    operandStack.push(field.getDoubleValue());
                    break;
                case 'L':
                case '[':
                    operandStack.push(field.getValue());
                    break;
            }
        });

        // putfield
        Set.put(0xb5, (frame, bytecode) -> {
            // ref
            final int refIndex = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            int[] ref = runtimeConstantPool.dereferenceReference(refIndex);

            // name and type
            String[] nameAndType = runtimeConstantPool.dereferenceNameAndType(ref[1]);

            Frame.OperandStack operandStack = frame.getOperandStack();

            Object value = null;
            switch (nameAndType[1].charAt(0)) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                    value = operandStack.popInt();
                    break;
                case 'F':
                    value = operandStack.popFloat();
                    break;
                case 'J':
                    value = operandStack.popLong();
                    break;
                case 'D':
                    value = operandStack.popDouble();
                    break;
                case 'L':
                case '[':
                    value = operandStack.pop();
                    break;
            }

            Heap.Instance instance = (Heap.Instance)operandStack.pop();
            if (instance == null) {
                throw new RuntimeException(
                        "Can not get field[" + nameAndType[0] + "," + nameAndType[1] + "] from a null instance.");
            }
            MethodArea.Field field = instance.findField(nameAndType[0], nameAndType[1]);

            if (field.getClassfileField().isFinal()) {
                // @todo
            }

            field.setValue(value);
        });

        // invokevirtual
        Set.put(0xb6, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            MethodArea.Method method = runtimeConstantPool.dereferenceMethod(index);

            Frame.OperandStack operandStack = frame.getOperandStack();

            if (method.getClassfileMethod().isStatic()) {
                throw new RuntimeException("Instruction invokevirtual can not invoke a static method.");
            }

            String[] argumentTypes = method.getArgumentTypes();
            int localVariableIndex = Helper.countArgumentTypesSpace(argumentTypes,
                    method.getClassfileMethod().isStatic()) - 1;

            Object object = operandStack.bottom(localVariableIndex);
            if (object == null) {
                // @todo hook for System.out.println & System.out.print
                if (Helper.printHook(method, argumentTypes, operandStack)) {
                    return;
                }
                throw new RuntimeException("Method " + method.getName() + " can not be called by a null instance.");
            } else if (object instanceof Heap.Instance) {
                Heap.Instance instance = (Heap.Instance)object;
                method = instance.getClazz().findMethod(method.getName(), method.getDescriptor());
            } else if (object instanceof Heap.ArrayInstance) {
                // just like: new String[0].getClass()
                method = ((Heap.ArrayInstance)object).getArrayClazz().findMethod(method.getName(),
                        method.getDescriptor());
            } else {
                throw new RuntimeException("Call a method from a unknown instance.");
            }

            if (method.getClassfileMethod().isAbstract()) {
                throw new RuntimeException("Instruction invokevirtual call a abstract method.");
            }

            Helper.createNewFrame(method, frame, operandStack, argumentTypes, localVariableIndex);
        });

        // invokespecial
        Set.put(0xb7, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            MethodArea.Method method = runtimeConstantPool.dereferenceMethod(index);

            Frame.OperandStack operandStack = frame.getOperandStack();

            if (method.getClassfileMethod().isStatic()) {
                throw new RuntimeException("Instruction invokespecial can not invoke a static method.");
            }

            String[] argumentTypes = method.getArgumentTypes();
            int localVariableIndex = Helper.countArgumentTypesSpace(argumentTypes,
                    method.getClassfileMethod().isStatic()) - 1;

            Heap.Instance instance = (Heap.Instance)operandStack.bottom(localVariableIndex);
            if (instance == null) {
                throw new RuntimeException("Method " + method.getName() + " can not be called by a null instance.");
            }

            MethodArea.Clazz currentClazz = runtimeConstantPool.getClazz();
            if (!method.getName().equals("<init>") && currentClazz.getClassfileInformation().isSuper()
                    && method.getClazz().isParentOf(currentClazz)) {
                method = ((MethodArea.Clazz)currentClazz.getParent()).findMethod(method.getName(),
                        method.getDescriptor());
            }

            if (method.getClassfileMethod().isAbstract()) {
                throw new RuntimeException("Instruction invokespecial call a abstract method.");
            }

            Helper.createNewFrame(method, frame, operandStack, argumentTypes, localVariableIndex);
        });

        // invokestatic
        Set.put(0xb8, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            MethodArea.Method method = runtimeConstantPool.dereferenceMethod(index);

            Frame.OperandStack operandStack = frame.getOperandStack();

            String[] argumentTypes = method.getArgumentTypes();
            int localVariableIndex = Helper.countArgumentTypesSpace(argumentTypes,
                    method.getClassfileMethod().isStatic()) - 1;

            Helper.createNewFrame(method, frame, operandStack, argumentTypes, localVariableIndex);
        });

        // invokeinterface
        Set.put(0xb9, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            MethodArea.Method interfaceMethod = runtimeConstantPool.dereferenceInterfaceMethod(index);

            Frame.OperandStack operandStack = frame.getOperandStack();

            if (interfaceMethod.getClassfileMethod().isStatic()) {
                throw new RuntimeException("Instruction invokevinterface can not invoke a static method.");
            }

            String[] argumentTypes = interfaceMethod.getArgumentTypes();
            int localVariableIndex = bytecode.getU1() - 1;
            bytecode.getU1();

            Heap.Instance instance = (Heap.Instance)operandStack.bottom(localVariableIndex);
            if (instance == null) {
                throw new RuntimeException(
                        "Method " + interfaceMethod.getName() + " can not be called by a null instance.");
            }

            if (!instance.getClazz().isImplementOf(interfaceMethod.getClazz())) {
                throw new RuntimeException("Class " + instance.getClazz().getClassName()
                        + " is not a implementation of interface " + interfaceMethod.getClazz().getClassName() + ".");
            }

            MethodArea.Method method = instance.getClazz().findMethod(interfaceMethod.getName(),
                    interfaceMethod.getDescriptor());

            if (method.getClassfileMethod().isAbstract()) {
                throw new RuntimeException("Instruction invokeinterface call a abstract method.");
            }

            Helper.createNewFrame(method, frame, operandStack, argumentTypes, localVariableIndex);
        });

        // @todo invokedynamic
        Set.put(0xba, Helper.Unsupport());

        // new
        Set.put(0xbb, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            MethodArea.Clazz clazz = runtimeConstantPool.dereferenceClazz(index);

            frame.getOperandStack().push(clazz.makeInstance());
        });

        // newarray
        Set.put(0xbc, (frame, bytecode) -> {
            final int type = bytecode.getU1();
            Frame.OperandStack operandStack = frame.getOperandStack();

            int size = operandStack.popInt();
            if (size < 0) {
                throw new RuntimeException("Array size can not be a negative number.");
            }

            MethodArea.ArrayClazz arrayClazz = MethodArea.findArrayClazz(Helper.convertArrayTypeToArrayClazzName(type));
            operandStack.push(arrayClazz.makeInstance(size));
        });

        // anewarray
        Set.put(0xbd, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            MethodArea.Clazz clazz = frame.getRuntimeConstantPool().dereferenceClazz(index);

            Frame.OperandStack operandStack = frame.getOperandStack();

            int size = operandStack.popInt();
            if (size < 0) {
                throw new RuntimeException("Array size can not be a negative number.");
            }

            MethodArea.ArrayClazz arrayClazz = MethodArea
                    .findArrayClazz(Helper.convertClassNameToArrayClazzName(clazz.getClassName()));
            operandStack.push(arrayClazz.makeInstance(size));
        });

        // arraylength
        Set.put(0xbe, (frame, bytecode) -> {
            Frame.OperandStack operandStack = frame.getOperandStack();

            Heap.ArrayInstance arrayInstance = (Heap.ArrayInstance)operandStack.pop();

            operandStack.push(arrayInstance.getSize());
        });

        // athrow
        Set.put(0xbf, (frame, bytecode) -> {
            Heap.Instance instance = (Heap.Instance)frame.getOperandStack().pop();
            if (instance == null) {
                throw new RuntimeException("Can not throw a null exception!");
            }

            RuntimeDataArea.JavaStack javaStack = frame.getThreadResource().getJavaStack();

            do {
                Frame currentFrame = javaStack.current();
                Frame.OperandStack operandStack = currentFrame.getOperandStack();

                int pc = currentFrame.getBytecode().getPc();
                MethodArea.Exception exception = currentFrame.getMethod().findException(instance.getClazz(), pc);

                if (exception != null && exception.getHandlePc() > 0) {
                    operandStack.clear();
                    operandStack.push(instance);
                    currentFrame.getBytecode().setPc(exception.getHandlePc());
                    return;
                }

                javaStack.pop();
            } while (!javaStack.isEmpty());

            // UncaughtException
            MethodArea.Field messageField = instance.findField("detailMessage", "Ljava/lang/String;");
            String message = String.format("Call %s.%s\n occur\t %s:%s", frame.getMethod().getClazz().getClassName(),
                    frame.getMethod().getName(), instance.getClazz().getClassName(), messageField.getValue());
            throw new RuntimeException(message);
        });

        // checkcast
        Set.put(0xc0, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            Frame.OperandStack operandStack = frame.getOperandStack();

            Object instance = operandStack.pop();
            operandStack.push(instance);

            if (instance == null) {
                return;
            }

            if (instance instanceof Heap.Instance) {
                MethodArea.Clazz clazz = frame.getRuntimeConstantPool().dereferenceClazz(index);

                if (!((Heap.Instance)instance).isInstanceOf(clazz)) {
                    throw new RuntimeException("Can not cast to instance of " + clazz.getClassName() + ".");
                }
            } else {
                String name = frame.getRuntimeConstantPool().dereferenceString(index);
                if (!((Heap.ArrayInstance)instance).isInstanceOf(name)) {
                    throw new RuntimeException("Can not cast to instance of " + name + ".");
                }
            }

        });

        // instanceof
        Set.put(0xc1, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            Frame.OperandStack operandStack = frame.getOperandStack();

            Object instance = operandStack.pop();

            if (instance == null) {
                operandStack.push(1);
            } else if (instance instanceof Heap.Instance) {
                MethodArea.Clazz clazz = frame.getRuntimeConstantPool().dereferenceClazz(index);
                operandStack.push(((Heap.Instance)instance).isInstanceOf(clazz) ? 1 : 0);
            } else {
                String name = frame.getRuntimeConstantPool().dereferenceString(index);
                operandStack.push(((Heap.ArrayInstance)instance).isInstanceOf(name) ? 1 : 0);
            }
        });

        // @todo monitorenter
        Set.put(0xc2, Helper.Unsupport());

        // @todo monitorexit
        Set.put(0xc3, Helper.Unsupport());

        // wide
        Set.put(0xc4, (frame, bytecode) -> {
            final int opcode = bytecode.getU1();
            switch (opcode) {
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                    Helper.pushToStackFromArray(bytecode1 -> bytecode1.getChar(), Helper.MapFunctionOfLoad.get(opcode))
                            .accept(frame, bytecode);
                    break;
                case 0x36:
                case 0x37:
                case 0x38:
                case 0x39:
                case 0x3a:
                    Helper.setToArrayFromStack(bytecode1 -> bytecode1.getChar(), Helper.MapFunctionOfStore.get(opcode))
                            .accept(frame, bytecode);
                    break;
                case 0x84:
                    final int index = bytecode.getChar();
                    final int value = bytecode.getChar();
                    final Frame.LocalVariable localVariable = frame.getLocalVariable();
                    localVariable.set(index, localVariable.getInt(index) + value);
                    break;
                case 0xa9:
                    Helper.Unsupport().accept(frame, bytecode);
                    break;
            }
        });

        // multianewarray
        Set.put(0xc5, (frame, bytecode) -> {
            final int index = bytecode.getChar();
            final int dimension = bytecode.getU1();
            MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
            final int arrayClazzNameIndex = (int)runtimeConstantPool.dereference(index).getValue();
            final String arrayClazzName = (String)runtimeConstantPool.dereference(arrayClazzNameIndex).getValue();

            Frame.OperandStack operandStack = frame.getOperandStack();

            int[] dimensionSize = new int[dimension];
            for (int i = dimension - 1; i >= 0; --i) {
                dimensionSize[i] = operandStack.popInt();
                if (dimensionSize[i] < 0) {
                    throw new RuntimeException("Array size can not be a negative number.");
                }
            }
            MethodArea.ArrayClazz arrayClazz = MethodArea.findArrayClazz(arrayClazzName);
            Heap.ArrayInstance arrayInstance = arrayClazz.makeInstance(dimensionSize[0]);

            Helper.createMultiArray(arrayClazz, arrayInstance, dimensionSize, 1);

            operandStack.push(arrayInstance);
        });

        // ifnull
        Set.put(0xc6, Helper.jumpBranchByComparation(operandStack -> operandStack.pop(), a -> a == null));

        // ifnonnull
        Set.put(0xc7, Helper.jumpBranchByComparation(operandStack -> operandStack.pop(), a -> a != null));

        // goto_w
        Set.put(0xc8, (frame, bytecode) -> {
            bytecode.jump(bytecode::peekInt);
        });

        // @todo jsr_w
        Set.put(0xc9, Helper.Unsupport());

        // @todo breakpoint
        Set.put(0xca, Helper.Unsupport());

        // @todo impdep1
        Set.put(0xfe, Helper.Unsupport());

        // @todo impdep2
        Set.put(0xff, Helper.Unsupport());
    }

    /**
     * Class Instruction.Helper include a lot of static method which can generate a lambda function which a instruction
     * need, so that implementing a instruction is a very esay way.
     */
    final private static class Helper {
        /**
         * A map of the lambda function which the load instructions need
         */
        private static Map<Integer, BiFunction<Integer, Frame.LocalVariable, ?>> MapFunctionOfLoad = new HashMap<>();

        /**
         * A map of the lambda function which the store instructions need
         */
        private static Map<Integer, Function<Frame.OperandStack, ?>> MapFunctionOfStore = new HashMap<>();

        /**
         * Generate a lambda function that push a value to the OperandStack. The value type is generic.
         * <p>
         * These instructions below use it: {@code aconst_null [i|l|f|d]const_xx}
         */
        private static <T> BiConsumer<Frame, Bytecode> pushValueToStack(final T value) {
            return (frame, bytecode) -> {
                frame.getOperandStack().push(value);
            };
        }

        /**
         * Generate a lambda function that get a generic value from the LocalVariable and push it to the OperandStack.
         * <p>
         * These instructions below use it: {@code [i|l|f|d|a]load [i|l|f|d|a]load_xx}
         */
        private static <T> BiConsumer<Frame, Bytecode> pushToStackFromArray(final Function<Bytecode, Integer> f1,
                final BiFunction<Integer, Frame.LocalVariable, T> f2) {
            return (frame, bytecode) -> {
                final int index = f1.apply(bytecode);
                frame.getOperandStack().push(f2.apply(index, frame.getLocalVariable()));
            };
        }

        /**
         * Generate a lambda function that pop a generic value from the OperandStack and set it to the LocalVariable.
         * <p>
         * These instructions below use it: {@code [i|l|f|d|a]store [i|l|f|d|a]store_xx}
         */
        private static <T> BiConsumer<Frame, Bytecode> setToArrayFromStack(final Function<Bytecode, Integer> f1,
                final Function<Frame.OperandStack, T> f2) {
            return (frame, bytecode) -> {
                final int index = f1.apply(bytecode);
                frame.getLocalVariable().set(index, f2.apply(frame.getOperandStack()));
            };
        }

        /**
         * Generate a lambda function that compare two values from the OperandStack and push the result to the
         * OperandStack.
         * <p>
         * These instructions below use it: {@code fcmpl fcmpg dcmpl dcmpg}
         */
        private static <T extends Number & Comparable<? super T>> BiConsumer<Frame, Bytecode> compareFromStack(
                final int defaultResult, Function<Frame.OperandStack, T> f, Predicate<T> p) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                int result;
                T a = f.apply(operandStack);
                T b = f.apply(operandStack);
                if (p.test(a) || p.test(b)) {
                    result = defaultResult;
                } else {
                    result = b.compareTo(a);
                }
                operandStack.push(result);
            };
        }

        /**
         * Generate a lambda function that jump a offset by comparing two value from the OperandStack.
         * <p>
         * These instructions below use it: {@code if_icmpxx}
         */
        private static <T> BiConsumer<Frame, Bytecode> jumpBranchByComparation(Function<Frame.OperandStack, T> f,
                BiPredicate<T, T> p) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                T a = f.apply(operandStack);
                if (p.test(f.apply(operandStack), a)) {
                    bytecode.jump();
                } else {
                    bytecode.getChar();
                }
            };
        }

        /**
         * Generate a lambda function that jump a offset by comparing the value from OperandStack and the given value.
         * <p>
         * These instructions below use it: {@code ifxx}
         */
        private static <T> BiConsumer<Frame, Bytecode> jumpBranchByComparation(Function<Frame.OperandStack, T> f,
                Predicate<T> p) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                if (p.test(f.apply(operandStack))) {
                    bytecode.jump();
                } else {
                    bytecode.getChar();
                }
            };
        }

        /**
         * Generate a lambda function that map a value from the OperandStack to the other value.
         * <p>
         * These instructions below use it: {@code [i|l|f|d]neg [i|l|f|d]2[i|l|f|d]}
         */
        private static <T, R> BiConsumer<Frame, Bytecode> unaryOperate(Function<Frame.OperandStack, T> f1,
                Function<T, R> f2) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                T a = f1.apply(operandStack);
                operandStack.push(f2.apply(a));
            };
        }

        /**
         * Generate a lambda function that calc two value from the OperandStack, and a exception is throwed when the
         * given function p is not satisfied.
         * <p>
         * These instructions below use it: {@code [i|l|f|d]div [i|l|f|d]rem}
         */
        private static <T> BiConsumer<Frame, Bytecode> arithmeticOperate(Function<Frame.OperandStack, T> f1,
                BiFunction<T, T, T> f2, Predicate<T> p) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                T a = f1.apply(operandStack);
                if (p.test(a)) {
                    throw new RuntimeException("Divisor can not be zero!");
                }
                operandStack.push(f2.apply(f1.apply(operandStack), a));
            };
        }

        /**
         * Generate a lambda function that calc two value from the OperandStack.
         * <p>
         * These instructions below use it: {@code [i|l|f|d]add [i|l|f|d]sub [i|l|f|d]mul [i|l]and [i|l]or [i|l]xor}
         */
        private static <T> BiConsumer<Frame, Bytecode> arithmeticOperate(Function<Frame.OperandStack, T> f1,
                BiFunction<T, T, T> f2) {
            return arithmeticOperate(f1, f2, a -> false);
        }

        /**
         * Generate a lambda function that calc two value from the OperandStack, and these two value can be a different
         * way to deal.
         * <p>
         * These instructions below use it: {@code [i|l]shl [i|l]shr [i|l]ushr}
         */
        private static <T, R> BiConsumer<Frame, Bytecode> arithmeticOperate(Function<Frame.OperandStack, R> f1,
                Function<Frame.OperandStack, T> f2, BiFunction<T, R, T> f3) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                R a = f1.apply(operandStack);
                operandStack.push(f3.apply(f2.apply(operandStack), a));
            };
        }

        /**
         * Generate a lambda function that create a new frame to java stack and drop the current frame from java stack.
         * <p>
         * These instructions below use it: {@code [i|l|f|d|a]return}
         */
        private static <T> BiConsumer<Frame, Bytecode> returnOperate(Function<Frame.OperandStack, T> f1) {
            return (frame, bytecode) -> {
                frame.getOperandStack().current();
                JavaStack javaStack = frame.getThreadResource().getJavaStack();
                javaStack.pop();
                Frame nextFrame = javaStack.current();
                if (nextFrame != null) {
                    nextFrame.getOperandStack().push(f1.apply(frame.getOperandStack()));
                }
            };
        }

        /**
         * Generate a lambda function that execute ldc operation.
         * <p>
         * These instructions below use it: {@code ldc ldc_w ldc2_w}
         */
        private static BiConsumer<Frame, Bytecode> LdcOperate(Function<Bytecode, Integer> f1) {
            return (frame, bytecode) -> {
                final int index = f1.apply(bytecode);
                MethodArea.RuntimeConstantPool runtimeConstantPool = frame.getRuntimeConstantPool();
                Classfile.ConstantPool constantPool = runtimeConstantPool.dereference(index);
                Frame.OperandStack operandStack = frame.getOperandStack();
                Object value = constantPool.getValue();

                switch (constantPool.getTag()) {
                    case Classfile.CONSTANTPOOL_TABLE.INTEGER:
                        operandStack.push((int)value);
                        break;
                    case Classfile.CONSTANTPOOL_TABLE.FLOAT:
                        operandStack.push((float)value);
                        break;
                    case Classfile.CONSTANTPOOL_TABLE.LONG:
                        operandStack.push((long)value);
                        break;
                    case Classfile.CONSTANTPOOL_TABLE.DOUBLE:
                        operandStack.push((double)value);
                        break;
                    case Classfile.CONSTANTPOOL_TABLE.STRING:
                        String stringValue = runtimeConstantPool.dereference((int)value).getValue().toString();
                        operandStack.push(MethodArea.Clazz.makeInstanceFrom(stringValue));
                        break;
                    case Classfile.CONSTANTPOOL_TABLE.CLASS:
                        String name = runtimeConstantPool.dereference((int)value).getValue().toString();
                        if (name.startsWith("[")) {
                            operandStack.push(runtimeConstantPool.dereferenceArrayClazz(index).getClazzInstance());
                        } else {
                            operandStack.push(runtimeConstantPool.dereferenceClazz(index).getClazzInstance());
                        }
                        break;
                    default:
                        throw new RuntimeException("Todo: LDC Unknown type.");
                    // @todo
                }
            };
        }

        /**
         * Generate a lambda function that get a array from OperandStack and push its generic value to LocalVariable.
         * <p>
         * These instructions below use it: {@code [i|l|f|d|a|b|c|s]aload}
         */
        private static <T, R> BiConsumer<Frame, Bytecode> arrayLoad(Function<Object, T> f1, BiPredicate<T, Integer> p,
                BiFunction<T, Integer, R> f2) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                int index = operandStack.popInt();

                if (index < 0) {
                    throw new RuntimeException("Array index can not a negative number.");
                }

                Heap.ArrayInstance arrayInstance = (Heap.ArrayInstance)operandStack.pop();

                if (arrayInstance == null) {
                    throw new RuntimeException("Can not store a value to a null array.");
                }

                T fields = f1.apply(arrayInstance.getFields());

                if (!p.test(fields, index)) {
                    throw new RuntimeException("Index " + index + " is larger than the size of array.");
                }

                operandStack.push(f2.apply(fields, index));
            };
        }

        /**
         * Generate a lambda function that store a generic value from OperandStack to class `Field`.
         * <p>
         * These instructions below use it: {@code [i|l|f|d|a|b|c|s]astore}
         */
        private static <T, R> BiConsumer<Frame, Bytecode> arrayStore(Function<Frame.OperandStack, T> f1,
                Function<Object, R> f2, BiPredicate<R, Integer> p, BiConsumer<R, Map.Entry<Integer, T>> c) {
            return (frame, bytecode) -> {
                Frame.OperandStack operandStack = frame.getOperandStack();
                T value = f1.apply(operandStack);
                int index = operandStack.popInt();

                if (index < 0) {
                    throw new RuntimeException("Array index can not a negative number.");
                }

                Heap.ArrayInstance arrayInstance = (Heap.ArrayInstance)operandStack.pop();

                if (arrayInstance == null) {
                    throw new RuntimeException("Can not store a value to a null array.");
                }

                R fields = f2.apply(arrayInstance.getFields());

                if (!p.test(fields, index)) {
                    throw new RuntimeException("Index " + index + " is larger than the size of array.");
                }

                c.accept(fields, new AbstractMap.SimpleEntry<>(index, value));
            };
        }

        private static void skipPadding(final Bytecode bytecode) {
            while (bytecode.getPc() % 4 != 0) {
                bytecode.get();
            }
        }

        /**
         * Generate a lambda function that throw a exception.
         */
        private static BiConsumer<Frame, Bytecode> Unsupport() {
            return (frame, bytecode) -> {
                throw new RuntimeException("This instruction is unsupported now!");
            };
        }

        private static String convertArrayTypeToArrayClazzName(int type) {
            switch (type) {
                case Classfile.ARRAY_TYPE_TABLE.T_BOOLEAN:
                    return "[Z";
                case Classfile.ARRAY_TYPE_TABLE.T_BYTE:
                    return "[B";
                case Classfile.ARRAY_TYPE_TABLE.T_CHAR:
                    return "[C";
                case Classfile.ARRAY_TYPE_TABLE.T_SHORT:
                    return "[S";
                case Classfile.ARRAY_TYPE_TABLE.T_INT:
                    return "[I";
                case Classfile.ARRAY_TYPE_TABLE.T_LONG:
                    return "[J";
                case Classfile.ARRAY_TYPE_TABLE.T_FLOAT:
                    return "[F";
                case Classfile.ARRAY_TYPE_TABLE.T_DOUBLE:
                    return "[D";
            }
            throw new RuntimeException(type + " is not a valid array type.");
        }

        private static String convertClassNameToArrayClazzName(String className) {
            return "[L" + className + ";";
        }

        private static void createMultiArray(MethodArea.ArrayClazz arrayClazz, Heap.ArrayInstance arrayInstance,
                int[] dimensionSize, int index) {
            if (index < dimensionSize.length) {
                arrayClazz = MethodArea.findArrayClazz(arrayClazz.getFieldType());
                Object[] fieldInstances = (Object[])arrayInstance.getFields();
                for (int i = 0; i < fieldInstances.length; ++i) {
                    fieldInstances[i] = arrayClazz.makeInstance(dimensionSize[index]);
                    createMultiArray(arrayClazz, (Heap.ArrayInstance)fieldInstances[i], dimensionSize, index + 1);
                }
            }
        }

        private static int countArgumentTypesSpace(String[] argumentTypes, boolean isStatic) {
            int size = isStatic ? argumentTypes.length : argumentTypes.length + 1;
            for (String argumentType : argumentTypes) {
                if (argumentType.equals("J") || argumentType.equals("D")) {
                    ++size;
                }
            }
            return size;
        }

        private static void createNewFrame(MethodArea.Method method, Frame currentFrame,
                Frame.OperandStack operandStack, String[] argumentTypes, int localVariableIndex) {
            // System.out.println("class: " + method.getClazz().getClassName());
            // System.out.println("method: " + method.getName());
            // System.out.println("sign: " + method.getDescriptor());
            // System.out.println("is native: " + method.getClassfileMethod().isNative());

            // native method
            if (method.getClassfileMethod().isNative()) {
                NativeMethod.run(method.getClazz().getClassName(), method.getName(), operandStack);
                return;
            }

            // operandStack.current();

            // java method
            Frame nextFrame = new Frame(method, currentFrame.getThreadResource());
            Frame.LocalVariable nextLocalVariable = nextFrame.getLocalVariable();
            currentFrame.getThreadResource().getJavaStack().push(nextFrame);

            for (int i = argumentTypes.length - 1; i >= 0; --i) {
                switch (argumentTypes[i].charAt(0)) {
                    case 'Z':
                    case 'B':
                    case 'C':
                    case 'S':
                    case 'I':
                        nextLocalVariable.set(localVariableIndex, operandStack.popInt());
                        break;
                    case 'F':
                        nextLocalVariable.set(localVariableIndex, operandStack.popFloat());
                        break;
                    case 'J':
                        --localVariableIndex;
                        nextLocalVariable.set(localVariableIndex, operandStack.popLong());
                        break;
                    case 'D':
                        --localVariableIndex;
                        nextLocalVariable.set(localVariableIndex, operandStack.popDouble());
                        break;
                    case 'L':
                    case '[':
                        nextLocalVariable.set(localVariableIndex, operandStack.pop());
                        break;
                }
                --localVariableIndex;
            }

            if (!method.getClassfileMethod().isStatic()) {
                nextLocalVariable.set(localVariableIndex, operandStack.pop());
            }
        }

        private static boolean printHook(MethodArea.Method method, String[] argumentTypes,
                Frame.OperandStack operandStack) {
            Map<String, Consumer<Object>> fnMap = new HashMap<>();
            fnMap.put("println", System.out::println);
            fnMap.put("print", System.out::print);
            String methodName = method.getName();
            if (method.getClazz().getClassName().equals("java/io/PrintStream")) {
                if (methodName.equals("println") || methodName.equals("print")) {
                    if (argumentTypes.length == 0) {
                        System.out.println("");
                    } else {
                        switch (argumentTypes[0]) {
                            case "F":
                                fnMap.get(methodName).accept(operandStack.popFloat());
                                break;
                            case "D":
                                fnMap.get(methodName).accept(operandStack.popDouble());
                                break;
                            case "J":
                                fnMap.get(methodName).accept(operandStack.popLong());
                                break;
                            default:
                                Object result = operandStack.pop();
                                if (result instanceof Heap.Instance) {
                                    Heap.Instance instance = (Heap.Instance)result;
                                    if (instance.getClazz().getClassName().equals("java/lang/String")) {
                                        // Heap.ArrayInstance charsInstance = (Heap.ArrayInstance)
                                        // instance.findField("value", "[B").getValue();
                                        Heap.ArrayInstance charsInstance = null;
                                        for (MethodArea.Field stringField : instance.getFields()) {
                                            if (stringField.getName().equals("value")) {
                                                charsInstance = (Heap.ArrayInstance)stringField.getValue();
                                                break;
                                            }
                                        }
                                        int[] ints = (int[])charsInstance.getFields();
                                        char[] chars = new char[ints.length];
                                        for (int i = 0; i < chars.length; ++i) {
                                            chars[i] = (char)ints[i];
                                        }
                                        fnMap.get(methodName).accept(new String(chars));
                                    } else {
                                        fnMap.get(methodName).accept(instance);
                                    }
                                } else {
                                    fnMap.get(methodName).accept(result);
                                }
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
