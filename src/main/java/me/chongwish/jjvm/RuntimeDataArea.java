package me.chongwish.jjvm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

/**
 * Runtime data areas for java.
 *
 * <pre>
 * There are 3 section here:
 *   1. ThreadResource (Thread owened)
 *      1.1. Program Counter Register
 *      1.2. Java Stack
 *      1.3. Native Method Stack
 *   2. Heap (Memory shared)
 *      Instance and ArrayInstance will be retained here
 *   3. Method Area (Memory shared)
 *      Method & Field & Clazz & Array & Runtime Constant Pool will be retained here
 * </pre>
 */
final class RuntimeDataArea {
    private volatile static Map<Long, ThreadResource> _threadResourceCache = new HashMap<>();

    /**
     * Class Thread is used to store Program Counter Register and Java Stack
     */
    @Getter
    final public static class ThreadResource {
        public ThreadResource() {}

        public static ThreadResource getCurrentThreadResource() {
            return _threadResourceCache.get(Thread.currentThread().getId());
        }

        /**
         * Program Counter Register
         */
        private int pcRegister;

        /**
         * Java Stack
         */
        private JavaStack javaStack = new JavaStack();

        /**
         * Create a instance of class `ThreadResource` for a thread. Every thread only can hold one instance.
         */
        public static void createThreadResource() {
            long currentId = Thread.currentThread().getId();
            if (!_threadResourceCache.containsKey(currentId)) {
                ThreadResource threadResource = new ThreadResource();
                _threadResourceCache.put(Thread.currentThread().getId(), threadResource);
            }
        }
    }

    /**
     * Class JavaStack is used to store Frame of methods. The spec of java said that
     * a StackOverflowError or a OutOfMemoryError was happend when memory was
     * insuffient. But I ignore that.
     */
    final public static class JavaStack {
        private Deque<Frame> stack = new ArrayDeque<>();

        public void push(Frame frame) {
            stack.push(frame);
        }

        public Frame pop() {
            return stack.pop();
        }

        public boolean isEmpty() {
            return stack.isEmpty();
        }

        public Frame current() {
            return stack.peek();
        }
    }

    // @todo
    final public static class NativeMethodStack {
    }

    /**
     * A logic data structure for discribing a java heap.
     */
    final public static class Heap {
        /**
         * A logic data structure for discribing a java instance.
         * <pre>
         * For example:
         *   A java instance:
         *
         *   ```java
         *   User user = new User();
         *   user.name = "chongwish";
         *   ```
         *
         *   A instance of Class `Instance`:
         *
         *   ```java
         *   Instance userInstance = ...;
         *   userInstance.fields[nameIndex] = "chongwish";
         *   ```
         * </pre>
         */
        @Getter
        public static class Instance extends MethodArea.FieldData {
            private MethodArea.Clazz clazz;

            private Instance parent;

            /**
             * Just like the java operator `instanceof`.
             * @param otherClazz  target class type
             * @return  is this instance the target class type
             */
            public boolean isInstanceOf(MethodArea.Clazz otherClazz) {
                return clazz.lookLike(otherClazz);
            }
        }

        /**
         * A logic data structure for discribing a java array instance.
         */
        @Getter
        public static class ArrayInstance {
            private MethodArea.ArrayClazz arrayClazz;

            /**
             * Array data storage.
             * <pre>
             * Data can be:
             *   int[] int[][] ...
             *   double[] double[] ...
             *   ...
             *   Object[] Object[][] ...
             * </pre>
             */
            @Setter
            private Object fields;

            /**
             * Size of fields.
             */
            private int size;

            /**
             * Just like the java array operator `instanceof`.
             * @param otherClazz  target class type
             * @return  is this instance the target class type
             */
            public boolean isInstanceOf(String name) {
                return arrayClazz.lookLike(name);
            }
        }
    }

    /**
     * A logic data structure for discribing a java method area.
     * <pre>
     * The Usage:
     *   ```java
     *   MethoadArea.store(clazz);
     *   MethoadArea.store(runtimeConstantPool);
     *   MethoadArea.findClazz("classNameWithNamespace");
     *   MethoadArea.findArrayClazz("arrayClassName");
     *   ```
     * </pre>
     */
    final public static class MethodArea {
        final public static String FIRST_CLASS_NANE = "java/lang/Object";
        final public static String CLASS_INFO_NAME = "java/lang/Class";

