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
