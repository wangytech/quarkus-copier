package tech.wangy.quarkus.copier.runtime;

public interface CopierConvertor<F, T> {
    T convert(F from);
}
