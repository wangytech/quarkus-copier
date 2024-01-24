package tech.wangy.quarkus.copier.test;

import tech.wangy.quarkus.copier.runtime.CopyTo;

@CopyTo(value = {B.class, C.class}, reversible = false)

public class A {

    private String a;

    private int b;

    private String c;


    private Double d;


    private Long e;
    private Short f;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public Double getD() {
        return d;
    }

    public void setD(Double d) {
        this.d = d;
    }

    public Long getE() {
        return e;
    }

    public void setE(Long e) {
        this.e = e;
    }

    public Short getF() {
        return f;
    }

    public void setF(Short f) {
        this.f = f;
    }
}
