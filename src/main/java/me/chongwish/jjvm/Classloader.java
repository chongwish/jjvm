package me.chongwish.jjvm;

import me.chongwish.jjvm.RuntimeDataArea.MethodArea;

/**
 * Class `ClassLoader` is used for storing the information of a class at runtime to method area.
 *
 * <pre>
 * The Usage:
 *
 * ```java
 *   ClassLoader classLoader = new Classloader();
 *
 *   classloader.load("[I");
 *
 *   classloader.load("java/lang/Object");
 * ```
 * </pre>
 */
final class ClassLoader {
    public ClassLoader() {
        // auto load first class: Object
        load(MethodArea.FIRST_CLASS_NANE);
        load("java/lang/String");

        // auto load second class: Class
        load(MethodArea.CLASS_INFO_NAME);

        // dependency resolve
        MethodArea.Clazz.classify();
        MethodArea.ArrayClazz.classify();

        // auto load basic type
        String[] basicTypes = new String[] { "void", "boolean", "byte", "char", "short", "int", "long", "float", "double" };
        for (String basicType: basicTypes) {
            MethodArea.Clazz.generate(basicType, this);
        }
    }

    /**
     * Load a class to runtime area.
     * @param className  class name with namespace
     */
    public synchronized void load(final String className) {
        if (className.startsWith("[")) {
            // for array
            MethodArea.ArrayClazz.generate(className);
        } else {
            // for class

            if (MethodArea.findClazz(className) == null) {
                // load
                Classfile.Information classfileInformation = Classfile.readInformation(className);

                // super class
                if (!classfileInformation.getClassName().equals(MethodArea.FIRST_CLASS_NANE)) {
                    load(classfileInformation.getSuperClassName());
                }

                // interfaces
                for (String interfaceName : classfileInformation.getInterfaceNameList()) {
                    load(interfaceName);
                }

                // link
                link(classfileInformation);
            }
        }
    }

    /**
     * Clazz linking.
     * <pre>
     * Link a class need 3 step:
     *   step 1. verify
     *   step 2. prepare
     *   step 3. resolute
     * </pre>
     * @param classfileInformation  a instance of Class `Classfile`
     * @return  a instance of Class `Clazz`
     */
    private MethodArea.Clazz link(Classfile.Information classfileInformation) {
        // step 1. verify
        //  nothing

        // step 2. prepare
        //  nothing

        // step 3. resolute
        //  generate
        MethodArea.Clazz clazz = MethodArea.Clazz.generate(classfileInformation, this);
        MethodArea.Field.generate(clazz);
        MethodArea.Method.generate(clazz);
        MethodArea.RuntimeConstantPool runtimeConstantPool = MethodArea.RuntimeConstantPool.generate(clazz);

        //  store
        MethodArea.store(runtimeConstantPool);
        if (MethodArea.store(clazz)) {
            MethodArea.Clazz.classify(clazz);
            // initialize
            initialize(clazz);
        }

        return clazz;
    }

    /**
     * Clazz initialization.
     * @param clazz  a instance of Class `Clazz`
     */
    private void initialize(MethodArea.Clazz clazz) {
        MethodArea.Field.initialize(clazz);
        MethodArea.Clazz.initialize(clazz);
    }
}
