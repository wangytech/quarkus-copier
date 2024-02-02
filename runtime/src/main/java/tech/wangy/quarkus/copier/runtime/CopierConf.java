package tech.wangy.quarkus.copier.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;

@ConfigRoot(prefix = "", phase = ConfigPhase.BUILD_TIME)
public class CopierConf {
    /**
     * export copier class to this path
     */
    @ConfigItem
    public Optional<String> export;
}
