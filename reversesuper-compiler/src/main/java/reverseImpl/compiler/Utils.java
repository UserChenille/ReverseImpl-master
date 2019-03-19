package reverseImpl.compiler;

import com.google.auto.common.MoreElements;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

/**
 * Created by Chenille on 2018/6/7.
 */
class Utils {

  static PackageElement getPackageElement(Element elementItem) {
    return MoreElements
        .asPackage(MoreElements.asType(elementItem).getEnclosingElement());
  }

  static boolean checkExistReverse(PackageElement packageElement, String targetClassName) {
    List<? extends Element> existElements = packageElement.getEnclosedElements();
    for (Element existElement : existElements) {
      if (targetClassName.equals(existElement.getSimpleName().toString())) {
        //查到已经存在 生成类 不再处理
        return true;
      }
    }
    return false;
  }

  private static final String DATE_FORMAT_LOG_FILE = "yyyy/MM/dd HH:mm:ss";

  static String getNowTime() {
    return new SimpleDateFormat(DATE_FORMAT_LOG_FILE,
        Locale.CHINA).format(new Date());
  }

}
