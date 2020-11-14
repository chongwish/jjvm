package me.chongwish.jjvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ClassfileTest {
    @Test
    public void testConvertFile() {
        String className = "me.chongwish.jjvm.Main".replace('.', '/');
        String classfile = Classfile.convertToInnerRelativeFile(className);
        assertEquals(classfile, "me/chongwish/jjvm/Main.class".replace("/", File.separator));
    }

    @Test
    public void testClassParsing() {
        final List<String> classpaths = new ArrayList<>();
        classpaths.add("./build/classes/java/main");

        Classpath.parse(classpaths);

        String className = "me.chongwish.jjvm.Classfile".replace('.', '/');

        Classfile.ConstantPool[] constantPool = Classfile.readInformation(className).getConstantPool();
        assertNotNull(constantPool);

        Classfile.Information information = Classfile.readInformation(className);

        assertTrue(information.getFields().length > 0);
        assertEquals(information.getThisClassName(), information.getClassName());
    }

    @Test
    public void testCache() {
        final List<String> classpaths = new ArrayList<>();
        classpaths.add("./build/classes/java/main");

        Classpath.parse(classpaths);

        String className = "me.chongwish.jjvm.Classfile".replace('.', '/');

        Classfile.Information information1 = Classfile.readInformation(className);
        Classfile.Information information2 = Classfile.readInformation(className);

        assertTrue(information1 == information2);
    }
}
