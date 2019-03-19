package com.model.chenille.reverseImpl;

import static java.lang.annotation.RetentionPolicy.CLASS;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import java.lang.annotation.Retention;
import reverseImpl.ReverseExtend;
import reverseImpl.ReverseOutMode;

/**
 * Created by Chenille on 2018/5/24.
 */
@ReverseExtend(superName = "BaseAdapter",mode = ReverseOutMode.SRC)
public class Adapter extends BaseAdapter{

  public static final int TYPE_A = 0x1;
  public static final int TYPE_B = 0x2;

  @IntDef({TYPE_A, TYPE_B})
  @Retention(CLASS)
  public @interface Type {

  }

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
   * @param type 带注解的固定数据
   * @return 固定返回值
   */
  @Override
  public String reversMethod(@IntRange(from = 0, to = 10) Integer integer, @Type int type) {
    //展示 方法参数注解 反向生成的能力
    return "被反向生成抽象方法的 目标方法-带参数注解";
  }

  /**
   * 保护方法 会被方向生成
   */
  @Override
  protected void reversMethod() {

  }

  /**
   * 私有方法不会反向生成
   */
  private void privateMethod() {
    //不会被反向生成的私有方法
  }

  /**
   * 默认访问修饰的方法 不会被反向
   */
  int getView() {
    return 0;
  }


}
