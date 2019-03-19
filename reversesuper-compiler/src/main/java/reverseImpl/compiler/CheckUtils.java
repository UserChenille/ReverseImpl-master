package reverseImpl.compiler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by 陈志远 on 2017/3/9 0009.
 */

public class CheckUtils {


  public static boolean isNull( Object object) {
    return object == null;
  }

  public static boolean equals( Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }

  public static boolean isEmpty( Object[] collection) {
    return collection == null || collection.length == 0;
  }

  public static boolean isEmpty( long[] array) {
    return array == null || array.length == 0;
  }

  public static boolean isEmpty( CharSequence str) {
    return str == null || str.length() == 0;
  }

  /**
   * 建议所有的集合非空检查都 使用该方法
   */
  public static boolean isEmpty( Collection collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * 建议所有的集合非空检查都 使用该方法
   */
  public static boolean isEmpty( Map map) {
    return map == null || map.isEmpty();
  }

  public static boolean isEmpty( Set set) {
    return set == null || set.isEmpty();
  }


}
