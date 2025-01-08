package me.chongwish.jjvm;

import me.chongwish.jjvm.RuntimeDataArea.MethodArea;
import me.chongwish.jjvm.RuntimeDataArea.ThreadResource;

public class Starter {
    public static void main(final String[] args) {
        final CommandLine commandLine = new CommandLine(args);
        Classpath.parse(commandLine.getClasspaths());

        ThreadResource.createThreadResource();
        ClassLoader classLoader = new ClassLoader();

        final String className = commandLine.getClazzName().replace('.', '/');
        classLoader.load(className);
        MethodArea.Clazz clazz = MethodArea.findClazz(className);

        MethodArea.Method mainMethod = clazz.findMethod("main", "([Ljava/lang/String;)V");
        if (mainMethod == null) {
            throw new RuntimeException("Can not find the main method!");
        }

        ThreadResource threadResource = ThreadResource.getCurrentThreadResource();

        Interpreter interpreter = Interpreter.init(threadResource);
        interpreter.read(mainMethod).execute();
    }
}
