package me.chongwish.jjvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parse the argument to the jjvm & main class.
 * <p>
 * 
 * <b>For example:</b>
 * <p>
 * <ul>
 * <li>{@code jjvm -help}: display manual</li>
 * <li>{@code jjvm -version}:display version</li>
 * <li>{@code jjvm -cp path1 -classpath path2 main.class -name value}</li>
 * </ul>
 * 
 * <b>Usage</b>:
 * <p>
 * {@code new CommandLine(...)}
 * <p>
 * The last example will be like that:
 * <p>
 * {@code commandline = new CommandLine("-cp", "path1", "-classpath", "path2", "main.class", "-name", "value")}
 * <p>
 * The classpath which will be passed to the jjvm is a list:
 * <p>
 * {@code commandline.getClasspaths() // path1 & path2}
 * <p>
 * The entry class is:
 * <p>
 * {@code commandline.getClazzName() // main.class}
 * <p>
 * And the argument which will be passed to the class is:
 * <p>
 * {@code commandline.getUserArgs() // -name value}
 * 
 */
public class CommandLine {
    private static final List<String> VERSION_IDENTIFIERS = Arrays.asList("-version");
    private static final List<String> HELP_IDENTIFIERS = Arrays.asList("-?", "-help");
    private static final List<String> CLASSPATHS_IDENTIFIERS = Arrays.asList("-cp", "-classpath");

    /**
     * Classpath list
     */
    private List<String> classpaths = new ArrayList<>();

    /**
     * Main class name
     */
    private String clazzName;

    /**
     * Main class argument list
     */
    private List<String> userArgs = new ArrayList<>();

    /**
     * All the argument
     */
    private List<String> args = new ArrayList<>();

    public CommandLine(String... text) {
        parseArgs(text);
        makeJvmArgs();
    }

    /**
     * Parse text to a argument list.
     * <p>
     * 
     * <b>For example</b>:
     * <p>
     * {@literal a bbb -c "dd' dd" 'e eee' g=hh}
     * <p>
     * Will be: {@code ["a", "bbb", "-c" "dd' dd", "e eee", "g", "hh"]}
     * 
     * @param text
     */
    private void parseArgs(String... text) {
        // record the argument is quoted by the ' or "
        char prefixChar = ' ';

        for (int i = 0; i < text.length; ++i) {
            // a single argument
            StringBuffer argv = new StringBuffer();

            // parse char by char
            for (char c : text[i].toCharArray()) {
                if (prefixChar == '"' || prefixChar == '\'') {
                    // if a quoted arguemt, it must be end by the same quote
                    if (c == prefixChar) {
                        // the last quote doesn't need to be recorded
                        prefixChar = ' ';
                    } else {
                        // keep the other chars
                        argv.append(c);
                    }
                } else {
                    if (c == ' ' || c == '=' || c == '\0') {
                        // if these char appear, it means a argument is complete
                        args.add(argv.toString());
                        argv = new StringBuffer();
                    } else {
                        // a quoted string appears
                        if (c == '"' || c == '\'') {
                            // the first quote doesn't need to be recorded
                            prefixChar = c;
                        } else {
                            // save the other chars
                            argv.append(c);
                        }
                    }
                }
            }

            // the last one argument will never ends with a specil char
            if (argv.length() != 0) {
                args.add(argv.toString());
            }
        }
    }

    /**
     * Divide the argument list into five categories:
     * <p>
     * 
     * <ol>
     * <li>version flag
     * <li>help flag
     * <li>classpath list
     * <li>main class name
     * <li>main class argument list
     * </ol>
     */
    private void makeJvmArgs() {
        // main class index
        int clazzI = 0;

        outter: for (int i = 0; i < args.size(); ++i) {
            // version
            for (String v : VERSION_IDENTIFIERS) {
                if (args.get(i).equals(v)) {
                    clazzI = i + 1;
                    showVersion();
                }
            }

            // help
            for (String v : HELP_IDENTIFIERS) {
                if (args.get(i).equals(v)) {
                    clazzI = i + 1;
                    showHelp();
                }
            }

            // classpath list
            for (String v : CLASSPATHS_IDENTIFIERS) {
                if (args.get(i).equals(v)) {
                    if (i + 1 < args.size()) {
                        // classpath needs a path as it's next argument
                        i++;
                        classpaths.add(args.get(i));
                        // main class index needs move
                        clazzI = i + 1;
                        // if a argument is a classpath, the work of this argument is done
                        continue outter;
                    } else {
                        // no path for classpath
                        System.err.println("JJVM's -cp/-classpath needs a argument!");
                        showHelp();
                    }
                }
            }

            // no other jvm argument here

            if (clazzI < args.size()) {
                // main class
                clazzName = args.get(clazzI);
                break;
            }
        }

        if (clazzName == null || clazzName.charAt(0) == '-') {
            // no main class
            System.err.println("JJVM needs a class to run!");
            showHelp();
        }

        // the other arugment is main class argument
        for (int i = clazzI + 1; i < args.size(); ++i) {
            userArgs.add(args.get(i));
        }
    }

    public void showVersion() {
        System.out.println("JJVM version 0.0.2");
        System.exit(0);
    }

    public void showHelp() {
        System.out.println("JJVM [-version] [-?|-help] [-cp|-classpath $path]... clazz [$argv...]");
        System.out.println("\tclazz $argv: Main class and it's argument");
        System.out.println("\t-version: Show version");
        System.out.println("\t-?, -help: Show manual");
        System.out.println("\t-cp, -classpath $path: Classpath");
        System.exit(0);
    }

    public String getClazzName() {
        return clazzName;
    }

    public List<String> getClasspaths() {
        return classpaths;
    }

    public List<String> getUserArgs() {
        return userArgs;
    }

    /**
     * For test
     * 
     * @param args
     */
    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(args);
        commandLine.showHelp();
        // commandLine.showVersion();
    }
}
