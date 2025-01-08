package me.chongwish.jjvm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import me.chongwish.jjvm.RuntimeDataArea.MethodArea;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;

public class ReflectionTest {
    @Test
    public void testClass() {
        final List<String> classpaths = new ArrayList<>();
        classpaths.add("./build/classes/java/test".replace('/', File.separatorChar));
        classpaths.add("./demo/build/classes/java/main".replace('/', File.separatorChar));
        Classpath.parse(classpaths);

        ThreadResource.createThreadResource();

        ClassLoader classLoader = new ClassLoader();

        final String className = "me.chongwish.jjvm.ReflectionTest".replace('.', '/');
        classLoader.load(className);
        MethodArea.Clazz clazz = MethodArea.findClazz(className);
        MethodArea.Method method = clazz.findMethod("getClassName", "()V");

        ThreadResource threadResource = ThreadResource.getCurrentThreadResource();
        Interpreter interpreter = Interpreter.init(threadResource);
        interpreter.read(method).execute();
    }

    @SuppressWarnings("unused")
    private void getClassName() {
        System.out.println(int.class.getName());
        System.out.println(int[].class.getName());
        System.out.println(int[][].class.getName());
        System.out.println(Integer.class.getName());
        System.out.println(Integer[].class.getName());
        System.out.println(Object[].class.getName());
        System.out.println(new String[0].getClass().getName());
        System.out.println("".getClass().getName());
        System.out.println(new double[0].getClass().getName());

        String hello = "你好,";
        String world = "world";
        String name = hello + world;
        name.intern();
        System.out.println(new Object().hashCode());
        System.out.println(new Object().hashCode());
    }
}
