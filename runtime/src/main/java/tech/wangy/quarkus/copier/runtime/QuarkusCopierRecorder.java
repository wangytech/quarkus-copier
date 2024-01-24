package tech.wangy.quarkus.copier.runtime;

import io.quarkus.runtime.annotations.Recorder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Recorder
public class QuarkusCopierRecorder {


    public static volatile Set<String> copier;

    public QuarkusCopierRecorder() {
    }

    public void copierHolder(List<String> items) {
        if (items != null) {
            if (copier == null) {
                copier = new HashSet<>(items);
            } else {
                copier = new HashSet<>(copier);
                copier.addAll(items);
            }
            copier = Collections.unmodifiableSet(copier);
        }
    }
}
