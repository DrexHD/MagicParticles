package me.drex.magic_particles.debug;

import java.util.*;

public class DebugAccessList<E> extends AbstractList<E> {

    final List<E> list;
    String threadName;
    StackTraceElement[] stackTraceElements;

    public DebugAccessList(List<E> list) {
        this.list = list;
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        saveStackTrace();
        list.add(index, element);
    }

    @Override
    public E remove(int index) {
        saveStackTrace();
        return list.remove(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    public void saveStackTrace() {
        threadName = Thread.currentThread().getName();
        stackTraceElements = Thread.currentThread().getStackTrace();
    }

    // Copied from AbstractList.Itr
    private class Itr implements Iterator<E> {

        int cursor = 0;
        int lastRet = -1;
        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != size();
        }

        public E next() {
            checkForComodification();
            try {
                int i = cursor;
                E next = get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException(e);
            }
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                DebugAccessList.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) {
                if (threadName != null) {
                    System.err.println("Potential cause: " + threadName);
                }
                if (stackTraceElements != null) {
                    for (StackTraceElement stackTraceElement : stackTraceElements) {
                        System.err.println(stackTraceElement.toString());
                    }
                }
                throw new ConcurrentModificationException();
            }
        }
    }
}
