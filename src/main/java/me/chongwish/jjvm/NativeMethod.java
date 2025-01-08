package me.chongwish.jjvm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import me.chongwish.jjvm.RuntimeDataArea.Heap;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;

/**
 * Native Method
 * 
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
        fill("java/lang/Class", "registerNatives", operandStack -> {
        });

        fill("java/lang/System", "registerNatives", operandStack -> {
        });

        fill("jdk/internal/misc/VM", "initialize", operandStack -> {
            MethodArea.Clazz vm = MethodArea.findClazz("jdk/internal/misc/VM");
            vm.getClassLoader().load("java/util/HashMap");
            MethodArea.Field field = vm.findField("savedProps", "Ljava/util/Map;");
            field.setValue(MethodArea.findClazz("java/util/HashMap").makeInstance());
        });

        fill("sun/misc/VM", "initialize", operandStack -> {
            MethodArea.Clazz vm = MethodArea.findClazz("sun/misc/VM");
            vm.getClassLoader().load("java/util/Properties");
            MethodArea.Field field = vm.findField("savedProps", "Ljava/util/Properties;");
            Heap.Instance propsInstance = (Heap.Instance)field.getValue();
            MethodArea.Method method = propsInstance.getClazz().findMethod("setProperty",
                    "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
            Interpreter.init(new ThreadResource()).read(method).with(propsInstance, propsInstance, propsInstance)
                    .execute();
        });

        // @todo
        fill("jdk/internal/misc/VM", "initializeFromArchive", operandStack -> {
            operandStack.pop();
        });

        fill("java/lang/Class", "getPrimitiveClass", operandStack -> {
            Heap.Instance stringInstance = (Heap.Instance)operandStack.pop();
            operandStack.push(MethodArea.findClazz(stringInstance.toString().replace('.', '/')).getClazzInstance());
        });

        fill("java/lang/Class", "initClassName", operandStack -> {
            String clazzName = ((Heap.Instance)operandStack.pop()).getTargetClazzName();
            operandStack.push(MethodArea.Clazz.makeInstanceFrom(clazzName));
        });

        fill("java/lang/Object", "getClass", operandStack -> {
            Object object = operandStack.pop();
            if (object instanceof Heap.Instance) {
                Heap.Instance instance = (Heap.Instance)object;
                operandStack.push(instance.getClazz().getClazzInstance());
            } else if (object instanceof Heap.ArrayInstance) {
                Heap.ArrayInstance instance = (Heap.ArrayInstance)object;
                operandStack.push(instance.getArrayClazz().getClazzInstance());
            } else {
                throw new RuntimeException("Can not get a class with a unknown instance.");
            }
        });

        fill("java/lang/Class", "getName0", operandStack -> {
            Heap.Instance instance = (Heap.Instance)operandStack.pop();
            String name = instance.getTargetClazzName();
            operandStack.push(MethodArea.Clazz.makeInstanceFrom(name));
        });

        fill("java/lang/Float", "floatToRawIntBits", operandStack -> {
            operandStack.push(Float.floatToIntBits(operandStack.popFloat()));
        });

        fill("java/lang/Double", "doubleToRawLongBits", operandStack -> {
            operandStack.push(Double.doubleToLongBits(operandStack.popDouble()));
        });

        fill("java/lang/Double", "longBitsToDouble", operandStack -> {
            operandStack.push(Double.longBitsToDouble(operandStack.popLong()));
        });

        fill("java/lang/Throwable", "fillInStackTrace", operandStack -> {
            operandStack.popInt();
        });

        fill("java/lang/Class", "forName0", operandStack -> {
            @SuppressWarnings("unused")
            Object argument4 = operandStack.pop();
            @SuppressWarnings("unused")
            Object argument3 = operandStack.pop();
            @SuppressWarnings("unused")
            boolean argument2 = (int)operandStack.pop() > 0 ? true : false;
            Object argument1 = operandStack.pop();

            Heap.Instance stringInstance = (Heap.Instance)argument1;
            String clazzName = stringInstance.toString().replace('.', '/');
            stringInstance.getClazz().getClassLoader().load(clazzName);
            operandStack.push(MethodArea.findClazz(clazzName).getClazzInstance());
        });

        // @todo
        fill("java/lang/Class", "desiredAssertionStatus0", operandStack -> {
            operandStack.push(0);
        });

        // // @todo
        // fill("jdk/internal/util/SystemProps$Raw", "platformProperties", operandStack -> {
        // operandStack.push(MethodArea.findArrayClazz("[java/lang/String;").makeInstance(100));
        // });

        // // @todo
        // fill("jdk/internal/util/SystemProps$Raw", "vmProperties", operandStack -> {
        // operandStack.push(MethodArea.findArrayClazz("[java/lang/String;").makeInstance(100));
        // });

        // // @todo
        // fill("java/lang/Class", "isPrimitive", operandStack -> {
        // operandStack.push(1);
        // });

        // // @todo
        // fill("java/lang/StringUTF16", "isBigEndian", operandStack -> {
        // operandStack.push(1);
        // });

        // // @todo
        // fill("java/lang/Runtime", "maxMemory", operandStack -> {
        // operandStack.push(1000000.0);
        // });

        // // @todo
        // fill("jdk/internal/misc/Unsafe", "arrayBaseOffset0", operandStack -> {
        // try {
        // java.lang.reflect.Method method = Class.class.getDeclaredMethod("arrayBaseOffset0", java.lang.Class.class);
        // method.setAccessible(true);
        // operandStack.pop();

        // Object result = method.invoke(null, java.lang.Class.class);
        // operandStack.push(result);
        // } catch (Exception e) {
        // operandStack.push(0);
        // }
        // });

        // // @todo
        // fill("jdk/internal/misc/Unsafe", "arrayIndexScale0", operandStack -> {
        // try {
        // java.lang.reflect.Method method = Class.class.getDeclaredMethod("arrayIndexScale0", java.lang.Class.class);
        // method.setAccessible(true);
        // operandStack.pop();

        // Object result = method.invoke(null, java.lang.Class.class);
        // operandStack.push(result);
        // } catch (Exception e) {
        // operandStack.push(0);
        // }
        // });

        fill("java/lang/System", "arraycopy", operandStack -> {
            // @todo check

            int argument5 = operandStack.popInt();
            int argument4 = operandStack.popInt();
            int[] argument3 = (int[])((Heap.ArrayInstance)operandStack.pop()).getFields();
            int argument2 = operandStack.popInt();
            int[] argument1 = (int[])((Heap.ArrayInstance)operandStack.pop()).getFields();

            for (int i = argument4, j = argument2, k = 0; k < argument5; ++i, ++j, ++k) {
                argument3[i] = argument1[j];
            }
        });

        // @todo
        fill("java/lang/String", "intern", operandStack -> {
        });

        fill("java/lang/Object", "hashCode", operandStack -> {
            operandStack.push(operandStack.pop().hashCode());
        });
    }
}
