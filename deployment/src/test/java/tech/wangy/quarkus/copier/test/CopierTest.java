package tech.wangy.quarkus.copier.test;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tech.wangy.quarkus.copier.runtime.Copier;

public class CopierTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addAsResource("application.properties")
                            .addClasses(A.class)
                            .addClasses(B.class)
                            .addClasses(C.class)
                            .addClasses(String2IntegerConvertor.class)
            );

    @Inject
    Copier<A, A> copier;

    @Inject
    Copier<A, B> copierAb;


    @Inject
    Copier<A, ?> copier1;

    @Inject
    Copier<?, ?> copier2;

    @Inject
    Copier<?, C> copier3;

    @Inject
    Copier copier4;

    @Test

    public void test() throws Exception {
        tech.wangy.quarkus.copier.runtime.CopierConvertor<java.lang.Long, java.lang.Integer> conv =
                tech.wangy.quarkus.copier.test.String2IntegerConvertor::cover;
        A a = new A();
        a.setA("123");
        a.setB(123);
        a.setC("1122");
        a.setD(1.23);
        a.setE(11111L);
        a.setF(Short.parseShort("1"));
        A nA = new A();
        copier.copy(a, nA);

        B nB = new B();
        copierAb.copy(a, nB);
        System.out.println(1);
    }

}
