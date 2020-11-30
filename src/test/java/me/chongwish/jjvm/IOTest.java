package me.chongwish.jjvm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import me.chongwish.jjvm.RuntimeDataArea.MethodArea;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;
import me.chongwish.jjvm.demo.Calculation;
import me.chongwish.jjvm.demo.sort.Bubble;

public class IOTest {
    @Test
    public void testOutput() {
        System.out.println("JVM");
        output();
        System.out.println("JJVM");

        final List<String> classpaths = new ArrayList<>();
        classpaths.add("./build/classes/java/test".replace('/', File.separatorChar));
        classpaths.add("./demo/build/classes/java/main".replace('/', File.separatorChar));

        Classpath.parse(classpaths);
        ThreadResource.createThreadResource();

        ClassLoader classLoader = new ClassLoader();

        final String className = "me.chongwish.jjvm.IOTest".replace('.', '/');
        classLoader.load(className);
        MethodArea.Clazz clazz = MethodArea.findClazz(className);
        MethodArea.Method method = clazz.findMethod("output", "()V");

        ThreadResource threadResource = ThreadResource.getCurrentThreadResource();
        Interpreter interpreter = Interpreter.init(threadResource);
        interpreter.read(method).execute();
    }

    private void output() {
        // static method
        System.out.print("fibonacci(10) = ");
        System.out.println(Calculation.fibonacci(10));

        // instanec method
        System.out.println("Bubble.sort");
        Integer[] array = new Integer[] { 5, 4, 1, 8, 12, 6 };
        System.out.print("before: ");
        for (int n : array) {
            System.out.print(n + " ");
        }
        System.out.println();
        new Bubble<Integer>().sort(array);
        System.out.print("after: ");
        for (int n : array) {
            System.out.print(n + " ");
        }
        System.out.println();
    }
}
