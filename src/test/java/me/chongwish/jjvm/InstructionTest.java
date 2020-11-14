package me.chongwish.jjvm;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import me.chongwish.jjvm.RuntimeDataArea.MethodArea;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea.Clazz;
import me.chongwish.jjvm.RuntimeDataArea.MethodArea.Method;

import me.chongwish.jjvm.demo.Calculation;
import me.chongwish.jjvm.demo.FieldData;
import me.chongwish.jjvm.demo.sort.Bubble;

public class InstructionTest {
    @Test
    public void testExecute() {
        final List<String> classpaths = new ArrayList<>();
        classpaths.add("./build/classes/java/test".replace('/', File.separatorChar));
        classpaths.add("./demo/build/classes/java/main".replace('/', File.separatorChar));

        Classpath.parse(classpaths);
        ThreadResource.createThreadResource();

        ClassLoader classLoader = new ClassLoader();

        final String className = "me.chongwish.jjvm.InstructionTest".replace('.', '/');
        classLoader.load(className);
        Clazz clazz = MethodArea.findClazz(className);
        ThreadResource threadResource = ThreadResource.getCurrentThreadResource();

        Interpreter interpreter = Interpreter.init(threadResource);

        runMethod(getMethod("testIf", clazz), interpreter);
        runMethod(getMethod("testSwitch", clazz), interpreter);
        runMethod(getMethod("testCircle", clazz), interpreter);
        runMethod(getMethod("testArray", clazz), interpreter);
        runMethod(getMethod("testObject", clazz), interpreter);
        runMethod(getMethod("testMethod", clazz), interpreter);
    }

    public void testObject() {
        double d = 43.2;
        FieldData fieldData = new FieldData();
        fieldData.intField1 = 40000;
        fieldData.intField2 = (int) d;
        fieldData.floatField1 = (float) d;
        fieldData.doubleField1 = fieldData.floatField1;
        d = FieldData.doubleStaticField;
        FieldData.doubleStaticField = d;
        d = fieldData.intField1;
        d = fieldData.intField2;
        d = fieldData.doubleField1;
        d = FieldData.constDoubleField2;

        if (fieldData instanceof FieldData) {}
    }

    public void testArray() {
        int i;
        double d;
        long l;
        int[] iArray = new int[2];
        long[] lArray = new long[3];
        Double[] dArray = new Double[4];
        i = iArray.length;
        i = dArray.length;
        iArray[1] = 3;
        i = iArray[1];
        lArray[2] = 7000;
        l = lArray[2];
        dArray[2] = 33.4;
        d = dArray[2];
        dArray[1] = d;
        dArray[0] = (double) l;
        int[][][] mi = new int[4][7][8];
        mi[3][5][6] = 378;
        i = mi[3][5][6];
        mi[2][3][4] = i;
        if (mi instanceof int[][][]) {}
    }

    public void testMethod() {
        // static method
        Calculation.fibonacci(10);

        // instanec method
        Integer[] array = new Integer[] { 5, 4, 1, 8, 12, 6 };
        new Bubble<Integer>().sort(array);
    }

    public void testIf() throws Exception {
        int result = 100;
        if (result == 100) {
            result = 101;
        }
        if (result != 102) {
            result = 103;
        }
    }

    public void testSwitch() {
        final int result = 10;
        int v = 0;
        switch (result) {
        case 0:
            v = 1;
            break;
        case 1:
            v = 2;
            break;
        default:
            v = 3;
        }
        switch (result) {
        case 21:
            v = 4;
            break;
        case 11:
            v = 5;
            break;
        default:
            v = 6;
        }
        if (v == 0) {}
    }

    public void testCircle() {
        int sum = 0;
        for (int i = 1; i <10; i++) {
            sum += i;
        }

        sum = 0;
        while (true) {
            sum++;
            if (sum > 10)
                break;
        }
    }

    private Method getMethod(final String name, final Clazz clazz) {
        Method result = null;
        for (final Method m: clazz.getMethods()) {
            if (name.equals(m.getName())) {
                result = m;
                break;
            }
        }
        assertNotNull(result);
        System.out.println("Method: " + name);
        return result;
    }

    private void runMethod(Method method, Interpreter interpreter) {
        show(method.getCode());
        interpreter.read(method).execute();
    }

    private void show(final byte[] bytecode) {
        System.out.println("Hex bytecode:");
        for (final int i : bytecode) {
            System.out.printf("%02x ", i & 0xff);
        }
        System.out.println("");
    }
}

