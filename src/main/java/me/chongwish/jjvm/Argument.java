package me.chongwish.jjvm;

import java.util.List;

import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Getter
@Command(name = "jjvm", description = "A toy of jvm")
class Argument {
    private Argument() {}

    @Option(names = { "-version" }, description = "display version info")
    private boolean versionRequestExist;

    @Option(names = { "-?", "-help" }, usageHelp = true, description = "display help message")
    private boolean helpRequestExist = false;

    @Option(names = { "-cp", "-classpath" }, description = "class search path of directories")
    private List<String> classpaths;

    @Parameters(index = "0", paramLabel = "mainclass", description = "main class")
    private String clazzName;

    @Parameters(paramLabel = "args...", description = "arguments following the main class")
    private List<String> args;

    /**
     * Parse the argument come from the command line to be a Argument object.
     * @param args
     */
    public static Argument parse(String... args) {
        Argument argument = new Argument();
        CommandLine commandLine = new CommandLine(argument);

        try {
            commandLine.parseArgs(args);
        } catch (MissingParameterException e) {
            // nothing need to do here
        }

        return argument;
    }

    /**
     * Display the usage in the console
     */
    public void printUsage() {
        CommandLine commandLine = new CommandLine(this);
        commandLine.usage(System.out);
    }
}
