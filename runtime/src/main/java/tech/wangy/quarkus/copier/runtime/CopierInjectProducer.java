package tech.wangy.quarkus.copier.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

@ApplicationScoped
public class CopierInjectProducer {

    private final HashMap<String, Copier> copierMap;

    public CopierInjectProducer() {

        copierMap = new HashMap<>();

        for (Class cls : QuarkusCopierRecorder.copier) {
            try {
                copierMap.put(cls.getName(), (Copier) cls.getDeclaredConstructor().newInstance());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }


    public static String generateName(String from, String to) {
        return from + "_2_" + to.replaceAll("\\.", "_");

    }


    private Copier def = new Copier() {
        @Override
        public Object copy(Object from, Object to) {
            if (from != null && to != null) {
                String name = generateName(from.getClass().getName(), to.getClass().getName());
                Copier copier = copierMap.get(name);
                if (copier == null) {
                    throw new NullPointerException(
                            String.format(
                                    "未定义从'%s'到'%s'的复制器," +
                                            "请在类'%s'上添加注解'@%s(%s.class)'," +
                                            "或在类'%s'上添加注解'@%s(%s.class)'," +
                                            "或使用注入的'@%s %s<$s,%s> copier;'进行复制.修改后,请重新编译."
                                    , from.getClass().getName(), to.getClass().getName()
                                    , from.getClass().getName(), CopyTo.class.getName(), to.getClass().getName()
                                    , to.getClass().getName(), CopyFrom.class.getName(), from.getClass().getName()
                                    , Inject.class, Copier.class.getName(), from.getClass().getName(), to.getClass().getName()
                            )
                    );
                }
                return copier.copy(from, to);
            }
            return to;
        }

        @Override
        public Object newTo() {
            return new Object();
        }
    };


    public Copier copier() {
        return def;

    }

    public <F, T> Copier<F, T> copier(Type from, Type to) {
        Copier copier = null;
        if (from != null && to != null) {
            String name = generateName(from.getTypeName()
                    , to.getTypeName());
            copier = copierMap.get(name);
        }
        if (copier == null) {
            copier = def;
        }
        return copier;
    }

    @Produces
    public <F, T> Copier<F, T> produceCopier(
            InjectionPoint point) {
        if (point.getType() instanceof ParameterizedType) {
            ParameterizedType targetType = (ParameterizedType) point.getType();
            Type[] targetTypeArgs = targetType.getActualTypeArguments();
            return copier(targetTypeArgs[0], targetTypeArgs[1]);
        }
        return copier(null, null);
    }
}
