package me.chongwish.jjvm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This standalone java file is used for parsing java class.
 * <p>
 * Seven section here:
 * 
 * <ol>
 * <li>Constant Table</li>
 * <li>Cache</li>
 * <li>Main Method</li>
 * <li>Information</li>
 * <li>Constant Pool</li>
 * <li>Field</li>
 * <li>Method</li>
 * <li>Attribute</li>
 * </ol>
 * 
 * Class {@code Information}, class {@code ConstantPool}, class {@code Field}, class {@code Method} and class
 * {@code Attribute} were data type to record useful information,
 * the they has a corresponding class which has a <b>s</b> suffix.
 * For example, Class {@code Informations} has some helper method to parse the information of a class.
 * <p>
 * 
 * <b>Usage:</b>
 * 
 * <ul>
 * <li>{@code Classfile.readBytes("classNameWithNamespace")}</li>
 * <li>{@code Classfile.readInformation("classNameWithNamespace")}</li>
 * </ul>
 *
 * The specifection of class structure:
 * 
 * @see https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html
 */
final class Classfile {
    /****************************************
     * Section 1. Constant Table Predefined *
     ****************************************/

    /**
     * Constant pool type table.
     */
    final static class CONSTANTPOOL_TABLE {
        final public static byte UTF8 = 1;
        final public static byte INTEGER = 3;
        final public static byte FLOAT = 4;
        final public static byte LONG = 5;
        final public static byte DOUBLE = 6;
        final public static byte CLASS = 7;
        final public static byte STRING = 8;
        final public static byte FIELD_REF = 9;
        final public static byte METHOD_REF = 10;
        final public static byte INTERFACE_METHOD_REF = 11;
        final public static byte NAME_AND_TYPE = 12;
        final public static byte METHOD_HANDLE = 15;
        final public static byte METHOD_TYPE = 16;
        final public static byte INVOKE_DYNAMIC = 18;
        final public static byte MODULE = 19;
        final public static byte PACKAGE = 20;
    }

    /**
     * Attribute element value tag table.
     */
    final static class ATTRIBUTE_ELEMENT_VALUE_TAG {
        final public static byte B = 66;
        final public static byte C = 67;
        final public static byte D = 68;
        final public static byte F = 70;
        final public static byte I = 73;
        final public static byte J = 74;
        final public static byte S = 83;
        final public static byte Z = 90;

        final public static byte s = 115;
        final public static byte e = 101;
        final public static byte c = 99;

        final public static byte ANNOTATION = 64;
        final public static byte ARRAY = 91;
    }

    /**
     * Attribute table, these value can be found in the constant pool.
     */
    final static class ATTRIBUTE_TABLE {
        final public static String CONSTANT_VALUE = "ConstantValue";
        final public static String CODE = "Code";
        final public static String STACK_MAP_TABLE = "StackMapTable";
        final public static String BOOTSTRAP_METHODS = "BootstrapMethods";
        final public static String NEST_HOST = "NestHost";
        final public static String NEST_MEMBERS = "NestMembers";

        final public static String EXCEPTIONS = "Exceptions";
        final public static String INNER_CLASSES = "InnerClasses";
        final public static String ENCLOSING_METHOD = "EnclosingMethod";
        final public static String SYNTHETIC = "Synthetic";
        final public static String SIGNATURE = "Signature";
        final public static String SOURCE_FILE = "SourceFile";
        final public static String LINE_NUMBER_TABLE = "LineNumberTable";
        final public static String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
        final public static String LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";

        final public static String SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
        final public static String DEPRECATED = "Deprecated";
        final public static String RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
        final public static String RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
        final public static String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
        final public static String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";
        final public static String RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
        final public static String RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = "RuntimeInvisibleTypeAnnotations";
        final public static String ANNOTATION_DEFAULT = "AnnotationDefault";
        final public static String METHOD_PARAMETERS = "MethodParameters";
        final public static String MODULE = "Module";
        final public static String MODULE_PACKAGES = "ModulePackages";
        final public static String MODULE_MAIN_CLASS = "ModuleMainClass";
    }

    /**
     * Access table for class
     */
    final static class ACCESS_CLASS_TABLE {
        final public static int ACC_PUBLIC = 0x0001;
        final public static int ACC_FINAL = 0x0010;
        final public static int ACC_SUPER = 0x0020;
        final public static int ACC_INTERFACE = 0x0200;
        final public static int ACC_ABSTRACT = 0x0400;
        final public static int ACC_SYNTHETIC = 0x1000;
        final public static int ACC_ANNOTATION = 0x2000;
        final public static int ACC_ENUM = 0x4000;
    }

    /**
     * Access table for field
     */
    final static class ACCESS_FIELD_TABLE {
        final public static int ACC_PUBLIC = 0x0001;
        final public static int ACC_PRIVATE = 0x0002;
        final public static int ACC_PROTECTED = 0x0004;
        final public static int ACC_STATIC = 0x0008;
        final public static int ACC_FINAL = 0x0010;
        final public static int ACC_VOLATILE = 0x0040;
        final public static int ACC_TRANSIENT = 0x0080;
        final public static int ACC_SYNTHETIC = 0x1000;
        final public static int ACC_ENUM = 0x4000;
    }

    /**
     * Access table for method
     */
    final static class ACCESS_METHOD_TABLE {
        final public static int ACC_PUBLIC = 0x0001;
        final public static int ACC_PRIVATE = 0x0002;
        final public static int ACC_PROTECTED = 0x0004;
        final public static int ACC_STATIC = 0x0008;
        final public static int ACC_FINAL = 0x0010;
        final public static int ACC_SYNCHRONIZED = 0x0020;
        final public static int ACC_BRIDGE = 0x0040;
        final public static int ACC_VARARGS = 0x0080;
        final public static int ACC_NATIVE = 0x0100;
        final public static int ACC_ABSTRACT = 0x0400;
        final public static int ACC_STRICT = 0x0800;
        final public static int ACC_SYNTHETIC = 0x1000;
    }

    /**
     * Access table for nested class
     */
    final static class ACCESS_NESTED_CLASS_TABLE {
        final public static int ACC_PUBLIC = 0x0001;
        final public static int ACC_PRIVATE = 0x0002;
        final public static int ACC_PROTECTED = 0x0004;
        final public static int ACC_STATIC = 0x0008;
        final public static int ACC_FINAL = 0x0010;
        final public static int ACC_INTERFACE = 0x0200;
        final public static int ACC_ABSTRACT = 0x0400;
        final public static int ACC_SYNTHETIC = 0x1000;
        final public static int ACC_ANNOTATION = 0x2000;
        final public static int ACC_ENUM = 0x4000;
    }

    /**
     * Array type table
     */
    final static class ARRAY_TYPE_TABLE {
        final public static int T_BOOLEAN = 4;
        final public static int T_CHAR = 5;
        final public static int T_FLOAT = 6;
        final public static int T_DOUBLE = 7;
        final public static int T_BYTE = 8;
        final public static int T_SHORT = 9;
        final public static int T_INT = 10;
        final public static int T_LONG = 11;
    }

    /********************
     * Section 2. Cache *
     ********************/

    /**
     * The cache of the class which has been parsed.
     */
    private static volatile Map<String, Information> _classfileCache = new HashMap<>();

    /**
     * The cache of current constant pool
     * <p>
     * <b>Only be used in the static method {@code Classfile.$type.parse}, and it can not be used any other place</b>.
     */
    private static volatile ConstantPool[] _currentConstantPoolWhenParsing;

    /**
     * Get the name in the constant pool by the index
     * <p>
     * <b>Only be used in the static method {@code Classfile.$type.parse}, and it can not be used any other place</b>.
     */
    private static String getConstantPoolStringWhenParsing(int index) {
        return _currentConstantPoolWhenParsing[index].value.toString();
    }

    /**************************
     * Section 3. Main Method *
     **************************/

    /**
     * Change a class name with namespace to a class path.
     * <p>
     * <b>Example:<b>
     * {@code me/chongwish/jvm/Main} // => {@code me $os_sep chongwish $os_sep jvm $os_sep Main.class}
     * 
     * @param className
     *        class name with namespace
     * @return the relative path of class
     */
    public static String convertToInnerRelativeFile(final String className) {
        return className.replace("/", File.separator) + ".class";
    }

