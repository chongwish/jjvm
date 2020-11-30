package me.chongwish.jjvm;

import me.chongwish.jjvm.RuntimeDataArea.JavaStack;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea.Method;

/**
 * A interpreter to execute the java bytecode.
 * <pre>
 * The Usage:
 *   ```java
 *   Interpreter.init(threadResource).read(method).execute();
 *   ```
 * </pre>
 */
final class Interpreter {
    private Interpreter() {}

    private ThreadResource threadResource;

    /**
     * Create a instance of class `Interpreter`.
     * @param threadResource  a instance of class `ThreadResource`
     * @return  a instance of class `Interpreter`
     */
    public static Interpreter init(ThreadResource threadResource) {
        Interpreter interpreter = new Interpreter();

        interpreter.threadResource = threadResource;

        return interpreter;
    }

    /**
     * Read a instance of class `Method` and load it to the current frame.
     * @param method  a instance of class `Method`
     * @return  a instance of class `Interpreter`
     */
    public Interpreter read(Method method) {
        Frame frame = new Frame(method, threadResource);
        threadResource.getJavaStack().push(frame);

        return this;
    }

    /**
     * Fill some argument to current frame.
     * @param args  arguments array
     * @return  a instance of class `Interpreter`
     */
    public Interpreter with(Object... args) {
        Frame frame = threadResource.getJavaStack().current();
        Frame.LocalVariable localVariable = frame.getLocalVariable();
        for (int i = 0; i < args.length; ++i) {
            localVariable.set(i, args[i]);
        }
        return this;
    }

    /**
     * Exceute the bytecode.
     */
    public void execute() {
        JavaStack javaStack = threadResource.getJavaStack();

        do {
            Frame frame = javaStack.current();

            Bytecode bytecode = frame.getBytecode();

            int opcode = bytecode.getU1();
            // System.out.printf("Instruction 0x%02x\n", opcode);
            Instruction.Set.get(opcode).accept(frame, bytecode);
        } while (!javaStack.isEmpty());
    }
}
