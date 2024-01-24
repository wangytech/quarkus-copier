package tech.wangy.quarkus.copier.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class CopierBuildItem extends MultiBuildItem {
    private final String from;
    private final String to;

    public CopierBuildItem(String from, String to) {

        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