    /**
     * Get a byte buffer from a class.
     * 
     * @param className
     *        class name with namespace
     * @return byte buffer of class
     */
    public static byte[] readBytes(final String className) {
        final String classfile = convertToInnerRelativeFile(className);

        try {
            return Files.readAllBytes(Classpath.getPath(classfile));
        } catch (final IOException e) {
            throw new RuntimeException("Can not read byte buffer from [" + classfile + "]");
        }
    }

    /**
     * Get class information.
     * 
     * @param className
     *        class name with namespace
     * @return class Information of the given class name
     */
    public static Information readInformation(final String className) {
        if (!_classfileCache.containsKey(className)) {
            synchronized (_classfileCache) {
                if (!_classfileCache.containsKey(className)) {
                    _classfileCache.put(className, Informations.parse(className));
                }
            }
        }
        return _classfileCache.get(className);
    }

    /**************************
     * Section 4. Information *
     **************************/

    /**
     * The helper class for class Information.
     */
    final static class Informations {
        final public static int MAGIC_NUMBER = 0xcafebabe;

        /**
         * Parse byte buffer to generate a {@code Classfile.Information} data structure.
         * 
         * @param className
         *        class name with namespace
         * @return class Information of the given class name
         */
        private static Information parse(final String className) {
            Information information = new Information();

            ByteBuffer byteBuffer = ByteBuffer.wrap(readBytes(className));

            // read byte by big-endian
            byteBuffer.order(ByteOrder.BIG_ENDIAN);

            // set class name
            information.className = className;

            // step 1: magic number
            final int magicNumber = byteBuffer.getInt();
            if (magicNumber != MAGIC_NUMBER) {
                throw new RuntimeException(
                        "File [" + convertToInnerRelativeFile(className) + "] is not a valid class.");
            }

            // step 2: class version
            information.minorVersion = byteBuffer.getChar();
            information.majorVersion = byteBuffer.getChar();
            // support jdk >= 1.0.2
            if (information.majorVersion < 45) {
                throw new RuntimeException("This class version is unsupported.");
            }

            // step 3: constant pool
            information.constantPoolCount = byteBuffer.getChar();
            information.constantPool = new ConstantPool[information.constantPoolCount];
            for (int i = 1; i < information.constantPoolCount; ++i) {
                information.constantPool[i] = ConstantPools.parse(byteBuffer);
                if (information.constantPool[i].getTag() == CONSTANTPOOL_TABLE.LONG
                        || information.constantPool[i].getTag() == CONSTANTPOOL_TABLE.DOUBLE) {
                    ++i;
                }
            }
            _currentConstantPoolWhenParsing = information.constantPool;

            // step 4: accessFlag
            information.accessFlags = byteBuffer.getChar();

            // step 5: class
            information.thisClass = byteBuffer.getChar();
            information.superClass = byteBuffer.getChar();

            // step 6: interface
            information.interfacesCount = byteBuffer.getChar();
            information.interfaces = new int[information.interfacesCount];
            for (int i = 0; i < information.interfacesCount; ++i) {
                information.interfaces[i] = byteBuffer.getChar();
            }

            // step 7: field
            information.fieldsCount = byteBuffer.getChar();
            information.fields = new Field[information.fieldsCount];
            for (int i = 0; i < information.fieldsCount; ++i) {
                information.fields[i] = Fields.parse(byteBuffer);
            }

            // step 8: method
            information.methodsCount = byteBuffer.getChar();
            information.methods = new Method[information.methodsCount];
            for (int i = 0; i < information.methodsCount; ++i) {
                information.methods[i] = Methods.parse(byteBuffer);
            }

            // step 9: attribute
            information.attributesCount = byteBuffer.getChar();
            information.attributes = new Attribute[information.attributesCount];
            for (int i = 0; i < information.attributesCount; ++i) {
                information.attributes[i] = Attributes.parse(byteBuffer);
            }

            return information;
        }
    }

    /**
     * The basic information record of a class
     */
    final public static class Information {
        private Information() {}

        private String className;

        @SuppressWarnings("unused")
        private int minorVersion;
        private int majorVersion;

        private int constantPoolCount;
        private ConstantPool[] constantPool;

        private int accessFlags;
        private int thisClass;
        private int superClass;

        private int interfacesCount;
        private int[] interfaces;

        private int fieldsCount;
        private Field[] fields;

        private int methodsCount;
        private Method[] methods;

        private int attributesCount;
        private Attribute[] attributes;

        public ConstantPool[] getConstantPool() {
            return constantPool;
        }

        public String getClassName() {
            return className;
        }

        public Field[] getFields() {
            return fields;
        }

        public Method[] getMethods() {
            return methods;
        }

        public String getThisClassName() {
            return constantPool[(int)constantPool[thisClass].value].value.toString();
        }

        public String getSuperClassName() {
            return constantPool[(int)constantPool[superClass].value].value.toString();
        }

        public List<String> getInterfaceNameList() {
            final List<String> result = new ArrayList<>();
            for (final int i : interfaces) {
                result.add(constantPool[(int)constantPool[i].value].value.toString());
            }
            return result;
        }

        public List<String> getMethodNameList() {
            final List<String> result = new ArrayList<>();
            for (final Method method : methods) {
                result.add(constantPool[method.nameIndex].value.toString());
            }
            return result;
        }

        public List<String> getFieldNameList() {
            final List<String> result = new ArrayList<>();
            for (final Field field : fields) {
                result.add(constantPool[field.nameIndex].value.toString());
            }
            return result;
        }

        public boolean isInterface() {
            return (accessFlags & ACCESS_CLASS_TABLE.ACC_INTERFACE) != 0;
        }

        public boolean isAbstract() {
            return (accessFlags & ACCESS_CLASS_TABLE.ACC_ABSTRACT) != 0;
        }

        public boolean isPublic() {
            return (accessFlags & ACCESS_CLASS_TABLE.ACC_PUBLIC) != 0;
        }

        public boolean isSuper() {
            return (accessFlags & ACCESS_CLASS_TABLE.ACC_SUPER) != 0;
        }

    }

    /****************************
     * Section 5. Constant Pool *
     ****************************/

    /**
     * The helper class for class ConstantPool.
     */
    final static class ConstantPools {
        /**
         * Get class ConstantPool from byte buffer.
         */
        private static ConstantPool parse(ByteBuffer byteBuffer) {
            ConstantPool constantPool = new ConstantPool();
            final byte tag = byteBuffer.get();
            switch (tag) {
                case CONSTANTPOOL_TABLE.UTF8:
                    constantPool.value = decodeString(byteBuffer);
                    break;
                case CONSTANTPOOL_TABLE.CLASS:
                case CONSTANTPOOL_TABLE.STRING:
                case CONSTANTPOOL_TABLE.METHOD_TYPE:
                case CONSTANTPOOL_TABLE.MODULE:
                case CONSTANTPOOL_TABLE.PACKAGE:
                    constantPool.value = (int)byteBuffer.getChar();
                    break;
                case CONSTANTPOOL_TABLE.FIELD_REF:
                case CONSTANTPOOL_TABLE.METHOD_REF:
                case CONSTANTPOOL_TABLE.INTERFACE_METHOD_REF:
                case CONSTANTPOOL_TABLE.NAME_AND_TYPE:
                    constantPool.value = new int[] { byteBuffer.getChar(), byteBuffer.getChar() };
                    break;
                case CONSTANTPOOL_TABLE.INTEGER:
                    constantPool.value = byteBuffer.getInt();
                    break;
                case CONSTANTPOOL_TABLE.FLOAT:
                    constantPool.value = byteBuffer.getFloat();
                    break;
                case CONSTANTPOOL_TABLE.DOUBLE:
                    constantPool.value = byteBuffer.getDouble();
                    break;
                case CONSTANTPOOL_TABLE.LONG:
                    constantPool.value = byteBuffer.getLong();
                    break;
                case CONSTANTPOOL_TABLE.METHOD_HANDLE:
                    constantPool.value = new int[] { byteBuffer.get(), byteBuffer.getChar() };
                    break;
                case CONSTANTPOOL_TABLE.INVOKE_DYNAMIC:
                    constantPool.value = new int[] { byteBuffer.getChar(), byteBuffer.getChar() };
                    break;
                default:
                    throw new RuntimeException("Unknown Constant Pool Type.");
            }

            constantPool.tag = tag;
            return constantPool;
        }

