package tech.wangy.quarkus.copier.runtime;

public interface Copier<F, T> {

    void copy(F from, T to);




}