        /**
         * Clazz storage.
         */
        private static volatile Map<String, Clazz> _clazzCache = new HashMap<>();

        /**
         * ArrayClazz storage.
         */
        private static volatile Map<String, ArrayClazz> _arrayClazzCache = new HashMap<>();

        /**
         * Runtime constant pool storage.
         */
        private static volatile Map<String, RuntimeConstantPool> _runtimeConstantPoolCache = new HashMap<>();

        /**
         * Record the given instance of class `Clazz`.
         * @param clazz  a instance of class `Clazz`
         * @return  has been the clazz existed
         */
        public static boolean store(Clazz clazz) {
            String className = clazz.className;
            if (!_clazzCache.containsKey(className)) {
                synchronized (_clazzCache) {
                    if (!_clazzCache.containsKey(className)) {
                        _clazzCache.put(className, clazz);
                        return true;
                    }
                }
            }
            return false;
        }


        /**
         * Record the given instance of class `RuntimeConstantPool`.
         * @param runtimeConstantPool  a instance of class `RuntimeConstantPool`
         */
        public static void store(RuntimeConstantPool runtimeConstantPool) {
            String className = runtimeConstantPool.getClazz().className;
            if (!_runtimeConstantPoolCache.containsKey(className)) {
                synchronized (_runtimeConstantPoolCache) {
                    if (!_runtimeConstantPoolCache.containsKey(className)) {
                        _runtimeConstantPoolCache.put(className, runtimeConstantPool);
                    }
                }
            }
        }

        /**
         * Get a instance of class `Clazz` by the given name.
         * @param className  the given class name
         * @return  a instance of class `Clazz`
         */
        public static Clazz findClazz(String className) {
            return _clazzCache.get(className);
        }

        /**
         * Get a instance of class `RuntimeConstantPool` by the given name.
         * @param className  the given class name
         * @return  a instance of class `RuntimeConstantPool`
         */
        public static RuntimeConstantPool findRuntimeConstantPool(String className) {
            return _runtimeConstantPoolCache.get(className);
        }

        /**
         * Get a instance of class `ArrayClazz` by the given name. If this instance is not exist, it will generate a new instance automatic.
         * @param arrayClazzName  the given array class name
         * @return  a instance of class `ArrayClazz`
         */
        public static ArrayClazz findArrayClazz(String arrayClazzName) {
            if (!arrayClazzName.startsWith("[")) {
                throw new RuntimeException("Class " + arrayClazzName + " is not a valid array type.");
            }
            if (!_arrayClazzCache.containsKey(arrayClazzName)) {
                _clazzCache.get("java/lang/Object").classLoader.load(arrayClazzName);
            }
            return _arrayClazzCache.get(arrayClazzName);
        }

        /**
         * A logic data storage of field of the java instance and java class.
         */
        @Getter
        @Setter
        abstract private static class FieldData {
            private Field[] fields;

            abstract public FieldData getParent();

            /**
             * Get a instance of class `Field` by the given information. It will search all its inheritance tree.
             * @param name  field name
             * @param descriptor  field descriptor
             * @return  a instance of class `Field`
             */
            public Field findField(String name, String descriptor) {
                FieldData object = this;
                do {
                    for (MethodArea.Field field : object.fields) {
                        if (field.getName().equals(name) && field.getDescriptor().equals(descriptor)) {
                            return field;
                        }
                    }
                    object = object.getParent();
                } while (object != null);

                throw new RuntimeException("Field[" + name + "," + descriptor + "] can not be found!");
            }