        /**
         * Extract the modify utf-8 from byte buffer, and convert it to utf-8.
         * 
         * @see https://stackoverflow.com/questions/32255023/how-would-i-go-about-parsing-the-java-class-file-constant-pool
         */
        public static String decodeString(ByteBuffer byteBuffer) {
            final int size = byteBuffer.getChar();
            final int oldLimit = byteBuffer.limit();
            byteBuffer.limit(byteBuffer.position() + size);
            final StringBuilder stringBuilder = new StringBuilder(size + (size >> 1));
            while (byteBuffer.hasRemaining()) {
                final byte b = byteBuffer.get();
                if (b > 0)
                    stringBuilder.append((char)b);
                else {
                    final int b2 = byteBuffer.get();
                    if ((b & 0xf0) != 0xe0)
                        stringBuilder.append((char)((b & 0x1F) << 6 | b2 & 0x3F));
                    else {
                        final int b3 = byteBuffer.get();
                        stringBuilder.append((char)((b & 0x0F) << 12 | (b2 & 0x3F) << 6 | b3 & 0x3F));
                    }
                }
            }
            byteBuffer.limit(oldLimit);
            return stringBuilder.toString();
        }
    }

    /**
     * Data struct of constant pool.
     */
    final public static class ConstantPool {
        private ConstantPool() {}

        /**
         * The value of tag can be the one in the class CONSTANTPOOL before
         */
        private byte tag;

        /**
         * Following the tag, type T will be a type below:
         * <ol>
         * <li>tag => {@code T}</li>
         * <li>utf8 => {@code String}</li>
         * <li>class/string/method type/module/package => {@code Integer}</li>
         * <li>field ref/method ref/interface method ref/name and type => {@code Map.Entry<Integer, Integer>}</li>
         * <li>integer => {@code Integer}</li>
         * <li>float => {@code Float}</li>
         * <li>double => {@code Double}</li>
         * <li>long => {@code Long}</li>
         * <li>method handle => {@code Map.Entry<Byte, Integer>}</li>
         * <li>invoke dynamic => {@code Map.Entry<Integer, Integer>}</li>
         * </ol>
         */
        private Object value;

        public byte getTag() {
            return tag;
        }

        public Object getValue() {
            return value;
        }
    }

    /********************
     * Section 6. Field *
     ********************/

    /**
     * The helper class for class Field.
     */
    final static class Fields {
        /**
         * Get class Field from byte buffer.
         */
        private static Field parse(ByteBuffer byteBuffer) {
            final Field field = new Field();
            field.accessFlags = byteBuffer.getChar();
            field.nameIndex = byteBuffer.getChar();
            field.descriptorIndex = byteBuffer.getChar();
            field.attributesCount = byteBuffer.getChar();
            field.attributes = new Attribute[field.attributesCount];
            for (int i = 0; i < field.attributesCount; ++i) {
                field.attributes[i] = Attributes.parse(byteBuffer);
            }
            return field;
        }
    }

    /**
     * Data struct of field.
     */
    final public static class Field {
        private Field() {}

        private int accessFlags;

        private int nameIndex;

        private int descriptorIndex;

        private int attributesCount;
        private Attribute[] attributes;

        public int getNameIndex() {
            return nameIndex;
        }

        public int getDescriptorIndex() {
            return descriptorIndex;
        }

        public Attribute[] getAttributes() {
            return attributes;
        }

        public boolean isStatic() {
            return (accessFlags & ACCESS_FIELD_TABLE.ACC_STATIC) != 0;
        }

        public boolean isFinal() {
            return (accessFlags & ACCESS_FIELD_TABLE.ACC_FINAL) != 0;
        }
    }

    /*********************
     * Section 7. Method *
     *********************/

    /**
     * The helper class for class Method.
     */
    final static class Methods {
        /**
         * Get class Method from byte buffer.
         */
        private static Method parse(ByteBuffer byteBuffer) {
            final Method method = new Method();
            method.accessFlags = byteBuffer.getChar();
            method.nameIndex = byteBuffer.getChar();
            method.descriptorIndex = byteBuffer.getChar();
            method.attributesCount = byteBuffer.getChar();
            method.attributes = new Attribute[method.attributesCount];
            for (int i = 0; i < method.attributesCount; ++i) {
                method.attributes[i] = Attributes.parse(byteBuffer);
            }
            return method;
        }
    }

    /**
     * Data struct of method.
     */
    final public static class Method {
        private Method() {}

        private int accessFlags;

        private int nameIndex;

        private int descriptorIndex;

        private int attributesCount;
        private Attribute[] attributes;

        public int getNameIndex() {
            return nameIndex;
        }

        public int getDescriptorIndex() {
            return descriptorIndex;
        }

        public Attribute[] getAttributes() {
            return attributes;
        }

        public boolean isStatic() {
            return (accessFlags & ACCESS_METHOD_TABLE.ACC_STATIC) != 0;
        }

        public boolean isFinal() {
            return (accessFlags & ACCESS_METHOD_TABLE.ACC_FINAL) != 0;
        }

        public boolean isAbstract() {
            return (accessFlags & ACCESS_METHOD_TABLE.ACC_ABSTRACT) != 0;
        }

        public boolean isNative() {
            return (accessFlags & ACCESS_METHOD_TABLE.ACC_NATIVE) != 0;
        }
    }

    /************************
     * Section 7. Attribute *
     ************************/

    /**
     * The helper class for class Attribute.
     */
    final static class Attributes {
        /**
         * Get class that extend AttributePredefined.Annotation from byte buffer.
         */
        private static AttributePredefined.Annotation parseAnnotation(ByteBuffer byteBuffer) {
            AttributePredefined.Annotation annotation = new AttributePredefined.Annotation();
            annotation.typeIndex = byteBuffer.getChar();
            annotation.numElementValuePairs = byteBuffer.getChar();
            annotation.elementValuePairs = new AttributePredefined.ElementValuePair[annotation.numElementValuePairs];
            for (int j = 0; j < annotation.numElementValuePairs; ++j) {
                annotation.elementValuePairs[j] = new AttributePredefined.ElementValuePair();
                annotation.elementValuePairs[j].elementNameIndex = byteBuffer.getChar();
                annotation.elementValuePairs[j].value = parseElementValue(byteBuffer);
            }

            return annotation;
        }

        /**
         * Get class that extend AttributePredefined.ElementValuePair.ElementValue from byte buffer.
         */
        private static AttributePredefined.ElementValuePair.ElementValue parseElementValue(ByteBuffer byteBuffer) {
            AttributePredefined.ElementValuePair.ElementValue elementValue;
            byte tag = byteBuffer.get();

            switch (tag) {
                case ATTRIBUTE_ELEMENT_VALUE_TAG.B:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.C:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.D:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.F:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.I:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.J:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.S:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.Z:
                case ATTRIBUTE_ELEMENT_VALUE_TAG.s:
                    elementValue = new AttributePredefined.ElementValuePair.ElementValueConstValueIndex();
                    ((AttributePredefined.ElementValuePair.ElementValueConstValueIndex)elementValue).constValueIndex = byteBuffer
                            .getChar();
                    break;
                case ATTRIBUTE_ELEMENT_VALUE_TAG.e:
                    elementValue = new AttributePredefined.ElementValuePair.ElementValueEnumConstValue();
                    ((AttributePredefined.ElementValuePair.ElementValueEnumConstValue)elementValue).enumConstValue = new int[] {
                            byteBuffer.getChar(), byteBuffer.getChar() };
                    break;
                case ATTRIBUTE_ELEMENT_VALUE_TAG.c:
                    elementValue = new AttributePredefined.ElementValuePair.ElementValueClassInfoIndex();
                    ((AttributePredefined.ElementValuePair.ElementValueClassInfoIndex)elementValue).classInfoIndex = byteBuffer
                            .getChar();
                    break;
                case ATTRIBUTE_ELEMENT_VALUE_TAG.ANNOTATION:
                    elementValue = new AttributePredefined.ElementValuePair.ElementValueAnnotationValue();
                    ((AttributePredefined.ElementValuePair.ElementValueAnnotationValue)elementValue).annotationValue = parseAnnotation(
                            byteBuffer);
                    break;
                case ATTRIBUTE_ELEMENT_VALUE_TAG.ARRAY:
                    AttributePredefined.ElementValuePair.ElementValueArrayValue elementValueArrayValue = new AttributePredefined.ElementValuePair.ElementValueArrayValue();

                    elementValueArrayValue.numValues = byteBuffer.getChar();
                    elementValueArrayValue.arrayValue = new AttributePredefined.ElementValuePair.ElementValue[elementValueArrayValue.numValues];
                    for (int i = 0; i < elementValueArrayValue.numValues; ++i) {
                        elementValueArrayValue.arrayValue[i] = parseElementValue(byteBuffer);
                    }

                    elementValue = elementValueArrayValue;
                    break;
                default:
                    throw new RuntimeException("Can not parse this attribute element value tag!");
            }

            elementValue.tag = tag;

            return elementValue;
        }

