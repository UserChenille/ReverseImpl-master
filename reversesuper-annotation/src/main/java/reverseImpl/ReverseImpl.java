package reverseImpl;

/**
 * Created by Chenille on 2018/3/15.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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


  /**
   * 指定代码输出模式 默认build模式
   * @return
   */
  ReverseOutMode mode() default ReverseOutMode.Build;

}
