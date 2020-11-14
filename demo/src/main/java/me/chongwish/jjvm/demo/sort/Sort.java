package me.chongwish.jjvm.demo.sort;

public interface Sort<T extends Comparable<? super T>> {
    public T[] sort(T[] array);
}
