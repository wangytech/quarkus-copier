package tech.wangy.quarkus.copier.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class CopierConvertorBuildItem extends MultiBuildItem {

    private final String convertor;
    private final String from;
    private final String to;

    public CopierConvertorBuildItem(String convertor, String from, String to) {
        this.convertor = convertor;
        this.from = from;
        this.to = to;
    }

    public String getConvertor() {
        return convertor;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
