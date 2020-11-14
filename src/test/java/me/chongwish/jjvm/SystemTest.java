package me.chongwish.jjvm;

import org.junit.Test;

import me.chongwish.jjvm.RuntimeDataArea.MethodArea;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;

public class SystemTest {
    @Test
    public void testInitJavaSystem() {
        ThreadResource.createThreadResource();
        ClassLoader classLoader = new ClassLoader();

        String className = "java/lang/System";
        classLoader.load(className);
        MethodArea.Clazz clazz = MethodArea.findClazz(className);
        MethodArea.Method method = clazz.findMethod("initPhase1", "()V");

        ThreadResource threadResource = ThreadResource.getCurrentThreadResource();
        // Interpreter interpreter = Interpreter.init(threadResource);
        // interpreter.read(method).execute();
    }
}
