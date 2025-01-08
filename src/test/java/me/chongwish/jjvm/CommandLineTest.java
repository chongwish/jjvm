package me.chongwish.jjvm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommandLineTest {
    @Test
    public void testNoJvmArgs() {
        CommandLine commandLine = new CommandLine(
                "-jar abc",
                "-cp nowhere.jar -cp /home/chongwish",
                "-cp ./.././123",
                "aaa");

        assertEquals(0, commandLine.getClasspaths().size());
        assertEquals("-jar", commandLine.getClazzName());
        assertEquals(8, commandLine.getUserArgs().size());
    }

    @Test
    public void testJvmArgs() {
        CommandLine commandLine = new CommandLine(
                "-cp nowhere.jar -cp /home/chongwish",
                "-cp ./.././123",
                "-cp \"abc 'cde efg\" -cp 11111",
                "-jar abc feg egf");

        assertEquals(5, commandLine.getClasspaths().size());
        assertEquals("abc 'cde efg", commandLine.getClasspaths().get(3));
        assertEquals("-jar", commandLine.getClazzName());
    }

    @Test
    public void testClazzName() {
        CommandLine commandLine = new CommandLine(
                "-cp .",
                "-cp ./123",
                "-cp",
                "home",
                "mycls");

        assertEquals("mycls", commandLine.getClazzName());
    }
}
