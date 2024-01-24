package tech.wangy.quarkus.copier.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedClassBuildItem;
import jakarta.inject.Inject;
import javassist.*;
import org.jboss.jandex.*;
import org.jboss.logging.Logger;
import tech.wangy.quarkus.copier.runtime.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

class QuarkusCopierProcessor {

    private static final Logger LOG = Logger.getLogger(QuarkusCopierProcessor.class);
    private static final String FEATURE = "quarkus-copier";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    private static final String CONVERTOR = "tech.wangy.convertor.Convertors";

    /**
     * 添加复制转换器编译步骤
     *
     * @param copierConvertorBuildItemBuildProducer
     * @param indexBuildItem
     */
    @BuildStep
    public void addCopierConvertor(BuildProducer<CopierConvertorBuildItem> copierConvertorBuildItemBuildProducer, CombinedIndexBuildItem indexBuildItem) {
        DotName convertor = DotName.createSimple(CopierConvertor.class);
        /**
         * 取得所有的转换注解
         */
        Collection<AnnotationInstance> anno = indexBuildItem.getComputingIndex().getAnnotations(DotName.createSimple(CopierConvert.class));
        for (AnnotationInstance ai : anno) {
            if (ai.target().kind() == AnnotationTarget.Kind.CLASS) {
                //类型注解
                ClassInfo conInfo = ai.target().asClass();
                // 处理接口
                for (Type type : conInfo.interfaceTypes()) {
                    // 泛型接口
                    if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                        ParameterizedType pType = type.asParameterizedType();
                        if (convertor.equals(pType.name())) {
                            //实现了Convertor
                            Type from = pType.arguments().get(0);
                            Type to = pType.arguments().get(1);
                            if (from.kind() == Type.Kind.CLASS && to.kind() == Type.Kind.CLASS) {
                                // 拥有标准的泛型
                                copierConvertorBuildItemBuildProducer.produce(
                                        new CopierConvertorBuildItem(conInfo.name().toString(), from.name().toString(), to.name().toString())
                                );
                            } else {
                                LOG.warn(String.format("无法解析特殊泛型的Convertor: %s", conInfo.name().toString()));
                            }
                        }
                    } else if (type.kind() == Type.Kind.CLASS) {
                        ClassType cType = type.asClassType();
                        if (convertor.equals(cType.name())) {
                            // 声明继承了接口,却不存在泛型
                            LOG.warn(String.format("Convertor的实现接口必须指定类而不是泛型: %s", conInfo));
                        }
                    }
                }
            } else if (ai.target().kind() == AnnotationTarget.Kind.METHOD) {
                MethodInfo mType = ai.target().asMethod();
                if (mType.parametersCount() != 1) {
                    LOG.warn(String.format("Convertor方法的入参数量不为1: %s", mType));
                    continue;
                }
                Type from = mType.parameterTypes().get(0);
                Type to = mType.returnType();
                if (from.kind() == Type.Kind.CLASS && to.kind() == Type.Kind.CLASS) {
                    // 拥有标准的泛型
                    copierConvertorBuildItemBuildProducer.produce(
                            new CopierConvertorBuildItem(mType.declaringClass().name().toString() + "#" + mType.name(),
                                    from.name().toString(),
                                    to.name().toString())
                    );
                } else {
                    LOG.warn(String.format("Convertor方法的传入参数和返回参数必须是类: %s", mType));
                }
            }
        }

    }

    /**
     * 添加复制器编译步骤
     *
     * @param copierBuildItemBuildProducer
     * @param indexBuildItem
     */
    @BuildStep
    public void addCopierStep(BuildProducer<CopierBuildItem> copierBuildItemBuildProducer, CombinedIndexBuildItem indexBuildItem) {
        Collection<AnnotationInstance> anno = indexBuildItem.getComputingIndex().getAnnotations(DotName.createSimple(CopyTo.class));
        for (AnnotationInstance ai : anno) {
            List<String> values = null;
            boolean reversible = true;
            String from = ai.target().asClass().name().toString();
            for (AnnotationValue av : ai.values()) {
                if ("value".equals(av.name())) {
                    values = av.asArrayList().stream().map(
                            AnnotationValue::asClass
                    ).map(Type::name).map(DotName::toString).collect(Collectors.toList());
                } else if ("reversible".equals(av.name())) {
                    reversible = av.asBoolean();
                }
            }
            for (String to : values) {

                copierBuildItemBuildProducer.produce(
                        new CopierBuildItem(from, to)
                );
                if (reversible) {
                    copierBuildItemBuildProducer.produce(
                            new CopierBuildItem(to, from)
                    );
                }
            }

        }
        anno = indexBuildItem.getComputingIndex().getAnnotations(DotName.createSimple(Inject.class));
        DotName copier = DotName.createSimple(Copier.class);
        for (AnnotationInstance ai : anno) {
            if (ai.target().kind() == AnnotationTarget.Kind.FIELD) {
                Type type = ai.target().asField().type();
                if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                    ParameterizedType pType = type.asParameterizedType();
                    if (copier.equals(pType.name())) {
                        Type from = pType.arguments().get(0);
                        Type to = pType.arguments().get(1);
                        if (from.kind() == Type.Kind.CLASS && to.kind() == Type.Kind.CLASS) {
                            copierBuildItemBuildProducer
                                    .produce(
                                            new CopierBuildItem(
                                                    from.toString(), to.toString()
                                            )
                                    );
                        } else {
                            LOG.warn(String.format("Copier的注入泛型必须具体类而非泛型: %s", ai.target().toString()));
                        }
                    }
                } else if (type.kind() == Type.Kind.CLASS) {
                    if (copier.equals(
                            type.name())) {
                        LOG.warn(String.format("Copier的注入必须声明泛型的具体类型: %s", ai.target().toString()));
                    }
                }
            }
        }
    }

    @BuildStep
    public void addCopierGenerate(List<CopierConvertorBuildItem> copierConvertorBuildItems,
                                  List<CopierBuildItem> copierBuildItems,
                                  BuildProducer<GeneratedClassBuildItem> generatedClassBuildItemBuildProducer,
                                  BuildProducer<RegisteredCopierBuildItem> registeredCopierBuildItemBuildProducer,

                                  CopierConf conf

    ) {
        List<CopierGeneratedClassBuildItem> generatedClassBuildItems = new LinkedList<>();
        ClassPool pool = ClassPool.getDefault();
        Map<String, Map<String, String>> convertors =
                registerConvertor(pool, copierConvertorBuildItems, generatedClassBuildItems);
        registerCopier(copierBuildItems, convertors, pool, generatedClassBuildItems);
        if (!generatedClassBuildItems.isEmpty()) {
            //注册类
            generatedClassBuildItems.stream().map(
                    i -> new GeneratedClassBuildItem(true, i.getName(), i.getData())
            ).forEach(generatedClassBuildItemBuildProducer::produce);
            //收集所有Copier
            generatedClassBuildItems.stream()
                    .filter(CopierGeneratedClassBuildItem::isCopier)
                    .map(CopierGeneratedClassBuildItem::getName)
                    .map(
                            RegisteredCopierBuildItem::new
                    ).forEach(
                            registeredCopierBuildItemBuildProducer::produce
                    );
            if (conf.export.isPresent()) {
                File root = new File(conf.export.get()).getAbsoluteFile();
                LOG.info(String.format("写入生成的类到: %s", root));
                generatedClassBuildItems.stream().forEach(
                        i -> {
                            Path path = root.toPath().resolve(Path.of(i.getName().replaceAll("\\.", "/") + ".class")).toAbsolutePath();
                            try {
                                path.getParent().toFile().mkdirs();
                                Files.write(path, i.getData());
                            } catch (Throwable e) {
                                LOG.warn(String.format("写入类异常: %s", path), e);
                            }
                        }

                );

            }
        }

    }

    /**
     * 注册所有的转换器
     *
     * @param pool
     * @param copierConvertorBuildItems
     * @param generatedClassBuildItems
     * @return
     */
    private Map<String, Map<String, String>> registerConvertor(ClassPool pool, List<CopierConvertorBuildItem> copierConvertorBuildItems, List<CopierGeneratedClassBuildItem> generatedClassBuildItems) {
        Map<String, Map<String, String>> convertors = new HashMap<>();
        int i = 0;
        try {
            CtClass ctClass = pool.makeClass(CONVERTOR);
            CtConstructor constructor = new CtConstructor(new CtClass[]{}, ctClass);
            constructor.setBody("{}");
            constructor.setModifiers(Modifier.PRIVATE);
            ctClass.addConstructor(constructor);
            Map<String, String> fields = new HashMap<>();
            CtClass convertor = pool.get(CopierConvertor.class.getName());
            for (CopierConvertorBuildItem copierConvertorBuildItem : copierConvertorBuildItems) {

                Map<String, String> toConvertors = convertors.computeIfAbsent(copierConvertorBuildItem.getFrom(), s -> new HashMap<>());
                String fieldName = toConvertors.get(copierConvertorBuildItem.getTo());
                if (fieldName != null) {
                    LOG.warn(String.format(
                                    "忽略Convertor %s,因为忽略Convertor <%s,%s> 已经被定义为 %s : %s",
                                    copierConvertorBuildItem.getConvertor(),
                                    copierConvertorBuildItem.getFrom(),
                                    copierConvertorBuildItem.getTo(),
                                    fields.get(fieldName)
                            )
                    );
                } else {
                    fieldName = "C" + i;
                    String[] clsMethod = copierConvertorBuildItem.getConvertor().split("#");
                    CtClass fieldType;

                    if (clsMethod.length == 1) {
                        fieldType = pool.get(clsMethod[0]);

                    } else {
                        CtClass convType = pool.get(clsMethod[0]);
                        CtMethod convMethod = convType.getDeclaredMethod(
                                clsMethod[1],
                                new CtClass[]{pool.get(copierConvertorBuildItem.getFrom())}
                        );

                        if (Modifier.isPublic(convMethod.getModifiers())) {

                            fieldType = pool.makeClass(CONVERTOR + "_" + fieldName);
                            fieldType.setInterfaces(new CtClass[]{convertor});
                            CtMethod convert = new CtMethod(
                                    pool.get(copierConvertorBuildItem.getTo()),
                                    "convert",
                                    new CtClass[]{pool.get(copierConvertorBuildItem.getFrom())},
                                    fieldType);
                            String expr;
                            if (Modifier.isStatic(convMethod.getModifiers())) {
                                expr = String.format(
                                        "{return %s.%s($1);}",
                                        convMethod.getDeclaringClass().getName(),
                                        convMethod.getName());
                            } else {
                                CtField field = new CtField(convType, "delegate", fieldType);
                                field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
                                CtField.Initializer init = CtField.Initializer.byExpr("new " + convType.getName() + "();");
                                fieldType.addField(field, init);
                                expr = String.format(
                                        "{return $0.delegate.%s($1);}",
                                        convMethod.getName());
                            }
                            convert.setBody(
                                    expr
                            );
                            fieldType.addMethod(convert);
                            generatedClassBuildItems.add(
                                    new CopierGeneratedClassBuildItem(false, fieldType.getName(), fieldType.toBytecode())
                            );
                        } else {
                            LOG.warn(String.format("Copier的Convertor方法不是公开的: %s", methodName(convMethod)));
                            continue;
                        }
                    }
                    CtField ctField = new CtField(fieldType, fieldName, ctClass);
                    CtField.Initializer initializer = CtField.Initializer.byNew(fieldType);
                    ctField.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
                    ctClass.addField(ctField, initializer);
                    fields.put(fieldName, copierConvertorBuildItem.getConvertor());
                    toConvertors.put(copierConvertorBuildItem.getTo(), fieldName);
                    i++;
                }
            }
            generatedClassBuildItems.add(
                    new CopierGeneratedClassBuildItem(false, CONVERTOR, ctClass.toBytecode())
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return convertors;
    }

    /**
     * @param copierBuildItems
     * @param convertors
     * @param pool
     * @param generatedClassBuildItems
     */
    private void registerCopier(List<CopierBuildItem> copierBuildItems, Map<String, Map<String, String>> convertors, ClassPool pool,
                                List<CopierGeneratedClassBuildItem> generatedClassBuildItems) {

        for (CopierBuildItem item : copierBuildItems) {
            String from = item.getFrom();
            String to = item.getTo();

            String clsName = CopierInjectProducer.generateName(from, to);
            CtClass cc = pool.getOrNull(clsName);
            if (cc == null) {
                try {
                    CtClass copier = pool.get(Copier.class.getName());
                    cc = pool.makeClass(clsName);
                    cc.setModifiers(Modifier.PUBLIC);

                    cc.setInterfaces(new CtClass[]{copier});

                    CtConstructor constructor = new CtConstructor(new CtClass[]{}, cc);
                    constructor.setBody("{}");
                    cc.addConstructor(constructor);

                    CtClass fc = pool.get(from);
                    CtClass tc = pool.get(to);
                    List<CtMethod> getters = new LinkedList<>();
                    for (CtMethod method : fc.getDeclaredMethods()) {
                        if (method.getParameterTypes().length == 0
                                && (method.getName().startsWith("is") || method.getName().startsWith("get"))
                        ) {
                            getters.add(method);
                        }
                    }
                    Map<String, CtMethod> setters = new HashMap<>();

                    for (CtMethod method : tc.getDeclaredMethods()) {
                        if (method.getName().startsWith("set")
                                && method.getParameterTypes().length == 1
                        ) {
                            setters.put(method.getName(), method);
                        }
                    }
                    StringBuilder body = new StringBuilder("{" +

                            "if($1!=null&&$2!=null){" +
                            fc.getName() + " f=(" + fc.getName() + ")$1;" +
                            tc.getName() + " t=(" + tc.getName() + ")$2;"
                    );
                    for (CtMethod get : getters) {
                        String setName = get.getName().replaceAll("^(is|get)", "set");
                        CtMethod set = setters.get(setName);
                        if (set != null) {
                            CtClass fType = get.getReturnType();
                            CtClass tType = set.getParameterTypes()[0];
                            if (fType.equals(tType)) {
                                body.append(
                                        "t." + set.getName() + "(f." + get.getName() + "());"
                                );
                                continue;
                            } else if (tType.isPrimitive() && pool.getCtClass(((CtPrimitiveType) tType).getWrapperName())
                                    .equals(fType)) {
                                body.append(
                                        "t." + set.getName() + "((" + fType.getName() + ")f." + get.getName() + "());"
                                );
                                continue;
                            } else if (fType.isPrimitive() && pool.getCtClass(((CtPrimitiveType) fType).getWrapperName())
                                    .equals(tType)) {
                                body.append(
                                        "t." + set.getName() + "(new " + tType.getName() + "(f." + get.getName() + "()));"
                                );
                                continue;
                            } else {
                                Map<String, String> toCon = convertors.get(fType.getName());
                                if (toCon != null) {
                                    String field = toCon.get(tType.getName());
                                    if (field != null) {
                                        body.append(
                                                "t." + set.getName() + "(" + CONVERTOR + "." + field + ".convert(f." + get.getName() + "()));"
                                        );
                                        continue;
                                    }
                                }
                            }
                            LOG.warn(String.format("同名对象类型无法转换: %s %s", methodName(get), methodName(set)));

                        }

                    }
                    body.append("}}");
                    CtMethod method = copier.getDeclaredMethod("copy");
                    method = CtNewMethod.copy(method, cc, null);
                    method.setModifiers(Modifier.PUBLIC);
                    method.setBody(body.toString());
                    cc.addMethod(method);
                    byte[] clsBytes = cc.toBytecode();

                    generatedClassBuildItems
                            .add(new CopierGeneratedClassBuildItem(true, clsName, clsBytes));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private String methodName(CtMethod method) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(method.getReturnType().getName())
                    .append(" ")
                    .append(method.getDeclaringClass().getName())
                    .append("#")
                    .append(method.getName())
                    .append("(");
            if (method.getParameterTypes().length > 0) {
                for (CtClass pType : method.getParameterTypes()) {
                    builder.append(pType.getName())
                            .append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
            }
            builder.append(")");
            return builder.toString();
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public void regToRecorder(List<RegisteredCopierBuildItem> quarkusCopierProcessors, QuarkusCopierRecorder recorder) {
        recorder.copierHolder(
                quarkusCopierProcessors
                        .stream()
                        .map(RegisteredCopierBuildItem::getName)
                        .collect(Collectors.toList())
        );
    }

    @BuildStep
    public void addProducer(
            BuildProducer<AdditionalBeanBuildItem> additionalBean) {
        additionalBean.produce(AdditionalBeanBuildItem.unremovableOf(
                CopierInjectProducer.class
        ));


    }


}
