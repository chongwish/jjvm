package me.chongwish.jjvm.demo;

import me.chongwish.jjvm.demo.sort.Bubble;
import me.chongwish.jjvm.demo.sort.Sort;

public class Main {
    public static void main(String[] args) {
        System.out.println("Bubble.sort");
        Integer[] array = new Integer[] { 5, 4, 1, 8, 12, 6 };
        System.out.print("before: ");
        for (int n : array) {
            System.out.print(n);
            System.out.print(" ");
        }
        System.out.println();
        Sort<Integer> fn = new Bubble<>();
        fn.sort(array);
        System.out.print("after: ");
        for (int n : array) {
            System.out.print(n);
            System.out.print(" ");
        }
        System.out.println();
    }
}
