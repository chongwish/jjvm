package me.chongwish.jjvm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import me.chongwish.jjvm.RuntimeDataArea.Heap;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea;

/**
 * Native Method
 * @todo
 */
final class NativeMethod {
    final private static Map<String, Consumer<Frame.OperandStack>> Set = new HashMap<>();

    public static Consumer<Frame.OperandStack> find(String className, String methodName) {
        return Set.get(className + "." + methodName);
    }

    public static void run(String className, String methodName, Frame.OperandStack operandStack) {
        if (!methodName.equals("registerNatives")) {
            Consumer<Frame.OperandStack> method = find(className, methodName);
            if (method == null) {
                throw new RuntimeException("Native Method: " + className + "." + methodName + " can not be found.");
            }
            method.accept(operandStack);
        }
    }

    private static void fill(String className, String methodName, Consumer<Frame.OperandStack> fn) {
        Set.put(className + "." + methodName, fn);
    }

    static {
        fill("java/lang/Class", "registerNatives", operandStack -> {});

        fill("java/lang/System", "registerNatives", operandStack -> {});

        fill("jdk/internal/misc/VM", "initialize", operandStack -> {});

        // @todo
        fill("jdk/internal/misc/VM", "initializeFromArchive", operandStack -> {
                operandStack.pop();
            });

        fill("java/lang/Class", "getPrimitiveClass", operandStack -> {
                Heap.Instance stringInstance = (Heap.Instance) operandStack.pop();
                operandStack.push(MethodArea.findClazz(stringInstance.toString()).getClazzInstance());
            });

        fill("java/lang/Class", "initClassName", operandStack -> {
                String clazzName = ((Heap.Instance) operandStack.pop()).getTargetClazzName();
                operandStack.push(MethodArea.Clazz.makeInstanceFrom(clazzName));
            });

        fill("java/lang/Object", "getClass", operandStack -> {
                Object object = operandStack.pop();
                if (object instanceof Heap.Instance) {
                    Heap.Instance instance = (Heap.Instance) object;
                    operandStack.push(instance.getClazz().getClazzInstance());
                } else if (object instanceof Heap.ArrayInstance) {
                    Heap.ArrayInstance instance = (Heap.ArrayInstance) object;
                    operandStack.push(instance.getArrayClazz().getClazzInstance());
                } else {
                    throw new RuntimeException("Can not get a class with a unknown instance.");
                }
            });

        fill("java/lang/Class", "getName0", operandStack -> {
                Heap.Instance instance = (Heap.Instance) operandStack.pop();
                String name = instance.getTargetClazzName();
                operandStack.push(MethodArea.Clazz.makeInstanceFrom(name));
            });

        // @todo
        fill("java/lang/Class", "forName0", operandStack -> {
                try {
                    java.lang.reflect.Method method = Class.class.getDeclaredMethod("forName0", Class.forName("java.lang.String"), boolean.class, java.lang.ClassLoader.class, java.lang.Class.class);
                    method.setAccessible(true);
                    Object argument4 = operandStack.pop();
                    Object argument3 = operandStack.pop();
                    boolean argument2 = (int) operandStack.pop() > 0 ? true : false;
                    Object argument1 = operandStack.pop();

                    Heap.Instance stringInstance = (Heap.Instance) argument1;
                    Object result = method.invoke(null, stringInstance.toString(), argument2, argument3, argument4);
                    operandStack.push(MethodArea.findClazz("java/lang/Class").makeInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        // @todo
        fill("java/lang/Class", "desiredAssertionStatus0", operandStack -> {
                operandStack.push(0);
            });

        // @todo
        fill("jdk/internal/util/SystemProps$Raw", "platformProperties", operandStack -> {
                operandStack.push(MethodArea.findArrayClazz("[java/lang/String;").makeInstance(100));
            });

        // @todo
        fill("jdk/internal/util/SystemProps$Raw", "vmProperties", operandStack -> {
                operandStack.push(MethodArea.findArrayClazz("[java/lang/String;").makeInstance(100));
            });

        // @todo
        fill("java/lang/Class", "isPrimitive", operandStack -> {
                operandStack.push(1);
            });

        // @todo
        fill("java/lang/StringUTF16", "isBigEndian", operandStack -> {
                operandStack.push(1);
            });

        // @todo
        fill("java/lang/Runtime", "maxMemory", operandStack -> {
                operandStack.push(1000000.0);
            });

        // @todo
        fill("jdk/internal/misc/Unsafe", "arrayBaseOffset0", operandStack -> {
                try {
                    java.lang.reflect.Method method = Class.class.getDeclaredMethod("arrayBaseOffset0", java.lang.Class.class);
                    method.setAccessible(true);
                    operandStack.pop();

                    Object result = method.invoke(null, java.lang.Class.class);
                    operandStack.push(result);
                } catch (Exception e) {
                    operandStack.push(0);
                }
            });

        // @todo
        fill("jdk/internal/misc/Unsafe", "arrayIndexScale0", operandStack -> {
                try {
                    java.lang.reflect.Method method = Class.class.getDeclaredMethod("arrayIndexScale0", java.lang.Class.class);
                    method.setAccessible(true);
                    operandStack.pop();

                    Object result = method.invoke(null, java.lang.Class.class);
                    operandStack.push(result);
                } catch (Exception e) {
                    operandStack.push(0);
                }
            });

        // @todo
        fill("java/lang/System", "arraycopy", operandStack -> {
                try {
                    java.lang.reflect.Method method = System.class.getDeclaredMethod("arraycopy", Object.class, int.class, Object.class, int.class, int.class);
                    method.setAccessible(true);
                    int argument5 = operandStack.popInt();
                    int argument4 = operandStack.popInt();
                    Object argument3 = ((Heap.ArrayInstance) operandStack.pop()).getFields();
                    int argument2 = operandStack.popInt();
                    Object argument1 = ((Heap.ArrayInstance) operandStack.pop()).getFields();

                    method.invoke(null, argument1, argument2, argument3, argument4, argument5);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });

    }
}
