package tech.wangy.quarkus.copier.runtime;

public interface Copier<F, T> {

    T copy(F from, T to);


    default T copy(F from) {
        return copy(from, newTo());
    }

    T newTo();


}
