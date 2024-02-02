package tech.wangy.quarkus.copier.runtime;

import io.quarkus.runtime.annotations.Recorder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Recorder
public class QuarkusCopierRecorder {


    public static volatile Set<Class> copier;

    public QuarkusCopierRecorder() {
    }

    private static Class loadClass(String s) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(s);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void copierHolder(List<String> items) {
        if (items != null) {
            if (copier == null) {
                copier = new HashSet<>();
            } else {
                copier = new HashSet<>(copier);
            }
            copier.addAll(items.stream().map(QuarkusCopierRecorder::loadClass).collect(Collectors.toList()));
            copier = Collections.unmodifiableSet(copier);
        }
    }
}
