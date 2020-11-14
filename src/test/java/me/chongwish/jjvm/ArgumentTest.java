package me.chongwish.jjvm;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ArgumentTest {
    @Test
    public void testClasspaths() {
        Argument argument = Argument.parse("-cp", ".", "-cp", "/opt/jre", "-classpath", "/usr");

        assertEquals(3, argument.getClasspaths().size());
        assertEquals("/usr", argument.getClasspaths().get(2));
    }
}
