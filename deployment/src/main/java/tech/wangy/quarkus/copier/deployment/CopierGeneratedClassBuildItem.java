package tech.wangy.quarkus.copier.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class CopierGeneratedClassBuildItem extends MultiBuildItem {

    private final String name;
    private final byte[] data;
    private final boolean copier;

    public CopierGeneratedClassBuildItem(boolean copier, String name, byte[] data) {
        this.copier = copier;
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isCopier() {
        return copier;
    }
}
