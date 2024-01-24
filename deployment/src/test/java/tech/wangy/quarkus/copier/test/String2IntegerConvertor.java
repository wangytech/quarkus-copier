package tech.wangy.quarkus.copier.test;

import tech.wangy.quarkus.copier.runtime.CopierConvert;
import tech.wangy.quarkus.copier.runtime.CopierConvertor;

@CopierConvert
public class String2IntegerConvertor implements CopierConvertor<String, Integer> {
    @Override
    public Integer convert(String from) {
        return from == null ? null : Integer.valueOf(from);
    }


    @CopierConvert
    public static Integer cover(Long from) {
        return from == null ? null : from.intValue();
    }

    @CopierConvert
    public Integer cover(Float from) {
        return from == null ? null : from.intValue();
    }

    @CopierConvert
    private Integer cover(Double from) {
        return from == null ? null : from.intValue();
    }

    @CopierConvert
    public void cover1(Long from) {

    }

    @CopierConvert
    public Integer cover() {
        return 0;
    }
}
