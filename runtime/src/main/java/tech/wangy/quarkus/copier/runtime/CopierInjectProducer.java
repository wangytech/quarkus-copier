package tech.wangy.quarkus.copier.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

@ApplicationScoped
public class CopierInjectProducer {

    private final HashMap<String, Copier> copierMap;

    public CopierInjectProducer() {

        copierMap = new HashMap<>();

        for (String cls : QuarkusCopierRecorder.copier) {
            try {
                copierMap.put(cls, (Copier) Thread.currentThread().getContextClassLoader()
                        .loadClass(cls).getDeclaredConstructor().newInstance());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }


    public static String generateName(String from, String to) {
        return from + "_2_" + to.replaceAll("\\.", "_");

    }


    public Copier def = new Copier() {
        @Override
        public void copy(Object from, Object to) {
            if (from != null && to != null) {
                String name = generateName(from.getClass().getName(), to.getClass().getName());
                Copier copier = copierMap.get(name);
                if (copier == null) {
                    throw new NullPointerException(
                            String.format(
                                    "copier '%s' to '%s' undefined," +
                                            "add annotation '@%s(%s.class)' at '%s', " +
                                            "or use inject '@%s %s<$s,%s> copier;'"
                                    , from.getClass().getName(), to.getClass().getName()
                                    , CopyTo.class.getName(), to.getClass().getName(), from.getClass().getName()
                                    , "Inject", Copier.class.getName(), from.getClass().getName(), to.getClass().getName()
                            )
                    );
                }
            }
        }
    };

    @Produces
    public <F, T> Copier<F, T> produceCopier(
            InjectionPoint point) {
        Copier copier = null;
        if (point.getType() instanceof ParameterizedType) {
            ParameterizedType targetType = (ParameterizedType) point.getType();
            Type[] targetTypeArgs = targetType.getActualTypeArguments();
            String name = generateName(targetTypeArgs[0].getTypeName()
                    , targetTypeArgs[1].getTypeName());
            copier = copierMap.get(name);
        }
        if (copier == null) {
            copier = def;
        }
        return copier;
    }
}
