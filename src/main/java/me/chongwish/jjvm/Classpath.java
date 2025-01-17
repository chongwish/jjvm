package me.chongwish.jjvm;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classpath is used for searching java class file.
 * <p>
 * <b>Usage:</b>
 * <p>
 * To use class Classpath, it need to call the static method parse first:
 * <p>
 * {@code Classpath.parse(classpaths)}
 * <p>
 * Then we can call the static method getPath to get the real path of a class:
 * <p>
 * {@code Classpath.getPath("me/chongwish/jvm/Main.class") // => $classpath_has_this_class/me/chongwish/jvm/Main.class}
 * <p>
 * <b>Note:</b>
 * <p>
 * Before jdk 9, there is three type of classpath:
 * <p>
 * <ol>
 * <li>bootstrap classpath, for example: {@code -Xbootclasspath /usr/lib/jdk/lib}</li>
 * <li>extension classpath, for example: {@code -Xbootclasspath /usr/lib/jdk/lib/ext}</li>
 * <li>user classpath, for example: {@code -cp /xxx -cp /yyy:./zzz}</li>
 * </ol>
 * But now, we only care about user classpath
 * <p>
 * Class file will be found in jar/zip or directory, and you also can use wildcard path to get all jar/zip in a
 * directory.
 * <p>
 * <ul>
 * <li>{@code -cp /xxx/*: get all jar/zip in /xxx}</li>
 * <li>{@code -cp /yyy.jar}</li>
 * </ul>
 */
class Classpath {
    private static Classpath _instance = new Classpath();

    private Archive archive = new Archive();

    private Classpath() {}

    /**
     * A set of classpath, it only include directory
     */
    private Set<Path> classpathSet = new HashSet<>();

    public static Set<Path> getClasspathSet() {
        return _instance.classpathSet;
    }

    /**
     * generate a hash set of the real class path
     * 
     * @param classpaths
     */
    public static void parse(List<String> classpaths) {
        // classpath need a path at least
        if (classpaths == null) {
            classpaths = new ArrayList<>();
            // add current directory
            classpaths.add("");
        }

        // java 8- rt.jar
        classpaths.add((System.getProperty("java.home") + "/lib/rt.jar").replace('/', File.separatorChar));

        // add the absolute and existing path to the classpathSet variable, or map class
        // file in the jar/zip to the archive instance
        classpaths.forEach((classpath) -> {
            for (String path : classpath.split(File.pathSeparator)) {
                try {
                    if (path.endsWith("*")) {
                        // deal with wildcard path: -cp /xxx/*
                        _instance.archive.scan(path).forEach(_instance.archive::map);
                    } else {
                        Path realPath = Paths.get(path).toRealPath();

                        if (!Files.isDirectory(realPath)
                                && (realPath.toString().endsWith(".jar") || realPath.toString().endsWith(".zip"))) {
                            // deal with jar/zip: -cp /yyy.jar
                            _instance.archive.map(realPath);
                        } else {
                            // deal with directory
                            _instance.classpathSet.add(realPath);
                        }
                    }
                } catch (IOException e) {
                    // Nothing need to do, We can safely ignore the classpath which isn't exist.
                }
            }
        });
    }

    /**
     * Find the given class in the classpaths(directory or jar/zip), and return its
     * real path
     * 
     * @param classfile
     *        the relative path in the classpath
     * @return the real path of the given class
     */
    public static Path getPath(String classfile) {
        // java[xxx] package
        if (classfile.startsWith("java") || classfile.startsWith("jdk") || classfile.startsWith("sun")) {
            String classfileRTPath = Classpath.class.getClassLoader()
                    .getResource(classfile.replace(File.separatorChar, '/')).toString();
            if (classfileRTPath.startsWith("jrt")) {
                // java 9+
                FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
                return fs.getPath("/modules/java.base/", classfile);
            }
        }

        // find class in the directory
        for (Path classpath : _instance.classpathSet) {
            Path classfilePath = classpath.resolve(classfile);
            if (Files.exists(classfilePath)) {
                return classfilePath;
            }
        }

        if (!_instance.archive.isExist(classfile)) {
            throw new RuntimeException("Class file [" + classfile + "] can not be found!");
        }

        // find class in the jar/zip
        return _instance.archive.getPath(classfile);
    }

    /**
     * {@code Classpath.Archive} is used for dealing with
     * classpath which type is jar/zip format.
     * 
     * For convenience and efficiency, it use two inner map ({@code archiveHandlerMapper}, {@code classfileMapper})
     * to cache all the classes inside jar/zip, so we don't deal with jar/zip everytime we search a class.
     */
    private class Archive {
        /**
         * Record: jar/zip name -> jar/zip handler.
         */
        private Map<String, FileSystem> archiveHandlerMapper = new HashMap<>();

        /**
         * Record: class file path -> jar/zip name.
         */
        private Map<String, String> classfileMapper = new HashMap<>();

        /**
         * @param classfile
         *        the relative path of the class
         * @return is the given class inside jar/zip
         */
        public boolean isExist(String classfile) {
            return classfileMapper.containsKey(classfile);
        }

        /**
         * Find the given class in the jar/zip.
         * 
         * @param classfile
         *        the relative path of the class
         * @return the real path of the given class
         */
        public Path getPath(String classfile) {
            return archiveHandlerMapper.get(classfileMapper.get(classfile)).getPath(classfile);
        }

        /**
         * Search all the jar/zip in a direcoty
         * 
         * @param wildCard
         *        A path string with wildcard: /xxx/*
         * @return a jar/zip list
         */
        public List<Path> scan(String wildCard) {
            List<Path> pathList = new ArrayList<>();

            // drop the last char '*'
            Path wildCardPath = Paths.get(wildCard.substring(0, wildCard.length() - 1));

            try {
                // search '*.jar' and '*.zip' only
                DirectoryStream<Path> stream = Files.newDirectoryStream(wildCardPath, "*.{jar,zip}");

                stream.forEach(path -> {
                    pathList.add(path);
                });
                stream.close();
            } catch (IOException e) {
                // Nothing need to do, though it failed.
            }

            return pathList;
        }

        /**
         * Write the class of jar/zip to the map classfile, and map the jar/zip's name
         * to it's handler thought the map archiveHandlerMapper.
         * 
         * @param archivePath
         *        jar/zip path
         */
        public void map(Path archivePath) {
            String archiveName = archivePath.toString();

            try {
                // handle jar/zip
                FileSystem fileSystem = FileSystems.newFileSystem(archivePath, Classpath.class.getClassLoader());

                Files.walk(fileSystem.getPath("/"))
                        .filter(path -> {
                            // search all class file
                            return path.toString().endsWith(".class");
                        })
                        .forEach(path -> {
                            // record to classfileMapper
                            classfileMapper.putIfAbsent(path.toString().substring(1), archiveName);
                        });

                // record to archiveHandlerMapper
                archiveHandlerMapper.putIfAbsent(archiveName, fileSystem);
            } catch (IOException e) {
                e.printStackTrace();
                // Nothing need to do
            }
        }
    }

    /**
     * For test
     * 
     * @param args
     */
    public static void main(String[] args) {
        List<String> classpaths = new ArrayList<>();

        String path1 = "/usr/lib/jvm/default/classes/java/main";
        String path2 = "./bin/test";
        String path3 = "./demo/build/classes/class/java/main";
        String path4 = "./build/classes/java/main";

        classpaths.add(path1);
        classpaths.add(path2);
        classpaths.add(path3);
        classpaths.add(path4);

        Classpath.parse(classpaths);

        final String classfile = Classfile.convertToInnerRelativeFile("me/chongwish/jjvm/Starter");
        System.out.printf("JVM Class: %s\n", classfile);

        final Path path = Classpath.getPath(classfile);
        System.out.printf("Found: %s\n", path);
    }
}
