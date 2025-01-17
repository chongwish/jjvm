# A toy JVM written by Java

### Instruction

**Fewer than 20 files make a JVM for yourself! Now there are only 11 files!**

**Support Java 8, Java 11 and Java 14!**

### Purpose

This purpose is not only for me, but also for the other javaer.

1. For fun.
2. To know how JVM works.
3. To learn Java deeply.
4. Make it (JJVM) **Simple**, **Clean** and **Funny**.

### Project structure

```
.                         # main project
├── build.gradle
├── demo                  # subproject
│   ├── build.gradle
│   └── src
│       └── main          #  some examples
├── gradle
├── gradlew
├── gradlew.bat
├── README.md
├── settings.gradle
└── src                   # gradle project
    ├── main              #  core code
    └── test              #  test case
```

The structure of the core code:

```
src/main/java/me/chongwish/jjvm/
├── Bytecode.java         # bytecode wrapper
├── Classfile.java        # parse the .class file
├── Classloader.java      # class loader
├── Classpath.java        # find class from [file,directory,jar,zip,jrt]
├── CommandLine.java      # parse command line argument
├── Frame.java            # java frame, is not jframe
├── Instruction.java      # jvm instruction
├── Interpreter.java      # jvm interpreter
├── NativeMethod.java     # jvm native method
├── RuntimeDataArea.java  # jvm runtime data area
└── Starter.java          # jjvm entry
```

### How to work

#### Development Environment

Mine is that:

- emacs + lsp-java
- openjdk 14 & openjdk 11 & openjdk 8
- archlinux container on gentoo host & windows 10

I haven't tested other jdk version.

#### Build & Run

Build a jar first:

``` shell
./gradlew clean build
```

And then a JVM which name is jjvm.jar will be generated in the build directory. Run it:

``` shell
java -jar build/libs/jjvm.jar
```

Some message was displayed in your terminal:

```
JJVM [-version] [-?|-help] [-cp|-classpath $path]... clazz [$argv...]
        clazz $argv: Main class and it's argument
        -version: Show version
        -?, -help: Show manual
        -cp, -classpath $path: Classpath
```

There are some samples in the demo project. For example, there are three java file:

``` java
// file: demo/src/main/java/me/chongwish/jjvm/demo/Main.java
package me.chongwish.jjvm.demo;

import me.chongwish.jjvm.demo.sort.Bubble;
import me.chongwish.jjvm.demo.sort.Sort;

public class Main {
    public static void main(String[] args) {
        System.out.println("Bubble.sort");
        Integer[] array = new Integer[] { 5, 4, 1, 8, 12, 6 };
        System.out.print("before: ");
        for (int n : array) {
            System.out.print(n + " ");
        }
        System.out.println();
        Sort<Integer> fn = new Bubble<>();
        fn.sort(array);
        System.out.print("after: ");
        for (int n : array) {
            System.out.print(n + "");
        }
        System.out.println();
    }
}
```

``` java
// file: demo/src/main/java/me/chongwish/jjvm/demo/sort/Sort.java
package me.chongwish.jjvm.demo.sort;

public interface Sort<T extends Comparable<? super T>> {
    public T[] sort(T[] array);
}
```

``` java
// file: demo/src/main/java/me/chongwish/jjvm/demo/sort/Bubble.java
package me.chongwish.jjvm.demo.sort;

public class Bubble<T extends Comparable<? super T>> implements Sort<T> {
    @Override
    public T[] sort(T[] array) {
        for (int i = 0; i < array.length - 1; ++i) {
            for (int j = 0; j < array.length - 1 - i; ++j) {
                if (array[j].compareTo(array[j + 1]) > 0) {
                    T temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
        return array;
    }
}
```

The class file was generated automatically when you run the command `gradle run build`.

``` shell
demo/build/classes/java/main/me/chongwish/jjvm/demo/
├── Main.class
└── sort
    ├── Bubble.class
    └── Sort.class
```

To use our JVM to run the class which name is `Main`:

``` shell
java -jar build/libs/jjvm.jar -cp demo/build/classes/java/main me.chongwish.jjvm.demo.Main
# or
java -cp build/classes/java/main me.chongwish.jjvm.Starter -cp demo/build/classes/java/main me.chongwish.jjvm.demo.Main
```

The output:

``` 
Bubble.sort
before: 5 4 1 8 12 6 
after: 1 4 5 6 8 12
```

### Todo

- [ ] Unfinished Instruction  (0/4)
- [ ] Multi Thread
- [x] Reflection (A poorly implementation)
- [ ] Native Method
- [x] Exception (A poorly implementation)
- [ ] Newer Feature come from Java 8+
- [ ] VM System Class Initialization

### **Reference**

- [The Java® Language Specification](https://docs.oracle.com/javase/specs/jls/se14/html/index.html)
- [The Java® Virtual Machine Specification](https://docs.oracle.com/javase/specs/jvms/se14/html/index.html)
- 《自己动手写 Java 虚拟机》