        /**
         * Get class that extend AttributePredefined.StackMapTable.VerificationTypeInfo from byte buffer.
         */
        private static AttributePredefined.StackMapTable.VerificationTypeInfo parseVerificationTypeInfo(
                ByteBuffer byteBuffer) {
            AttributePredefined.StackMapTable.VerificationTypeInfo verificationTypeInfo;
            byte sameLocals1StackItemFrameStackTag = byteBuffer.get();

            if (sameLocals1StackItemFrameStackTag == 0) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.TopVariableInfo();
            } else if (sameLocals1StackItemFrameStackTag == 1) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.IntegerVariableInfo();
            } else if (sameLocals1StackItemFrameStackTag == 2) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.FloatVariableInfo();
            } else if (sameLocals1StackItemFrameStackTag == 3) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.DoubleVariableInfo();
            } else if (sameLocals1StackItemFrameStackTag == 4) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.LongVariableInfo();
            } else if (sameLocals1StackItemFrameStackTag == 5) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.NullVariableInfo();
            } else if (sameLocals1StackItemFrameStackTag == 6) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.UninitializedVariableInfo();
            } else if (sameLocals1StackItemFrameStackTag == 7) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.ObjectVariableInfo();
                ((AttributePredefined.StackMapTable.ObjectVariableInfo)verificationTypeInfo).cpoolIndex = byteBuffer
                        .getChar();
            } else if (sameLocals1StackItemFrameStackTag == 8) {
                verificationTypeInfo = new AttributePredefined.StackMapTable.UninitializedVariableInfo();
                ((AttributePredefined.StackMapTable.UninitializedVariableInfo)verificationTypeInfo).offset = byteBuffer
                        .getChar();
            } else {
                throw new RuntimeException("Can not parse this verification type!");
            }

            verificationTypeInfo.tag = sameLocals1StackItemFrameStackTag;

            return verificationTypeInfo;
        }

        /**
         * Get class that extend Attribute from byte buffer.
         */
        private static Attribute parse(ByteBuffer byteBuffer) {
            Attribute attribute;
            int attributeNameIndex = byteBuffer.getChar();
            String attributeName = getConstantPoolStringWhenParsing(attributeNameIndex);
            int attributeLength = byteBuffer.getInt();

            switch (attributeName) {
                case ATTRIBUTE_TABLE.CONSTANT_VALUE:
                    attribute = new AttributePredefined.ConstantValue();
                    ((AttributePredefined.ConstantValue)attribute).constantValueIndex = byteBuffer.getChar();
                    break;

                case ATTRIBUTE_TABLE.CODE:
                    AttributePredefined.Code codeAttribute = new AttributePredefined.Code();

                    codeAttribute.maxStack = byteBuffer.getChar();
                    codeAttribute.maxLocals = byteBuffer.getChar();

                    codeAttribute.codeLength = byteBuffer.getInt();
                    codeAttribute.code = new byte[codeAttribute.codeLength];
                    byteBuffer.get(codeAttribute.code);

                    codeAttribute.exceptionTableLength = byteBuffer.getChar();
                    codeAttribute.exceptionTable = new int[codeAttribute.exceptionTableLength][4];
                    for (int i = 0; i < codeAttribute.exceptionTableLength; ++i) {
                        for (int j = 0; j < 4; ++j) {
                            codeAttribute.exceptionTable[i][j] = byteBuffer.getChar();
                        }
                    }

                    codeAttribute.attributesCount = byteBuffer.getChar();
                    codeAttribute.attributes = new Attribute[codeAttribute.attributesCount];
                    for (int i = 0; i < codeAttribute.attributesCount; i++) {
                        codeAttribute.attributes[i] = parse(byteBuffer);
                    }

                    attribute = codeAttribute;
                    break;

                case ATTRIBUTE_TABLE.STACK_MAP_TABLE:
                    AttributePredefined.StackMapTable stackMapTableAttribute = new AttributePredefined.StackMapTable();

                    stackMapTableAttribute.numberOfEntries = byteBuffer.getChar();
                    stackMapTableAttribute.entries = new AttributePredefined.StackMapTable.StackMapFrame[stackMapTableAttribute.numberOfEntries];

                    for (int i = 0; i < stackMapTableAttribute.numberOfEntries; ++i) {
                        byte frameType = byteBuffer.get();
                        int frameTypeValue = frameType & 0xff;

                        if (frameTypeValue >= 0 && frameTypeValue <= 63) {
                            stackMapTableAttribute.entries[i] = new AttributePredefined.StackMapTable.SameFrame();

                        } else if (frameTypeValue >= 64 && frameTypeValue <= 127) {
                            AttributePredefined.StackMapTable.SameLocals1StackItemFrame sameLocals1StackItemFrame = new AttributePredefined.StackMapTable.SameLocals1StackItemFrame();
                            sameLocals1StackItemFrame.stack = new AttributePredefined.StackMapTable.VerificationTypeInfo[1];
                            sameLocals1StackItemFrame.stack[0] = parseVerificationTypeInfo(byteBuffer);
                            stackMapTableAttribute.entries[i] = sameLocals1StackItemFrame;
                        } else if (frameTypeValue == 247) {
                            AttributePredefined.StackMapTable.SameLocals1StackItemFrameExtended sameLocals1StackItemFrameExtended = new AttributePredefined.StackMapTable.SameLocals1StackItemFrameExtended();
                            sameLocals1StackItemFrameExtended.offsetDelta = byteBuffer.getChar();
                            sameLocals1StackItemFrameExtended.stack = new AttributePredefined.StackMapTable.VerificationTypeInfo[1];
                            sameLocals1StackItemFrameExtended.stack[0] = parseVerificationTypeInfo(byteBuffer);
                            stackMapTableAttribute.entries[i] = sameLocals1StackItemFrameExtended;
                        } else if (frameTypeValue >= 248 && frameTypeValue <= 250) {
                            stackMapTableAttribute.entries[i] = new AttributePredefined.StackMapTable.ChopFrame();
                            ((AttributePredefined.StackMapTable.ChopFrame)stackMapTableAttribute.entries[i]).offsetDelta = byteBuffer
                                    .getChar();
                        } else if (frameTypeValue == 251) {
                            stackMapTableAttribute.entries[i] = new AttributePredefined.StackMapTable.SameFrameExtended();
                            ((AttributePredefined.StackMapTable.SameFrameExtended)stackMapTableAttribute.entries[i]).offsetDelta = byteBuffer
                                    .getChar();
                        } else if (frameTypeValue >= 252 && frameTypeValue <= 254) {
                            AttributePredefined.StackMapTable.AppendFrame appendFrame = new AttributePredefined.StackMapTable.AppendFrame();
                            appendFrame.offsetDelta = byteBuffer.getChar();
                            appendFrame.locals = new AttributePredefined.StackMapTable.VerificationTypeInfo[frameTypeValue
                                    - 251];
                            for (int j = 0; j < frameTypeValue - 251; ++j) {
                                appendFrame.locals[j] = parseVerificationTypeInfo(byteBuffer);
                            }

                            stackMapTableAttribute.entries[i] = appendFrame;
                        } else if (frameTypeValue == 255) {
                            AttributePredefined.StackMapTable.FullFrame fullFrame = new AttributePredefined.StackMapTable.FullFrame();
                            fullFrame.offsetDelta = byteBuffer.getChar();

                            fullFrame.numberOfLocals = byteBuffer.getChar();
                            fullFrame.locals = new AttributePredefined.StackMapTable.VerificationTypeInfo[fullFrame.numberOfLocals];
                            for (int j = 0; j < fullFrame.numberOfLocals; ++j) {
                                fullFrame.locals[j] = parseVerificationTypeInfo(byteBuffer);
                            }

                            fullFrame.numberOfStackItems = byteBuffer.getChar();
                            fullFrame.stack = new AttributePredefined.StackMapTable.VerificationTypeInfo[fullFrame.numberOfStackItems];
                            for (int j = 0; j < fullFrame.numberOfStackItems; ++j) {
                                fullFrame.stack[j] = parseVerificationTypeInfo(byteBuffer);
                            }

                            stackMapTableAttribute.entries[i] = fullFrame;
                        } else {
                            throw new RuntimeException("Can not parse this attribute frame type!");
                        }

                        stackMapTableAttribute.entries[i].frameType = frameType;
                    }

                    attribute = stackMapTableAttribute;
                    break;

                case ATTRIBUTE_TABLE.BOOTSTRAP_METHODS:
                    AttributePredefined.BootstrapMethods bootstrapMethods = new AttributePredefined.BootstrapMethods();

                    bootstrapMethods.numBoostrapMethods = byteBuffer.getChar();
                    bootstrapMethods.bootstrapMethods = new AttributePredefined.BootstrapMethods.InnerBootstrapMethods[bootstrapMethods.numBoostrapMethods];
                    for (int i = 0; i < bootstrapMethods.numBoostrapMethods; ++i) {
                        bootstrapMethods.bootstrapMethods[i] = new AttributePredefined.BootstrapMethods.InnerBootstrapMethods();
                        bootstrapMethods.bootstrapMethods[i].bootstrapMethodRef = byteBuffer.getChar();
                        bootstrapMethods.bootstrapMethods[i].numBootstrapArguments = byteBuffer.getChar();
                        bootstrapMethods.bootstrapMethods[i].bootstrapArguments = new int[bootstrapMethods.bootstrapMethods[i].numBootstrapArguments];
                        for (int j = 0; j < bootstrapMethods.bootstrapMethods[i].numBootstrapArguments; ++j) {
                            bootstrapMethods.bootstrapMethods[i].bootstrapArguments[j] = byteBuffer.getChar();
                        }
                    }

                    attribute = bootstrapMethods;
                    break;

                case ATTRIBUTE_TABLE.NEST_HOST:
                    attribute = new AttributePredefined.NestHost();
                    ((AttributePredefined.NestHost)attribute).hostClassIndex = byteBuffer.getChar();
                    break;

                case ATTRIBUTE_TABLE.NEST_MEMBERS:
                    AttributePredefined.NestMembers nestMembers = new AttributePredefined.NestMembers();

                    nestMembers.numberOfClasses = byteBuffer.getChar();
                    nestMembers.classes = new int[nestMembers.numberOfClasses];
                    for (int i = 0; i < nestMembers.numberOfClasses; ++i) {
                        nestMembers.classes[i] = byteBuffer.getChar();
                    }

                    attribute = nestMembers;
                    break;

                case ATTRIBUTE_TABLE.EXCEPTIONS:
                    AttributePredefined.Exceptions exceptions = new AttributePredefined.Exceptions();

                    exceptions.numberOfExceptions = byteBuffer.getChar();
                    exceptions.exceptionIndexTable = new int[exceptions.numberOfExceptions];
                    for (int i = 0; i < exceptions.numberOfExceptions; ++i) {
                        exceptions.exceptionIndexTable[i] = byteBuffer.getChar();
                    }

                    attribute = exceptions;
                    break;

                case ATTRIBUTE_TABLE.INNER_CLASSES:
                    AttributePredefined.InnerClasses innerClasses = new AttributePredefined.InnerClasses();

                    innerClasses.numberOfClasses = byteBuffer.getChar();
                    innerClasses.classes = new int[innerClasses.numberOfClasses][4];
                    for (int i = 0; i < innerClasses.numberOfClasses; ++i) {
                        for (int j = 0; j < 4; ++j) {
                            innerClasses.classes[i][j] = byteBuffer.getChar();
                        }
                    }

                    attribute = innerClasses;
                    break;

                case ATTRIBUTE_TABLE.ENCLOSING_METHOD:
                    AttributePredefined.EnclosingMethod enclosingMethod = new AttributePredefined.EnclosingMethod();

                    enclosingMethod.classIndex = byteBuffer.getChar();
                    enclosingMethod.methodIndex = byteBuffer.getChar();

                    attribute = enclosingMethod;
                    break;

                case ATTRIBUTE_TABLE.SYNTHETIC:
                    attribute = new AttributePredefined.Synthetic();
                    break;

                case ATTRIBUTE_TABLE.SIGNATURE:
                    attribute = new AttributePredefined.Signature();
                    ((AttributePredefined.Signature)attribute).signatureIndex = byteBuffer.getChar();
                    break;

                case ATTRIBUTE_TABLE.SOURCE_FILE:
                    attribute = new AttributePredefined.SourceFile();
                    ((AttributePredefined.SourceFile)attribute).sourceFileIndex = byteBuffer.getChar();
                    break;

                case ATTRIBUTE_TABLE.LINE_NUMBER_TABLE:
                    AttributePredefined.LineNumberTable lineNumberTable = new AttributePredefined.LineNumberTable();

                    lineNumberTable.lineNumberTableLength = byteBuffer.getChar();
                    lineNumberTable.lineNumberTable = new int[lineNumberTable.lineNumberTableLength][2];
                    for (int i = 0; i < lineNumberTable.lineNumberTableLength; ++i) {
                        lineNumberTable.lineNumberTable[i][0] = byteBuffer.getChar();
                        lineNumberTable.lineNumberTable[i][1] = byteBuffer.getChar();
                    }

                    attribute = lineNumberTable;
                    break;

                case ATTRIBUTE_TABLE.LOCAL_VARIABLE_TABLE:
                    AttributePredefined.LocalVariableTable localVariableTable = new AttributePredefined.LocalVariableTable();

                    localVariableTable.localVariableTableLength = byteBuffer.getChar();
                    localVariableTable.localVariableTable = new int[localVariableTable.localVariableTableLength][5];
                    for (int i = 0; i < localVariableTable.localVariableTableLength; ++i) {
                        for (int j = 0; j < 5; ++j) {
                            localVariableTable.localVariableTable[i][j] = byteBuffer.getChar();
                        }
                    }

                    attribute = localVariableTable;
                    break;

                case ATTRIBUTE_TABLE.LOCAL_VARIABLE_TYPE_TABLE:
                    AttributePredefined.LocalVariableTypeTable localVariableTypeTable = new AttributePredefined.LocalVariableTypeTable();

                    localVariableTypeTable.localVariableTypeTableLength = byteBuffer.getChar();
                    localVariableTypeTable.localVariableTypeTable = new int[localVariableTypeTable.localVariableTypeTableLength][5];
                    for (int i = 0; i < localVariableTypeTable.localVariableTypeTableLength; ++i) {
                        for (int j = 0; j < 5; ++j) {
                            localVariableTypeTable.localVariableTypeTable[i][j] = byteBuffer.getChar();
                        }
                    }

                    attribute = localVariableTypeTable;
                    break;

                case ATTRIBUTE_TABLE.SOURCE_DEBUG_EXTENSION:
                    AttributePredefined.SourceDebugExtension sourceDebugExtension = new AttributePredefined.SourceDebugExtension();
                    sourceDebugExtension.debugExtension = new byte[attributeLength];
                    byteBuffer.get(sourceDebugExtension.debugExtension);
                    attribute = sourceDebugExtension;
                    break;

                case ATTRIBUTE_TABLE.DEPRECATED:
                    attribute = new AttributePredefined.Deprecated();
                    break;

                case ATTRIBUTE_TABLE.RUNTIME_VISIBLE_ANNOTATIONS:
                    AttributePredefined.RuntimeVisibleAnnotations runtimeVisibleAnnotations = new AttributePredefined.RuntimeVisibleAnnotations();

                    runtimeVisibleAnnotations.numAnnotations = byteBuffer.getChar();
                    runtimeVisibleAnnotations.annotations = new AttributePredefined.Annotation[runtimeVisibleAnnotations.numAnnotations];
                    for (int i = 0; i < runtimeVisibleAnnotations.numAnnotations; ++i) {
                        runtimeVisibleAnnotations.annotations[i] = parseAnnotation(byteBuffer);
                    }

                    attribute = runtimeVisibleAnnotations;
                    break;

                case ATTRIBUTE_TABLE.RUNTIME_INVISIBLE_ANNOTATIONS:
                    AttributePredefined.RuntimeInvisibleAnnotations runtimeInvisibleAnnotations = new AttributePredefined.RuntimeInvisibleAnnotations();

                    runtimeInvisibleAnnotations.numAnnotations = byteBuffer.getChar();
                    runtimeInvisibleAnnotations.annotations = new AttributePredefined.Annotation[runtimeInvisibleAnnotations.numAnnotations];
                    for (int i = 0; i < runtimeInvisibleAnnotations.numAnnotations; ++i) {
                        runtimeInvisibleAnnotations.annotations[i] = parseAnnotation(byteBuffer);
                    }

                    attribute = runtimeInvisibleAnnotations;
                    break;

                case ATTRIBUTE_TABLE.RUNTIME_VISIBLE_TYPE_ANNOTATIONS:
                case ATTRIBUTE_TABLE.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:
                    // @todo: I don't have a class with these attribute
                    throw new RuntimeException("Runtime type annotation has not been supported!");

                case ATTRIBUTE_TABLE.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
                    AttributePredefined.RuntimeVisibleParameterAnnotations runtimeVisibleParameterAnnotations = new AttributePredefined.RuntimeVisibleParameterAnnotations();

                    runtimeVisibleParameterAnnotations.numParameters = byteBuffer.get();
                    runtimeVisibleParameterAnnotations.parameterAnnotations = new AttributePredefined.ParameterAnnotation[runtimeVisibleParameterAnnotations.numParameters];
                    for (int i = 0; i < runtimeVisibleParameterAnnotations.numParameters; ++i) {
                        AttributePredefined.ParameterAnnotation parameterAnnotation = new AttributePredefined.ParameterAnnotation();
                        parameterAnnotation.numAnnotations = byteBuffer.getChar();
                        parameterAnnotation.annotations = new AttributePredefined.Annotation[parameterAnnotation.numAnnotations];
                        for (int j = 0; j < parameterAnnotation.numAnnotations; ++j) {
                            parameterAnnotation.annotations[j] = parseAnnotation(byteBuffer);
                        }
                        runtimeVisibleParameterAnnotations.parameterAnnotations[i] = parameterAnnotation;
                    }

                    attribute = runtimeVisibleParameterAnnotations;
                    break;

                case ATTRIBUTE_TABLE.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
                    AttributePredefined.RuntimeInvisibleParameterAnnotations runtimeInvisibleParameterAnnotations = new AttributePredefined.RuntimeInvisibleParameterAnnotations();

                    runtimeInvisibleParameterAnnotations.numParameters = byteBuffer.get();
                    runtimeInvisibleParameterAnnotations.parameterAnnotations = new AttributePredefined.ParameterAnnotation[runtimeInvisibleParameterAnnotations.numParameters];
                    for (int i = 0; i < runtimeInvisibleParameterAnnotations.numParameters; ++i) {
                        AttributePredefined.ParameterAnnotation parameterAnnotation = new AttributePredefined.ParameterAnnotation();
                        parameterAnnotation.numAnnotations = byteBuffer.getChar();
                        parameterAnnotation.annotations = new AttributePredefined.Annotation[parameterAnnotation.numAnnotations];
                        for (int j = 0; j < parameterAnnotation.numAnnotations; ++j) {
                            parameterAnnotation.annotations[j] = parseAnnotation(byteBuffer);
                        }
                        runtimeInvisibleParameterAnnotations.parameterAnnotations[i] = parameterAnnotation;
                    }

                    attribute = runtimeInvisibleParameterAnnotations;
                    break;

                case ATTRIBUTE_TABLE.ANNOTATION_DEFAULT:
                    attribute = new AttributePredefined.AnnotationDefault();
                    ((AttributePredefined.AnnotationDefault)attribute).defaultValue = parseElementValue(byteBuffer);
                    break;

                case ATTRIBUTE_TABLE.METHOD_PARAMETERS:
                    AttributePredefined.MethodParameters methodParameters = new AttributePredefined.MethodParameters();

                    methodParameters.parametersCount = byteBuffer.get();
                    methodParameters.parameters = new int[methodParameters.parametersCount][2];
                    for (int i = 0; i < methodParameters.parametersCount; ++i) {
                        methodParameters.parameters[i][0] = byteBuffer.getChar();
                        methodParameters.parameters[i][1] = byteBuffer.getChar();
                    }

                    attribute = methodParameters;
                    break;

                case ATTRIBUTE_TABLE.MODULE:
                case ATTRIBUTE_TABLE.MODULE_PACKAGES:
                case ATTRIBUTE_TABLE.MODULE_MAIN_CLASS:
                    // @todo: I don't have a class with these attribute
                    throw new RuntimeException("Module has not been supported!");

                default:
                    // pass if the attribute we can not identify
                    attribute = new Attribute() {};
                    byteBuffer.position(byteBuffer.position() + attributeLength);
            }

            attribute.attributeName = attributeName;
            attribute.attributeNameIndex = attributeNameIndex;
            attribute.attributeLength = attributeLength;

            return attribute;
        }
    }

    /**
     * Data struct of attribute
     */
    abstract public static class Attribute {
        private int attributeNameIndex;

        private String attributeName;

        private int attributeLength;

        public int getAttributeNameIndex() {
            return attributeNameIndex;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public int getAttributeLength() {
            return attributeLength;
        }
    }

    /**
     * These class include the attribute that was had predefined in the spec.
     * <p>
     * Now, it has 23 jvm dependency attributes:
     * <p>
     * {@code ConstantValue}, {@code Code}, {@code StackMapTable}, {@code BootstrapMethods}, {@code NestHost},
     * {@code NestMembers}, {@code Exceptions}, {@code InnerClasses}, {@code EnclosingMethod}, {@code Synthetic},
     * {@code Signature}, {@code SourceFile}, {@code LineNumberTable}, {@code LocalVariableTable},
     * {@code LocalVariableTypeTable}, {@code SourceDebugExtension}, {@code Deprecated},
     * {@code RuntimeVisibleAnnotations}, {@code RuntimeInvisibleAnnotations},
     * {@code RuntimeVisibleParameterAnnotations}, {@code RuntimeInvisibleParameterAnnotations},
     * {@code RuntimeVisibleTypeAnnotations}, {@code RuntimeInvisibleTypeAnnotations}, {@code AnnotationDefault},
     * {@code MethodParameters}, {@code Module}, {@code ModulePackages}, {@code ModuleMainClass}
     * <p>
     * 
     * And 4 common attributes:
     * <p>
     * {@code  TypeAnnotation}, {@code ParameterAnnotation}, {@code Annotation}, {@code ElementValuePair}
     */
    final public static class AttributePredefined {
        private AttributePredefined() {}

        final public static class ConstantValue extends Attribute {
            private int constantValueIndex;

            public int getConstantValueIndex() {
                return constantValueIndex;
            }
        }

        final public static class Code extends Attribute {

            private int maxStack;
            private int maxLocals;

            private int codeLength;
            private byte[] code;

            /**
             * {@code int[][4]}:
             * <ul>
             * <li>0 => {@code startPc}</li>
             * <li>1 => {@code endPc}</li>
             * <li>2 => {@code handlerPc}</li>
             * <li>3 => {@code catchType}</li>
             * </ul>
             */
            private int exceptionTableLength;
            private int[][] exceptionTable;

            private int attributesCount;
            private Attribute[] attributes;

            public int getMaxStack() {
                return maxStack;
            }

            public int getMaxLocals() {
                return maxLocals;
            }

            public byte[] getCode() {
                return code;
            }

            public int getExceptionTableLength() {
                return exceptionTableLength;
            }

            public int[][] getExceptionTable() {
                return exceptionTable;
            }

            public int getCodeLength() {
                return codeLength;
            }

            public int getAttributesCount() {
                return attributesCount;
            }

            public Attribute[] getAttributes() {
                return attributes;
            }

        }

        final public static class StackMapTable extends Attribute {
            private int numberOfEntries;

            private StackMapFrame[] entries;

            public int getNumberOfEntries() {
                return numberOfEntries;
            }

            public StackMapFrame[] getEntries() {
                return entries;
            }

            abstract public static class StackMapFrame {
                private byte frameType;

                public byte getFrameType() {
                    return frameType;
                }
            }

            final public static class SameFrame extends StackMapFrame {}

            final public static class SameLocals1StackItemFrame extends StackMapFrame {
                private VerificationTypeInfo[] stack;

                public VerificationTypeInfo[] getStack() {
                    return stack;
                }
            }

            final public static class SameLocals1StackItemFrameExtended extends StackMapFrame {
                private int offsetDelta;
                private VerificationTypeInfo[] stack;

                public int getOffsetDelta() {
                    return offsetDelta;
                }

                public VerificationTypeInfo[] getStack() {
                    return stack;
                }
            }

            final public static class ChopFrame extends StackMapFrame {
                private int offsetDelta;

                public int getOffsetDelta() {
                    return offsetDelta;
                }
            }

            final public static class SameFrameExtended extends StackMapFrame {
                private int offsetDelta;

                public int getOffsetDelta() {
                    return offsetDelta;
                }
            }

            final public static class AppendFrame extends StackMapFrame {
                private int offsetDelta;

                private VerificationTypeInfo[] locals;

                public int getOffsetDelta() {
                    return offsetDelta;
                }

                public VerificationTypeInfo[] getLocals() {
                    return locals;
                }
            }

            final public static class FullFrame extends StackMapFrame {
                private int offsetDelta;

                private int numberOfLocals;
                private VerificationTypeInfo[] locals;

                private int numberOfStackItems;
                private VerificationTypeInfo[] stack;

                public int getOffsetDelta() {
                    return offsetDelta;
                }

                public int getNumberOfLocals() {
                    return numberOfLocals;
                }

                public VerificationTypeInfo[] getLocals() {
                    return locals;
                }

                public int getNumberOfStackItems() {
                    return numberOfStackItems;
                }

                public VerificationTypeInfo[] getStack() {
                    return stack;
                }
            }

            abstract public static class VerificationTypeInfo {
                private byte tag;

                public byte getTag() {
                    return tag;
                }
            }

            final public static class TopVariableInfo extends VerificationTypeInfo {}

            final public static class IntegerVariableInfo extends VerificationTypeInfo {}

            final public static class FloatVariableInfo extends VerificationTypeInfo {}

            final public static class NullVariableInfo extends VerificationTypeInfo {}

            final public static class UninitializedThisVariableInfo extends VerificationTypeInfo {}

            final public static class ObjectVariableInfo extends VerificationTypeInfo {
                private int cpoolIndex;

                public int getCpoolIndex() {
                    return cpoolIndex;
                }
            }

            final public static class UninitializedVariableInfo extends VerificationTypeInfo {
                private int offset;

                public int getOffset() {
                    return offset;
                }
            }

            final public static class LongVariableInfo extends VerificationTypeInfo {}

            final public static class DoubleVariableInfo extends VerificationTypeInfo {}
        }

        final public static class BootstrapMethods extends Attribute {
            private int numBoostrapMethods;
            private InnerBootstrapMethods[] bootstrapMethods;

            public int getNumBoostrapMethods() {
                return numBoostrapMethods;
            }

            public InnerBootstrapMethods[] getBootstrapMethods() {
                return bootstrapMethods;
            }

            final static class InnerBootstrapMethods {
                private int bootstrapMethodRef;

                private int numBootstrapArguments;
                private int[] bootstrapArguments;

                public int getBootstrapMethodRef() {
                    return bootstrapMethodRef;
                }

                public int getNumBootstrapArguments() {
                    return numBootstrapArguments;
                }

                public int[] getBootstrapArguments() {
                    return bootstrapArguments;
                }
            }
        }

        final public static class NestHost extends Attribute {
            private int hostClassIndex;

            public int getHostClassIndex() {
                return hostClassIndex;
            }
        }

        final public static class NestMembers extends Attribute {
            private int numberOfClasses;
            private int[] classes;

            public int getNumberOfClasses() {
                return numberOfClasses;
            }

            public int[] getClasses() {
                return classes;
            }
        }

        final public static class Exceptions extends Attribute {
            private int numberOfExceptions;
            private int[] exceptionIndexTable;

            public int getNumberOfExceptions() {
                return numberOfExceptions;
            }

            public int[] getExceptionIndexTable() {
                return exceptionIndexTable;
            }
        }

        final public static class InnerClasses extends Attribute {
            /**
             * {@code int[][4]}:
             * <ul>
             * <li>0 => {@code inner_class_info_index}</li>
             * <li>1 => {@code outer_class_info_index}</li>
             * <li>2 => {@code inner_name_index}</li>
             * <li>3 => {@code inner_class_access_flags}</li>
             * </ul>
             */
            private int numberOfClasses;
            private int[][] classes;

            public int getNumberOfClasses() {
                return numberOfClasses;
            }

            public int[][] getClasses() {
                return classes;
            }
        }

        final public static class EnclosingMethod extends Attribute {
            private int classIndex;

            private int methodIndex;

            public int getClassIndex() {
                return classIndex;
            }

            public int getMethodIndex() {
                return methodIndex;
            }
        }

        final public static class Synthetic extends Attribute {}

        final public static class Signature extends Attribute {
            private int signatureIndex;

            public int getSignatureIndex() {
                return signatureIndex;
            }
        }

        final public static class SourceFile extends Attribute {
            private int sourceFileIndex;

            public int getSourceFileIndex() {
                return sourceFileIndex;
            }
        }

        final public static class LineNumberTable extends Attribute {
            /**
             * {@code int[][2]}:
             * <ul>
             * <li>0 => {@code start_pc}</li>
             * <li>1 => {@code line_number}</li>
             * </ul>
             */
            private int lineNumberTableLength;
            private int[][] lineNumberTable;

            public int getLineNumberTableLength() {
                return lineNumberTableLength;
            }

            public int[][] getLineNumberTable() {
                return lineNumberTable;
            }
        }

        final public static class LocalVariableTable extends Attribute {
            /**
             * {@code int[][5]}:
             * <ul>
             * <li>0 => {@code start_pc}</li>
             * <li>1 => {@code length}</li>
             * <li>2 => {@code name_index}</li>
             * <li>3 => {@code descriptor_index}</li>
             * <li>4 => index</li>
             * </ul>
             */
            private int localVariableTableLength;
            private int[][] localVariableTable;

            public int getLocalVariableTableLength() {
                return localVariableTableLength;
            }

            public int[][] getLocalVariableTable() {
                return localVariableTable;
            }
        }

        final public static class LocalVariableTypeTable extends Attribute {
            /**
             * {@code int[][5]}:
             * <ul>
             * <li>0 => {@code start_pc}</li>
             * <li>1 => {@code length}</li>
             * <li>2 => {@code name_index}</li>
             * <li>3 => {@code signature_index}</li>
             * <li>4 => {@code index}</li>
             * </pre>
             */
            private int localVariableTypeTableLength;
            private int[][] localVariableTypeTable;

            public int getLocalVariableTypeTableLength() {
                return localVariableTypeTableLength;
            }

            public int[][] getLocalVariableTypeTable() {
                return localVariableTypeTable;
            }
        }

        final public static class SourceDebugExtension extends Attribute {
            private byte[] debugExtension;

            public byte[] getDebugExtension() {
                return debugExtension;
            }
        }

        final public static class Deprecated extends Attribute {}

        final public static class RuntimeVisibleAnnotations extends Attribute {
            private int numAnnotations;
            private Annotation[] annotations;

            public int getNumAnnotations() {
                return numAnnotations;
            }

            public Annotation[] getAnnotations() {
                return annotations;
            }
        }

        final public static class RuntimeInvisibleAnnotations extends Attribute {
            private int numAnnotations;
            private Annotation[] annotations;

            public int getNumAnnotations() {
                return numAnnotations;
            }

            public Annotation[] getAnnotations() {
                return annotations;
            }
        }

        final public static class RuntimeVisibleParameterAnnotations extends Attribute {
            private byte numParameters;
            private ParameterAnnotation[] parameterAnnotations;

            public byte getNumParameters() {
                return numParameters;
            }

            public ParameterAnnotation[] getParameterAnnotations() {
                return parameterAnnotations;
            }
        }

        final public static class RuntimeInvisibleParameterAnnotations extends Attribute {
            private byte numParameters;
            private ParameterAnnotation[] parameterAnnotations;

            public byte getNumParameters() {
                return numParameters;
            }

            public ParameterAnnotation[] getParameterAnnotations() {
                return parameterAnnotations;
            }
        }

        final public static class RuntimeVisibleTypeAnnotations extends Attribute {
            private int numAnnotations;
            private TypeAnnotation[] annotations;

            public int getNumAnnotations() {
                return numAnnotations;
            }

            public TypeAnnotation[] getAnnotations() {
                return annotations;
            }
        }

        final public static class RuntimeInvisibleTypeAnnotations extends Attribute {
            private int numAnnotations;
            private TypeAnnotation[] annotations;

            public int getNumAnnotations() {
                return numAnnotations;
            }

            public TypeAnnotation[] getAnnotations() {
                return annotations;
            }
        }

        final public static class AnnotationDefault extends Attribute {
            private ElementValuePair.ElementValue defaultValue;

            public ElementValuePair.ElementValue getDefaultValue() {
                return defaultValue;
            }
        }

        final public static class MethodParameters extends Attribute {
            /**
             * {@code int[][2]}:
             * <ul>
             * <Li>0 => {@code name_index}</li>
             * <li>1 => {@code access_flags}</li>
             * </ul>
             */
            private byte parametersCount;
            private int[][] parameters;

            public byte getParametersCount() {
                return parametersCount;
            }

            public int[][] getParameters() {
                return parameters;
            }
        }

        final public static class Module extends Attribute {
            private int moduleNameIndex;

            private int moduleFlags;

            private int moduleVersionIndex;

            /**
             * {@code int[][3]}:
             * <ul>
             * <li>0 => {@code requires_index}</li>
             * <li>1 => {@code requires_flags}</li>
             * <li>2 => {@code requires_version_index}</li>
             * </ul>
             */
            private int requiresCount;
            private int[][] requires;

            private int exportsCount;
            private Exports[] exports;

            private int opensCount;
            private Opens[] opens;

            private int usesCount;
            private int[] usesIndex;

            private int providesCount;
            private Provides[] provides;

            public int getModuleNameIndex() {
                return moduleNameIndex;
            }

            public int getModuleFlags() {
                return moduleFlags;
            }

            public int getModuleVersionIndex() {
                return moduleVersionIndex;
            }

            public int getRequiresCount() {
                return requiresCount;
            }

            public int[][] getRequires() {
                return requires;
            }

            public int getExportsCount() {
                return exportsCount;
            }

            public Exports[] getExports() {
                return exports;
            }

            public int getOpensCount() {
                return opensCount;
            }

            public Opens[] getOpens() {
                return opens;
            }

            public int getUsesCount() {
                return usesCount;
            }

            public int[] getUsesIndex() {
                return usesIndex;
            }

            public int getProvidesCount() {
                return providesCount;
            }

            public Provides[] getProvides() {
                return provides;
            }

            final public static class Exports {
                private int exportsIndex;

                private int exportsFlags;

                private int exportsToCount;
                private int[] exportsToIndex;

                public int getExportsIndex() {
                    return exportsIndex;
                }

                public int getExportsFlags() {
                    return exportsFlags;
                }

                public int getExportsToCount() {
                    return exportsToCount;
                }

                public int[] getExportsToIndex() {
                    return exportsToIndex;
                }
            }

            final public static class Opens {
                private int opensIndex;

                private int opensFlags;

                private int opensToCount;
                private int[] opensToIndex;

                public int getOpensIndex() {
                    return opensIndex;
                }

                public int getOpensFlags() {
                    return opensFlags;
                }

                public int getOpensToCount() {
                    return opensToCount;
                }

                public int[] getOpensToIndex() {
                    return opensToIndex;
                }
            }

            final public static class Provides {
                private int providesIndex;

                private int providesWithCount;
                private int[] providesWithIndex;

                public int getProvidesIndex() {
                    return providesIndex;
                }

                public int getProvidesWithCount() {
                    return providesWithCount;
                }

                public int[] getProvidesWithIndex() {
                    return providesWithIndex;
                }
            }
        }

        final public static class ModulePackages extends Attribute {
            private int packageCount;
            private int[] packageIndex;

            public int getPackageCount() {
                return packageCount;
            }

            public int[] getPackageIndex() {
                return packageIndex;
            }
        }

        final public static class ModuleMainClass extends Attribute {
            private int mainClassIndex;

            public int getMainClassIndex() {
                return mainClassIndex;
            }
        }

        final public static class TypeAnnotation {
            private byte targetType;

            private TargetInfo targetInfo;

            private TypePath targetPath;

            private int typeIndex;

            private int numElementValuePairs;
            private ElementValuePair[] elementValuePairs;

            public byte getTargetType() {
                return targetType;
            }

            public TargetInfo getTargetInfo() {
                return targetInfo;
            }

            public TypePath getTargetPath() {
                return targetPath;
            }

            public int getTypeIndex() {
                return typeIndex;
            }

            public int getNumElementValuePairs() {
                return numElementValuePairs;
            }

            public ElementValuePair[] getElementValuePairs() {
                return elementValuePairs;
            }

            public static interface TargetInfo {}

            final public static class TypeParameterTarget implements TargetInfo {
                private byte typeParameterIndex;

                public byte getTypeParameterIndex() {
                    return typeParameterIndex;
                }
            }

            final public static class SuperTypeTarget implements TargetInfo {
                private int superTypeIndex;

                public int getSuperTypeIndex() {
                    return superTypeIndex;
                }
            }

            final public static class TypeParameterBoundTarget implements TargetInfo {
                private byte typeParameterIndex;

                private byte boundIndex;

                public byte getTypeParameterIndex() {
                    return typeParameterIndex;
                }

                public byte getBoundIndex() {
                    return boundIndex;
                }
            }

            final public static class EmptyTarget {}

            final public static class FormalParameterTarget {
                private byte formalParameterIndex;

                public byte getFormalParameterIndex() {
                    return formalParameterIndex;
                }
            }

            final public static class ThrowsTarget {
                private int throwsTypeIndex;

                public int getThrowsTypeIndex() {
                    return throwsTypeIndex;
                }
            }

            final public static class LocalVarTarget {
                private int tableLength;
                private int[][] table;

                public int getTableLength() {
                    return tableLength;
                }

                public int[][] getTable() {
                    return table;
                }
            }

            final public static class CatchTarget {
                private int exceptionTableIndex;

                public int getExceptionTableIndex() {
                    return exceptionTableIndex;
                }
            }

            final public static class offsetTarget {
                private int offset;

                public int getOffset() {
                    return offset;
                }
            }

            final public static class TypeArgumentTarget {
                private int offset;

                private byte typeArgumentIndex;

                public int getOffset() {
                    return offset;
                }

                public byte getTypeArgumentIndex() {
                    return typeArgumentIndex;
                }
            }

            final public static class TypePath {
                private byte pathLength;
                private byte[][] path;

                public byte getPathLength() {
                    return pathLength;
                }

                public byte[][] getPath() {
                    return path;
                }
            }

        }

        final public static class ParameterAnnotation {
            private int numAnnotations;
            private Annotation[] annotations;

            public int getNumAnnotations() {
                return numAnnotations;
            }

            public Annotation[] getAnnotations() {
                return annotations;
            }
        }

        final public static class Annotation {
            private int typeIndex;

            private int numElementValuePairs;
            private ElementValuePair[] elementValuePairs;

            public int getTypeIndex() {
                return typeIndex;
            }

            public int getNumElementValuePairs() {
                return numElementValuePairs;
            }

            public ElementValuePair[] getElementValuePairs() {
                return elementValuePairs;
            }
        }

        final public static class ElementValuePair {
            private int elementNameIndex;

            private ElementValue value;

            public int getElementNameIndex() {
                return elementNameIndex;
            }

            public ElementValue getValue() {
                return value;
            }

            public static class ElementValue {
                private byte tag;

                public byte getTag() {
                    return tag;
                }
            }

            final public static class ElementValueConstValueIndex extends ElementValue {
                private int constValueIndex;

                public int getConstValueIndex() {
                    return constValueIndex;
                }
            }

            final public static class ElementValueEnumConstValue extends ElementValue {
                private int[] enumConstValue;

                public int[] getEnumConstValue() {
                    return enumConstValue;
                }
            }

            final public static class ElementValueClassInfoIndex extends ElementValue {
                private int classInfoIndex;

                public int getClassInfoIndex() {
                    return classInfoIndex;
                }
            }

            final public static class ElementValueAnnotationValue extends ElementValue {
                private Annotation annotationValue;

                public Annotation getAnnotationValue() {
                    return annotationValue;
                }
            }

            final public static class ElementValueArrayValue extends ElementValue {
                private int numValues;
                private ElementValue[] arrayValue;

                public int getNumValues() {
                    return numValues;
                }

                public ElementValue[] getArrayValue() {
                    return arrayValue;
                }
            }
        }
    }
}