            /**
             * The helper method to create a array of instance of class `Field`.
             * <pre>
             * The instance of class `Classfile` include static fields and instance fields.
             * The indexList would be a list of static field list or a list of instance field index.
             * </pre>
             * @param indexList  a list include the index of a array
             * @param classfileInformation  a instance of class `Classfile`
             * @return  a array of instance of class `Field`
             */
            protected Field[] generateFields(List<Integer> indexList, Classfile.Information classfileInformation) {
                // classfile constant pool
                Classfile.ConstantPool[] constantPools = classfileInformation.getConstantPool();

                // classfile fields
                Classfile.Field[] classfileFields = classfileInformation.getFields();

                fields = new Field[indexList.size()];
                for (int i = 0; i < indexList.size(); ++i) {
                    Classfile.Field classfileField = classfileFields[indexList.get(i)];
                    Field field = new Field();
                    field.classfileField = classfileField;
                    field.clazz = (Clazz) this;
                    field.name = constantPools[classfileField.getNameIndex()].getValue().toString();
                    field.descriptor = constantPools[classfileField.getDescriptorIndex()].getValue().toString();
                    fields[i] = field;
                }

                return fields;
            }
        }

        /**
         * A logic data structure for discribing a java class.
         * <pre>
         * For example:
         *   A java instance:
         *
         *   ```java
         *   User.prefix = "name: ";
         *   ```
         *   A instance of class `Clazz`:
         *
         *   ```java
         *   Clazz userClazz = ...;
         *   userClazz.fields[prefixIndex] = "name: ";
         *   ```
         * </pre>
         * <pre>
         * The Usage:
         *   ```java
         *   Clazz.generate("classNameWithNamespace");
         *   Clazz.initialize(clazz);
         *   Instance instance = clazz.makeInstance();
         *   clazz.findField("fieldName");
         *   clazz.findMethod("methodName");
         *   clazz.findInterfaceMethod("interfaceMethodName");
         *   clazz.lookLike(otherClazz);
         *   clazz.isImplementOf(otherClazz);
         *   clazz.isParentOf(otherClazz);
         *   ```
         * </pre>
         */
        @Getter
        final public static class Clazz extends FieldData {
            private ClassLoader classLoader;

            private String className;

            private Clazz parent;

            private Classfile.Information classfileInformation;

            private Clazz[] interfaces;

            private Method[] methods;

            private Heap.Instance clazzInstance;

            /**
             * Static field index list.
             */
            private List<Integer> staticFieldIndexList = new ArrayList<>();

            /**
             * Instance field index list
             */
            private List<Integer> instanceFieldIndexList = new ArrayList<>();

            /**
             * Create a instance of class `Clazz`.
             * @param classfileInformation  a instance of class `Classfile`
             * @param classLoader  a instance of class `ClassLoader`
             * @return  a instance of Class `Clazz`
             */
            public static Clazz generate(Classfile.Information classfileInformation, ClassLoader classLoader) {
                String className = classfileInformation.getClassName();

                Clazz clazz = new Clazz();
                clazz.className = className;
                clazz.classLoader = classLoader;
                clazz.classfileInformation = classfileInformation;

                // super class && interfaces, classloader will load super class & interface first
                clazz.parent = className.equals(FIRST_CLASS_NANE) ? null : _clazzCache.get(classfileInformation.getSuperClassName());
                List<String> interfaceNameList = classfileInformation.getInterfaceNameList();
                clazz.interfaces = new Clazz[interfaceNameList.size()];
                for (int i = 0; i < interfaceNameList.size(); ++i) {
                    clazz.interfaces[i] = _clazzCache.get(interfaceNameList.get(i));
                }

                return clazz;
            }

            /**
             * Assign a instance of java class to instance of class `Clazz`.
             * @param clazz  a instance of class `Clazz`
             */
            public static void classify(Clazz clazz) {
                if (_clazzCache.containsKey(CLASS_INFO_NAME)) {
                    clazz.clazzInstance = _clazzCache.get(CLASS_INFO_NAME).makeInstance();
                }
            }

            /**
             * Assign instance of java class for every loaded instance of class `Clazz`.
             */
            public static void classify() {
                Clazz classClazz = _clazzCache.get(CLASS_INFO_NAME);
                _clazzCache.forEach((name, clazz) -> {
                        clazz.clazzInstance = classClazz.makeInstance();
                    });
            }

            /**
             * Call the static initializer of a java class.
             * @param clazz  a instance of Class `Clazz`
             */
            public static void initialize(Clazz clazz) {
                Method method = clazz.findMethod("<clinit>", "()V");
                if (method != null) {
                    Interpreter.init(new ThreadResource()).read(method).execute();
                }
            }

