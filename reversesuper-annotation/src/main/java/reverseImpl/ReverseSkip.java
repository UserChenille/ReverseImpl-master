package reverseImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用在{@link ReverseImpl}和{@link ReverseExtend}注解的类的内部方法中
 * 表示跳过该方法，不会反向生成抽象方法
 * @author Chenille
 * @date 2018/10/15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReverseSkip {

}
