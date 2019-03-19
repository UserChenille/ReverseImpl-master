package com.model.chenille.reverseImpl;

import android.support.annotation.IntRange;
import android.support.annotation.StringRes;

import java.io.FileNotFoundException;
import java.util.zip.ZipException;

import reverseImpl.ReverseImpl;

@ReverseImpl
public class ABCManagerImpl implements ABCManager {


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

  @Override
  public void reversMethod(String input) throws FileNotFoundException, ZipException {
    throw new FileNotFoundException("");
  }

  private String value = "不会被处理非方法信息 变量";

  private void privateMethod() {
    //不会被反向生成的私有方法
  }
}