            /**
             * Generate a instance of class `Instance` for this instance of class `Clazz`. If this class has a super class, it will generate a instance for its super class recursively.
             * @return  a instance of class `Instance`.
             */
            public Heap.Instance makeInstance() {
                Clazz clazz = this;

                Heap.Instance instance = new Heap.Instance();
                Heap.Instance currentInstance = instance;

                // generate parent instance recursively
                do {
                    instance.clazz = clazz;

                    // constant pool
                    Classfile.ConstantPool[] constantPools = clazz.classfileInformation.getConstantPool();

                    // field
                    Field[] fields = new Field[clazz.instanceFieldIndexList.size()];
                    Classfile.Field[] classfileFields = clazz.classfileInformation.getFields();
                    for (int i = 0; i < clazz.instanceFieldIndexList.size(); ++i) {
                        Classfile.Field classfileField = classfileFields[clazz.instanceFieldIndexList.get(i)];
                        Field field = new Field();
                        field.classfileField = classfileField;
                        field.clazz = clazz;
                        field.name = constantPools[classfileField.getNameIndex()].getValue().toString();
                        field.descriptor = constantPools[classfileField.getDescriptorIndex()].getValue().toString();
                        fields[i] = field;
                    }
                    instance.setFields(fields);

                    clazz = (Clazz) clazz.getParent();
                    if (clazz != null) {
                        instance.parent = new Heap.Instance();
                        instance = instance.getParent();
                    } else {
                        break;
                    }
                } while (true);

                return currentInstance;
            }

            /**
             * Generate a instance of class `Instance` from a string.
             * @param stringValue  a string
             */
            public static Heap.Instance makeInstanceFrom(String stringValue) {
                char[] chars = stringValue.toCharArray();
                Heap.Instance stringInstance = MethodArea.findClazz("java/lang/String").makeInstance();
                Heap.ArrayInstance charsInstance = MethodArea.findArrayClazz("[B").makeInstance(chars.length);
                int[] fields = (int[]) charsInstance.getFields();
                for (int i = 0; i < fields.length; ++i) {
                    fields[i] = chars[i];
                }
                for (MethodArea.Field field : stringInstance.getFields()) {
                    switch (field.getName()) {
                        case "value":
                            field.setValue(charsInstance);
                            break;
                        case "hash":
                            field.setValue(stringValue.hashCode());
                            break;
                        case "coder":
                            field.setValue(1);
                            break;
                    }
                }
                return stringInstance;
            }


            /**
             * Get a instance of class `Method` by the given information. It will search all its inheritance tree.
             * @param name  method name
             * @param descriptor  method descriptor
             * @return  a instance of class `Method`
             */
            public Method findMethod(String name, String descriptor) {
                Clazz clazz = this;
                do {
                    for (Method method: clazz.methods) {
                        if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                            return method;
                        }
                    }
                    clazz = (Clazz) clazz.getParent();
                } while (clazz != null);

                return findInterfaceMethod(name, descriptor);
            }

            /**
             * Get a instance of class `Method` by the given information, and this instance must be a interface method instance. It will search all its inheritance tree.
             * @param name  interface method name
             * @param descriptor  interface method descriptor
             * @return  a instance of class `Method`
             */
            public Method findInterfaceMethod(String name, String descriptor) {
                Clazz clazz = this;

                for (Method method : clazz.methods) {
                    if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                        return method;
                    }
                }

                do {
                    for (Clazz interfaceClazz : interfaces) {
                        for (Method method : interfaceClazz.methods) {
                            if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                                return method;
                            }
                        }
                    }
                    clazz = (Clazz) clazz.getParent();
                } while (clazz != null);

