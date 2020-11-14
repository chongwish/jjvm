package me.chongwish.jjvm;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class ClasspathTest {
    @Test
    public void testClasspath() {
        final List<String> classpaths = new ArrayList<>();
        classpaths.add("/usr/lib" + File.pathSeparator + "./build/classes/java/main");
        classpaths.add("./bin/test");

        Classpath.parse(classpaths);

        final Path path = Classpath.getPath(Classfile.convertToInnerRelativeFile("me/chongwish/jjvm/Starter"));
        assertEquals(path.toString(), Paths.get("").toAbsolutePath().toString() + "/build/classes/java/main/me/chongwish/jjvm/Starter.class".replace(("/"), File.separator));
    }
}
