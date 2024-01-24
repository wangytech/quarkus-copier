package tech.wangy.quarkus.copier.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class RegisteredCopierBuildItem extends MultiBuildItem {


    private final String name;


    public String getName() {
        return name;
    }

    public RegisteredCopierBuildItem(String name) {
        this.name = name;
    }
}