                return null;
            }

            /**
             * Verifing this class has a interface that the given one or this class is a child class of the given class.
             * @param otherClazz  a instance of class `Clazz`
             * @return  is it a child class of the given class or has it implemented the interface that the given one
             */
            public boolean lookLike(Clazz otherClazz) {
                Clazz clazz = this;
                if (otherClazz.classfileInformation.isInterface()) {
                    return clazz.isImplementOf(otherClazz);
                } else {
                    if (clazz == otherClazz) {
                        return true;
                    }
                    return clazz.isParentOf(otherClazz);
                }
            }

            /**
             * Verifing this class has a interface that the given one.
             * @param otherClazz  a interface instance of class `Clazz`
             * @return  has it implemented the interface
             */
            public boolean isImplementOf(Clazz otherClazz) {
                Clazz clazz = this;

                if (clazz == otherClazz) {
                    return true;
                }

                do {
                    for (Clazz interfaceClazz: clazz.interfaces) {
                        if (interfaceClazz == otherClazz) {
                            return true;
                        }
                    }
                    clazz = (Clazz) clazz.getParent();
                } while (clazz != null);

                return false;
            }

            /**
             * Verifing this class is a child class of the given one.
             * @param otherClazz  a instance of class `Clazz`
             * @return  is it a child class of the given class
             */
            public boolean isParentOf(Clazz otherClazz) {
                Clazz parent = (Clazz) this.getParent();
                do {
                    if (parent == otherClazz) {
                        return true;
                    }
                    parent = (Clazz) parent.getParent();
                } while (parent != null);
                return false;
            }

            // @todo
            public boolean isAccessibleTo(Clazz otherClass) {
                return true;
            }
        }

        /**
         * A logic data structure for discribing a java array class.
         * <pre>
         * The Usage:
         *   ```java
         *   ArrayClazz.generate("arrayClassName");
         *   arrayClazz.lookLike("arrayClassName");
         *   arrayClazz.getFieldType();
         *   arrayClazz.makeInstance();
         *   ```
         * </pre>
         */
        @Getter
        final public static class ArrayClazz {
            private ClassLoader classLoader;

            private String arrayClazzName;

            private Clazz parent;

            private Clazz[] interfaces;

            private Heap.Instance clazzInstance;

            /**
             * Create a instance of class `ArrayClazz`.
             * @param arrayClazzName  array clazz name
             */
            public static void generate(String arrayClazzName) {
                if (!_arrayClazzCache.containsKey(arrayClazzName)) {
                    synchronized (_arrayClazzCache) {
                        if (!_arrayClazzCache.containsKey(arrayClazzName)) {
                            ArrayClazz arrayClazz = new ArrayClazz();

                            arrayClazz.arrayClazzName = arrayClazzName;

                            arrayClazz.parent = _clazzCache.get(MethodArea.FIRST_CLASS_NANE);

                            arrayClazz.classLoader = arrayClazz.parent.classLoader;

                            String[] interfaceNames = new String[] { "java/lang/Cloneable", "java/io/Serializable" };

                            arrayClazz.interfaces = new Clazz[interfaceNames.length];
                            for (int i = 0; i < interfaceNames.length; ++i) {
                                arrayClazz.classLoader.load(interfaceNames[i]);
                                arrayClazz.interfaces[i] = _clazzCache.get(interfaceNames[i]);
                            }

                            classify(arrayClazz);

                            _arrayClazzCache.put(arrayClazzName, arrayClazz);
                        }
                    }
                }
            }

            /**
             * Assign a instance of java class to instance of class `ArrayClazz`.
             * @param arrayClazz  a instance of class `ArrayClazz`
             */
            public static void classify(ArrayClazz arrayClazz) {
                if (_clazzCache.containsKey(CLASS_INFO_NAME)) {
                    arrayClazz.clazzInstance = _clazzCache.get(CLASS_INFO_NAME).makeInstance();
                }
            }

            /**
             * Assign instance of java class for every loaded instance of class `Clazz`.
             */
            public static void classify() {
                Clazz classClazz = _clazzCache.get(CLASS_INFO_NAME);
                _arrayClazzCache.forEach((name, arrayClazz) -> {
                    arrayClazz.clazzInstance = classClazz.makeInstance();
                });
            }

            /**
             * Generate a instance of class `ArrayInstance` for this instance of class `ArrayClazz`.
             * @return  a instance of class `ArrayInstance`
             */
            public Heap.ArrayInstance makeInstance(int size) {
                Heap.ArrayInstance arrayInstance = new Heap.ArrayInstance();
                arrayInstance.arrayClazz = this;
                arrayInstance.size = size;

                switch (arrayClazzName) {
                    case "[Z":
                    case "[B":
                    case "[C":
                    case "[S":
                    case "[I":
                        arrayInstance.fields = new int[size];
                        break;
                    case "[J":
                        arrayInstance.fields = new long[size];
                        break;
                    case "[F":
                        arrayInstance.fields = new float[size];
                        break;
                    case "[D":
                        arrayInstance.fields = new double[size];
                        break;
                    default:
                        arrayInstance.fields = new Object[size];
                }
                return arrayInstance;
            }

            /**
             * Verifing this array class is a child class of the given one.
             * @param arrayClazzName  array class name
             * @return  is it a child class of the given one
             */
            public boolean lookLike(String otherArrayClazzName) {
                if (arrayClazzName.equals(otherArrayClazzName)) {
                    return true;
                }
                if (parent.getClassName().equals(otherArrayClazzName)) {
                    return true;
                }
                for (Clazz interfaceClazz: interfaces) {
                    if (interfaceClazz.getClassName().equals(otherArrayClazzName)) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * Get the field type.
             */
            public String getFieldType() {
                String fieldType = arrayClazzName.substring(1);
                if (fieldType.charAt(0) == 'L') {
                    return fieldType.substring(1, fieldType.length() - 1);
                }
                return fieldType;
            }
        }


        /**
         * A logic data structure for discribing a java method.
         */
        @Getter
        final public static class Method {
            private Clazz clazz;

            private Classfile.Method classfileMethod;

            private String name;
            private String descriptor;

            /**
             * Maximum quantity of frame stack.
             */
            private int maxStack;

            /**
             * Maximum quantity of frame local variables.
             */
            private int maxLocals;

            private byte[] code;

            /**
             * Create a array of instance of class `Method` and fill them to the given `clazz`.
             * @param clazz  a instance of class `Clazz`
             * @return  a array of instance of class `Method`
             */
            public static Method[] generate(Clazz clazz) {
                Classfile.Information classfileInformation = clazz.classfileInformation;

                // classfile constant pool
                Classfile.ConstantPool[] constantPools = classfileInformation.getConstantPool();

                Classfile.Method[] classfileMethods = classfileInformation.getMethods();
                Method[] methods = new Method[classfileMethods.length];
                for (int i = 0; i < classfileMethods.length; ++i) {
                    Method method = new Method();
                    method.clazz = clazz;
                    method.classfileMethod = classfileMethods[i];
                    method.name = constantPools[classfileMethods[i].getNameIndex()].getValue().toString();
                    method.descriptor = constantPools[classfileMethods[i].getDescriptorIndex()].getValue().toString();

                    // attribute
                    for (Classfile.Attribute attribute : classfileMethods[i].getAttributes()) {
                        // bytecode
                        if (attribute instanceof Classfile.AttributePredefined.Code) {
                            Classfile.AttributePredefined.Code codeAttribute = (Classfile.AttributePredefined.Code) attribute;
                            method.code = codeAttribute.getCode();
                            method.maxStack = codeAttribute.getMaxStack();
                            method.maxLocals = codeAttribute.getMaxLocals();
                            break;
                        }
                    }
                    methods[i] = method;
                }
                clazz.methods = methods;

                return methods;
            }

            /**
             * Get the type of arguments by parsing the descriptor.
             * <pre>
             * For example:
             *   descriptor = "([I[[J[Llang/java/Object;IJ)V"
             *   return ["[I", "[[J", "[Llang/java/Object;", "I", "J"]
             * </pre>
             * @return  a type list
             */
            public String[] getArgumentTypes() {
                String re = "\\[*L[^;]+;|\\[*[ZBCSIFDJ]";
                // jdk 8 error
                // return Pattern.compile(re)
                //     .matcher(descriptor.substring(1, descriptor.lastIndexOf(")")))
                //     .results()
                //     .map(MatchResult::group)
                //     .toArray(String[]::new);
                List<String> result = new ArrayList<>();
                Matcher matcher = Pattern.compile(re).matcher(descriptor.substring(1, descriptor.lastIndexOf(")")));
                while (matcher.find()) {
                    result.add(matcher.group());
                }
                return result.stream().toArray(String[]::new);
            }

            /**
             * Get the type of return by parsing the descriptor.
             */
            public String getReturnType() {
                return descriptor.substring(descriptor.lastIndexOf(")") + 1);
            }

            // @todo
            public boolean isAccessibleTo(Clazz otherClass) {
                return true;
            }
        }

        /**
         * A logic data structure for discribing a java field.
         */
        @Getter
        final public static class Field {
            private Clazz clazz;

            private Classfile.Field classfileField;

            private String name;
            private String descriptor;

            private Object value;

            /**
             * Create a array of instance of class `Field` and fill them to the given `clazz`.
             * @param clazz  a instnace of class `Clazz`
             * @return  a array of instance of class `Field`
             */
            public static Field[] generate(Clazz clazz) {
                Classfile.Information classfileInformation = clazz.classfileInformation;

                Classfile.Field[] classfileFields = classfileInformation.getFields();
                for (int i = 0; i < classfileFields.length; ++i) {
                    if (!classfileFields[i].isStatic()) {
                        clazz.instanceFieldIndexList.add(i);
                    } else {
                        clazz.staticFieldIndexList.add(i);
                    }
                }

                return clazz.generateFields(clazz.staticFieldIndexList, classfileInformation);
            }

            /**
             * Assign the const value to the `final static` field in the instance of class `Clazz`.
             * @param clazz  a instance of class `Clazz`
             */
            public static void initialize(Clazz clazz) {
                Classfile.Information classfileInformation = clazz.classfileInformation;

                // constant pool
                Classfile.ConstantPool[] constantPools = classfileInformation.getConstantPool();

                for (Field field: clazz.getFields()) {
                    Classfile.Field classfileField = field.classfileField;

                    if (classfileField.isFinal()) {
                        for (Classfile.Attribute attribute : classfileField.getAttributes()) {
                            if (attribute instanceof Classfile.AttributePredefined.ConstantValue) {
                                int constantValueIndex = ((Classfile.AttributePredefined.ConstantValue) attribute).getConstantValueIndex();
                                switch (field.descriptor) {
                                    case "Z":
                                    case "B":
                                    case "C":
                                    case "S":
                                    case "I":
                                        field.value = (int) constantPools[constantValueIndex].getValue();
                                        break;
                                    case "F":
                                        field.value = (float) constantPools[constantValueIndex].getValue();
                                        break;
                                    case "J":
                                        field.value = (long) constantPools[constantValueIndex].getValue();
                                        break;
                                    case "D":
                                        field.value = (double) constantPools[constantValueIndex].getValue();
                                        break;
                                    case "Ljava/lang/String;":
                                        int stringIndex = (int) constantPools[constantValueIndex].getValue();
                                        field.value = constantPools[stringIndex].getValue();
                                }
                                break;
                            }
                        }
                    }
                }

            }

            public void setValue(Object value) {
                this.value = value;
            }

            public int getIntValue() {
                return value == null ? 0 : (int) value;
            }

            public long getLongValue() {
                return value == null ? 0l : (long) value;
            }

            public float getFloatValue() {
                return value == null ? 0.0f : (float) value;
            }

            public double getDoubleValue() {
                return value == null ? 0.0: (double) value;
            }

            // @todo
            public boolean isAccessibleTo(Clazz otherClazz) {
                return true;
            }
        }

        /**
         * A logic data structure for discribing a java runtime constant pool.
         */
        @Getter
        final public static class RuntimeConstantPool {
            private Clazz clazz;

            private ClassLoader classLoader;

            private Classfile.ConstantPool[] constantPools;

            /**
             * Create a instance of class `RuntimeConstantPool`.
             * @param clazz  a instance of class `Clazz`
             * @return  a instance of class `RuntimeConstantPool`
             */
            public static RuntimeConstantPool generate(Clazz clazz) {
                RuntimeConstantPool runtimeConstantPool = new RuntimeConstantPool();
                runtimeConstantPool.classLoader = clazz.getClassLoader();
                runtimeConstantPool.clazz = clazz;
                runtimeConstantPool.constantPools = clazz.classfileInformation.getConstantPool();
                return runtimeConstantPool;
            }

            /**
             * Get a value from constant pool.
             */
            public Classfile.ConstantPool dereference(int index) {
                return constantPools[index];
            }

            /**
             * Get a string from constant pool.
             */
            public String dereferenceString(int index) {
                int classIndex = (int) (constantPools[index].getValue());
                return constantPools[classIndex].getValue().toString();
            }

            /**
             * Get a instance of class `Clazz` from constant pool.
             */
            public Clazz dereferenceClazz(int index) {
                String className = dereferenceString(index);
                classLoader.load(className);
                Clazz clazz = MethodArea.findClazz(className);
                if (!clazz.isAccessibleTo(this.clazz)) {
                    throw new RuntimeException("Class " + this.clazz.getClassName()+ " can not reference Class " + clazz.getClassName() + ".");
                }
                return clazz;
            }

            /**
             * Get a instance of class `ArrayClazz` from constant pool.
             */
            public ArrayClazz dereferenceArrayClazz(int index) {
                String arrayClassName = dereferenceString(index);
                classLoader.load(arrayClassName);
                ArrayClazz arrayClazz = MethodArea.findArrayClazz(arrayClassName);
                return arrayClazz;
            }

            /**
             * Get a pair value from constant pool.
             */
            public int[] dereferenceReference(int index) {
                return (int[]) (constantPools[index].getValue());
            }

            /**
             * Get a name and a type from constant pool.
             */
            public String[] dereferenceNameAndType(int index) {
                int[] nameIndexAndTypeIndex = dereferenceReference(index);
                return new String[] { constantPools[nameIndexAndTypeIndex[0]].getValue().toString(), constantPools[nameIndexAndTypeIndex[1]].getValue().toString() };
            }

            /**
             * Get a instance of class `Field` from constant pool.
             */
            public Field dereferenceField(int index) {
                // field ref: key => classIndex, value => nameAndTypeIndex
                int[] fieldRef = dereferenceReference(index);

                Clazz clazz = dereferenceClazz(fieldRef[0]);
                String[] nameAndType = dereferenceNameAndType(fieldRef[1]);

                // search field
                Field field = clazz.findField(nameAndType[0], nameAndType[1]);
                if (!field.isAccessibleTo(this.clazz)) {
                    throw new RuntimeException("Class " + this.clazz.getClassName() + " can not reference Field " + clazz.getClassName() + "." + field.getName() + ".");
                }

                return field;
            }

            /**
             * Get a instance of class `Method` from constant pool.
             */
            public Method dereferenceMethod(int index) {
                // method ref: key => classIndex, value => nameAndTypeIndex
                int[] methodRef = dereferenceReference(index);

                Clazz clazz = dereferenceClazz(methodRef[0]);
                if (clazz.classfileInformation.isInterface()) {
                    throw new RuntimeException(clazz.getClassName() + " is a interface.");
                }

                String[] nameAndType = dereferenceNameAndType(methodRef[1]);

                // search method
                Method method = clazz.findMethod(nameAndType[0], nameAndType[1]);
                if (method == null) {
                    throw new RuntimeException("Can not find the method " + nameAndType[0] + ".");
                }
                if (!method.isAccessibleTo(this.clazz)) {
                    throw new RuntimeException("Class " + this.clazz.getClassName() + " can not reference method " + clazz.getClassName() + "." + method.getName() + ".");
                }

                return method;
            }

            /**
             * Get a instance of class `Method` which is a interface method from constant pool.
             */
            public Method dereferenceInterfaceMethod(int index) {
                // interface method ref: key => interfaceIndex, value => nameAndTypeIndex
                int[] interfaceMethodRef = dereferenceReference(index);

                Clazz interfaceClazz = dereferenceClazz(interfaceMethodRef[0]);
                if (!interfaceClazz.classfileInformation.isInterface()) {
                    throw new RuntimeException(interfaceClazz.getClassName() + " is not a interface.");
                }

                String[] nameAndType = dereferenceNameAndType(interfaceMethodRef[1]);

                // search method
                Method method = interfaceClazz.findInterfaceMethod(nameAndType[0], nameAndType[1]);
                if (method == null) {
                    throw new RuntimeException("Can not find the interface method " + nameAndType[0] + ".");
                }

                return method;
            }
        }

    }
}
