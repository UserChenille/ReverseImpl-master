还在为MVP要写无数的接口而烦恼吗？重复的创建各种父类是不是很辛苦？

都说程序员是世界上最会偷懒的生物；

来试试ReverseImpl吧！

# 作用
一键生成动态代码，支持生成接口/抽象类


# 引用

```java
    引用
    implementation  'com.chenzhiyuan:reverseImpl-annotation:1.1.0'//注解库
    annotationProcessor 'com.chenzhiyuan:reverseImpl-compiler:1.1.0'//代码生成工具库
```

# 使用
下面展示库的动态生成能力

```java
/**
 * Created by Chenille on 2018/3/15.
 * 使用示例，主要针对已经存在的类，rebuild后就可生成对应的接口类。
 * 避免需要手动编写，针对项目重构，抽象等，加快开发。
 * 其中AccountManager接口类是动态生成，它抽象目标类的public方法
 */
@ReverseImpl
public class AccountManagerImpl implements AccountManager {

  /**
   * 被反向生成抽象方法的 目标方法
   *
   * @param input 输入值
   * @return 固定返回
   */
  @Override
  public String reverseMethod(String input) {
    return "被反向生成抽象方法的 目标方法";
  }

  /**
   * 被反向生成抽象方法的 目标方法-带参数注解
   *
   * @param integer 带注解的输入范围
   * @return 固定返回值
   */
  @Override
  public String reversMethod(@IntRange(from = 0, to = 10) Integer integer) {
    //展示 方法参数注解 反向生成的能力
    return "被反向生成抽象方法的 目标方法-带参数注解";
  }

  @Override
  @StringRes
  public int reversMethod() {
    //展示 返回值注解 反向生成的能力
    return android.R.string.ok;
  }

  private String value = "不会被处理非方法信息 变量";

  private void privateMethod() {
    //不会被反向生成的私有方法
  }

}
```
当```@ReverseImpl```注解在目标类上，点击Build-Rebuild，就会动态生成对应的接口类。并且最终的生成代码其实和目标类在相同包下（src包和build包打包时合并）。

可以看到一键Rebuild动态生成代码，省略了对现有代码的抽象public方法的手动操作，效率飞快，而且还有对方法注解、参数注解的处理。

