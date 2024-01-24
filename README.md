# quarkus的copier扩展

因为native编译的问题,导致动态类都不能用了.又不想使用基于反射的Bean复制.所以写了这个.

考虑在deployment阶段生成字节码和加载,使用了javassist而非cglib

目前拥有的特性:

* 通过CopyTo注解,声明当前类会被复制给哪些类(reversible设置双向复制)
  
* 通过Inject一个Copier对象,自动注入对应的复制器(注意Coper<F,T>的泛型必须是具体的某个Bean)
  
* 通过CopierConvert声明类是一个类型转换器(该类必须实现CopierConvertor<F,T>,同样的,泛型必须是具体某个Bean)

* 通过CopierConvert声明方法是一个类型转换器,该方法必须是public的,方法包含一个入参(F)和出参(T)

后期需要的特性:

* 字段指定特殊的CopierConvert

* 名称映射(a_b到aB),注意,必须是静态的.

* Bean到Map

* Map到Bean(这个如果使用静态编译,很难实现)
