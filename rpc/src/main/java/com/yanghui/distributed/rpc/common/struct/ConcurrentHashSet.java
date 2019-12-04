package com.yanghui.distributed.rpc.common.struct;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 线程安全的 HashSet
 * @author YangHui
 */
public class ConcurrentHashSet<T> extends AbstractSet<T> implements Set<T> {

    private final ConcurrentMap<T,Object> map;

    private static final Object CONSTANT = new Object();

    public ConcurrentHashSet(){
        map = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(int initialCap){
        map = new ConcurrentHashMap<>(initialCap);
    }

    public ConcurrentHashSet(Collection<? extends T> collection){
        map = new ConcurrentHashMap<>();
        addAll(collection);
    }


    @Override
    public boolean add(T t) {
        return map.put(t, CONSTANT) == null;
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == CONSTANT;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }


}