同样还有```@ReverseExtend```注解对目标类生成抽象类。使用类似参见[Adapter](https://github.com/Chenille/ReverseSuper/blob/master/app/src/main/java/com/model/Chenille/reversesuper/Adapter.java)


# 项目背景
在项目重构时，面对一些之前因为某些原因没有抽象的模块代码，直接实现功能而没有抽象出接口，直接对外暴露实现类。

当需要把实现类抽象成接口，对外暴露接口，从而实现对接口的mock隔离、装饰者模式添加功能时或者其他操作。

抽象实现类成接口是我们的目标，一般少量的代码手动就可以完成，但是面对大量的实现类需要抽象出接口,这个工作量就是巨大的。

面对巨大且简单重复的工作，首先我们考虑的就是机器代替实现自动化，自动生成代码。而且重构是一个动态的过程，还需要一定的灵活性，因为实现类的函数名和参数都可能变化。

# 项目实现原理
首先定义我们的目标
* 自动生成代码
* 还要灵活性，不只一次生成。

这就联想到Java提供注解处理器，在项目编译前期，注解器有机会处理代码。利用这个处理器，我们可以实现对现有目标类的扫描，然后根据扫描得到的方法信息，利用工具生成java文件。

同时注解处理器的处理过程发生在每次项目编译前期，能够提供灵活性，只要修改实现类再一次编译就会生成新的动态代码。

> 关于注解的命名Reverse反向:
>
> 一般的写代码是从接口（上层）->实现类（下层）。
>
> 抽象重构时面对实现类（下层），反而生成接口类（上层），所以就是Reverse反向。


# 说明
下面说明一些细节问题
## 关于命名规范

参照阿里巴巴Java开发手册-编程规约-命名风格。
> 14.接口和实现类的命名规则：
>【强制】对于 Service 和 DAO 类，基于 SOA 的理念，暴露出来的服务一定是接口，内部的实现类用 Impl 的后缀与接口区别。
> 正例： CacheServiceImpl 实现 CacheService 接口
>【推荐】如果是形容能力的接口名称，取对应的形容词做接口名 （ 通常是–able 的形式）。
正例： AbstractTranslator 实现 Translatable。

这里有两套规则，对应的到`@ReverseImpl`注解中两个可选项。其中默认实现【强制】的Impl命名风格。
```java
/**
 * 反向生成高层接口类注解
 * 被标记的类在编译时，会在build目录下的同级包 生成接口类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReverseImpl {

  /**
   * 被标注类的名称后缀 默认是命名是 Impl
   * 默认规则 如：AccountMangerImpl(标记类)->AccountManager(生成的接口)
   * 也可以根据实际实现类的后缀修改。
   * 严格检查参数：必须是被标记类的后缀。
   */
  String targetSuffix() default "Impl";

  /**
   * 指定 生成接口名称
   * 默认：默认该字段不作用，通过{@link #targetSuffix}裁剪约定后缀的标记类名称生成接口
   * 非空输入：指定生成的接口名称，忽略后缀检查
   * 如：AbstractTranslator->Translatable
   */
  String interfaceName() default "";
 }

```

> 6.【强制】抽象命名使用Abstract或Base开头

这里也是两套规则，对应`@ReverseExtend`注解，其中默认实现`Abstract`开头命名风格
```java
/**
 * Created by Chenille on 2018/5/24.
 * 反向生成高层抽象类注解
 * 被标记的类在编译时，会在build目录下的同级包 生成抽象类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReverseExtend {


  /**
   * 生成抽象类名的前缀 默认是Abstract
   * 默认规则：Adapter(标记类)->AbstractAdapter(生成的抽象类)
   * 也可以传入参数指定生成的抽象类名的前缀
   * 如：传入Base，作用Adapter(标记类)->BaseAdapter(生成的抽象类)
   */
  String superPrefix() default "Abstract";

  /**
   * 指定 生成的抽象类名称
   * 默认：默认该字段不作用，通过{@link #superName()}直接拼接标记类和前缀生成抽象类
   * 非空输入：指定生成的抽象类名称
   * 如：传入BaseAdapter, MyAdapter(标记类)->BaseAdapter(生成的抽象类)
   */
  String superName() default "";

}
```

## 关于代码输出模式

重构是一个渐进的过程，从最初的实现类反向生成接口类。接口类可能会修改。动态的反向可以带来便利。只要添加/修改实现类方法参数/返回值以及它们的注解，rebuild就会马上生成接口。一次修改（否则就要修改接口类和实现类的方法，两次修改）。

当重构完成或者更高层抽象分离出来（比如动态代理，直接抽象方法内部实现逻辑），我们的高层类最终确定，就不需要build项目时每次都动态生成代码，每次build动态生成反而可能拖慢了项目的编译时间。

就可以修改`@ReverseImpl/@ReverseExtend`默认的代码输出mode模式，
```java
/**
   * 指定代码输出模式 默认build模式
   * @return
   */
  ReverseOutMode mode() default ReverseOutMode.Build;
```

修改为`ReverseOutMode.Src`模式，代码会输出到源代码src目录同名包下。

# 最后
其实针对简单少量的实现类需要抽象成接口，可以通过AS的`Extract`功能，通过窗口选择生成代码。

但是AS目前只提供
- 只提供实现类提取接口，没有提取抽象类功能。
- 通过窗口选择，还是慢。不够灵活，只是一次生成。

# 项目灵感
源自大名鼎鼎的[butterknife](https://github.com/JakeWharton/butterknife/tree/master/butterknife-compiler)对注解处理器的应用。

# future
其实按照这个思路，可以解决`MVP`架构中`V/P`两层接口方法太多时带来的麻烦。
只要我们的思路清晰而且是以一个人分工负责一个`V/P`对应模块，可以先写P层业务代码，然后一键生成抽象接口暴露public方法，提供给V层使用。而且每次P层方法新增/修改参数都动态生成，避免两次修改的麻烦。
