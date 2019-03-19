package reverseImpl.compiler;

import static reverseImpl.compiler.Utils.checkExistReverse;
import static reverseImpl.compiler.Utils.getPackageElement;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import reverseImpl.ReverseExtend;
import reverseImpl.ReverseImpl;
import reverseImpl.ReverseOutMode;
import reverseImpl.ReverseSkip;

/**
 * Created by Chenille on 2017/6/21.
 *
 * 注解生成代码：生成目录在项目build包下，和目标类同级
 *
 * 简单示例说明：{@link <a href="http://blog.stablekernel.com/the-10-step-guide-to-annotation-processing-in-android-studio">}
 * 引入{@link <a href="https://github.com/square/javapoet">}
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ReverseProcessor extends AbstractProcessor {

  private static final String DEFAULT_SRC_PATH = "./app/src/main/java";

  private Filer writeFiler;
  private File writeFile = new File(DEFAULT_SRC_PATH);


  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    writeFiler = processingEnv.getFiler();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
      types.add(annotation.getCanonicalName());
    }
    return types;
  }

  private Set<Class<? extends Annotation>> getSupportedAnnotations() {
    Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
    annotations.add(ReverseImpl.class);
    annotations.add(ReverseExtend.class);
    return annotations;
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

    Set<? extends Element> elementImplSet = roundEnvironment
        .getElementsAnnotatedWith(ReverseImpl.class);//得到被注解目标类们

    Set<? extends Element> elementSuperSet = roundEnvironment
        .getElementsAnnotatedWith(ReverseExtend.class);//得到被注解目标类们

    List<OutWriteCommand> commands = new ArrayList<>();

    //构造接口代码 遍历被注解的目标类 获取信息
    for (Element elementItem : elementImplSet) {

      //获取输出模式
      ReverseOutMode mode = elementItem.getAnnotation(ReverseImpl.class).mode();
      //获取包信息
      PackageElement packageElement = getPackageElement(elementItem);
      //构建接口build
      Builder buildInterface = buildInterface(elementItem, packageElement, mode);
      if (buildInterface == null) {
        continue;
      }

      //获取有效的方法集合
      List<MethodSpec> validMethods = getValidMethods(elementItem);
      if (!CheckUtils.isEmpty(validMethods)) {
        buildInterface.addMethods(validMethods);
      }

      //完成新接口类的构建
      TypeSpec typeSpec = buildInterface.build();

      commands.add(new OutWriteCommand() {
        @Override
        public void execute() throws IOException {

          JavaFile javaFile = JavaFile
              .builder(packageElement.getQualifiedName().toString(),
                  typeSpec).build();
          if (mode == ReverseOutMode.Build) {
            javaFile.writeTo(writeFiler);
          } else if (mode == ReverseOutMode.SRC) {
            javaFile.writeTo(writeFile);
          }
        }
      });

    }

    //构造抽象类代码
    for (Element elementItem : elementSuperSet) {

      ReverseOutMode mode = elementItem.getAnnotation(ReverseExtend.class).mode();
      PackageElement packageElement = getPackageElement(elementItem);

      Builder buildSuper = buildSuper(elementItem, packageElement, mode);

      if (buildSuper == null) {
        continue;
      }

      List<MethodSpec> validMethods = getValidMethods(elementItem);
      if (!CheckUtils.isEmpty(validMethods)) {
        buildSuper.addMethods(validMethods);
      }
      TypeSpec typeSpec = buildSuper.build();

      commands.add(new OutWriteCommand() {
        @Override
        public void execute() throws IOException {
          JavaFile javaFile = JavaFile
              .builder(packageElement.getQualifiedName().toString(),
                  typeSpec).build();
          if (mode == ReverseOutMode.Build) {
            javaFile.writeTo(writeFiler);
          } else if (mode == ReverseOutMode.SRC) {
            javaFile.writeTo(writeFile);
          }
        }
      });
    }

    try {
      //依次生成代码
      for (OutWriteCommand item : commands) {
        item.execute();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  private TypeSpec.Builder buildSuper(Element element, PackageElement packageElement,
      ReverseOutMode mode) {

    String superName = getSuperName(element);

    TypeSpec.Builder classSpecBuild = TypeSpec.classBuilder(superName)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .addJavadoc("Created by @$L on $L \n", ReverseExtend.class.getSimpleName(),
            Utils.getNowTime())
        .addJavadoc("该抽象类由{@link $L}类自动生成\n", element.getSimpleName().toString());

    if (mode == ReverseOutMode.SRC) {
      //src模式 检查同名src包下是否存在 目标类
      if (checkExistReverse(packageElement, superName)) {
        return null;
      }
      classSpecBuild
          .addJavadoc("注：同名包下，存在$L就不会再次生成代码\n", superName);
    } else if (mode == ReverseOutMode.Build) {
      classSpecBuild
          .addJavadoc("注：每次Rebuild都会重新生成代码\n");
    } else {
      return null;
    }

    return classSpecBuild;
  }

  private TypeSpec.Builder buildInterface(Element element, PackageElement packageElement,
      ReverseOutMode mode) {

    String interfaceName = getInterfaceName(element);

    TypeSpec.Builder classSpecBuild =
        TypeSpec.interfaceBuilder(interfaceName)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Created by @$L on $L \n", ReverseImpl.class.getSimpleName(),
                Utils.getNowTime())
            .addJavadoc("该接口由{@link $L}类自动生成\n", element.getSimpleName().toString());

    if (mode == ReverseOutMode.SRC) {
      //src模式 检查同名src包下是否存在 目标类
      if (checkExistReverse(packageElement, interfaceName)) {
        return null;
      }
      classSpecBuild
          .addJavadoc("注：同名包下，存在$L就不会再次生成代码\n", interfaceName);
    } else if (mode == ReverseOutMode.Build) {
      //build模式 每次都生成代码
      classSpecBuild
          .addJavadoc("注：每次Rebuild都会重新生成代码\n");
    } else {
      return null;
    }

    return classSpecBuild;
  }


  private String getInterfaceName(Element element) {

    String targetInterfaceName = element.getAnnotation(ReverseImpl.class)
        .interfaceName();//获取注解指定的接口名称

    if (!CheckUtils.isEmpty(targetInterfaceName)) {
      //指定命名 直接使用值
      return targetInterfaceName;
    }

    String suffix = element.getAnnotation(ReverseImpl.class).targetSuffix();

    String targetClassName = element.getSimpleName().toString();//获取当前标记类名
    int suffixLength = suffix.length();
    int targetClassNameLength = targetClassName.length();

    if (CheckUtils.isEmpty(suffix)) {
      error(element, "@ReverseImpl %s注解的suffix后缀值不能为空，否则导致生成同名接口",
          targetClassName);
      throw new IllegalArgumentException(targetClassName);
    }

    if (targetClassNameLength == suffixLength) {
      error(element, "@ReverseImpl 命名不符合规范 %s类名称太短无法得到有效名称", targetClassName);
      throw new IllegalArgumentException(targetClassName);
    }

    String targetClassSuffix = targetClassName.substring(targetClassNameLength - suffixLength);
    if (!targetClassSuffix.equals(suffix)) {
      error(element, "@ReverseImpl 命名不符合规范 %s类应当以%s结尾", targetClassName, suffix);
      throw new IllegalArgumentException(targetClassName);
    }

    return targetClassName.substring(0, targetClassNameLength - suffixLength);
  }


  private String getSuperName(Element element) {

    String targetSuperName = element.getAnnotation(ReverseExtend.class).superName();

    if (!CheckUtils.isEmpty(targetSuperName)) {
      return targetSuperName;
    }

    String targetClassName = element.getSimpleName().toString();//获取当前标记类名
    String superPrefix = element.getAnnotation(ReverseExtend.class).superPrefix();

    if (CheckUtils.isEmpty(superPrefix)) {
      error(element, "@ReverseExtend %s注解的superPrefix值不能为空，否则导致生成同名抽象类",
          targetClassName);
      throw new IllegalArgumentException(targetClassName);
    }

    return superPrefix + targetClassName;
  }

  /**
   * 根据传入的元素 构造接口类
   */
  private List<MethodSpec> getValidMethods(Element element) {

    ArrayList<MethodSpec> methodSpecs = new ArrayList<>();

    //得到类内部全部定义元素（包括 域、静态方法、对象方法等）
    List<? extends Element> elementMethods = MoreElements.asType(element).getEnclosedElements();

    for (Element elementItem : elementMethods) {
      if (elementItem.getKind() != ElementKind.METHOD) {
        //跳过非方法
        continue;
      }

      ExecutableElement executableElement = MoreElements.asExecutable(elementItem);
      Set<Modifier> modifiers = executableElement.getModifiers();

      if (modifiers.isEmpty()) {
        //默认修饰符 modifiers表示为空 跳过
        continue;
      }

      if (modifiers.contains(Modifier.PRIVATE)) {
        //私有方法 跳过
        continue;
      }

      if (modifiers.contains(Modifier.STATIC)) {
        //修饰符中 包含static 即静态方法 跳过
        continue;
      }

      if (modifiers.contains(Modifier.ABSTRACT)) {
        //抽象方法 跳过
        continue;
      }

      //Super类肯定有2个修饰符
      Modifier[] modifierList = new Modifier[2];

      for (Modifier modifier : modifiers) {
        if (Modifier.SYNCHRONIZED == modifier) {
          //修饰符synchronized Super类方法不能修饰
          continue;
        }

        if (Modifier.FINAL == modifier) {
          //修饰符final Super类方法不能修饰
          continue;
        }

        modifierList[0] = modifier;
      }

      //固定的修饰符
      modifierList[1] = Modifier.ABSTRACT;

      //添加接口抽象方法
      MethodSpec methodSpec = buildInterfaceMethod(executableElement, modifierList);
      if (methodSpec != null) {
        methodSpecs.add(methodSpec);
      }
    }

    return methodSpecs;

  }

  private MethodSpec buildInterfaceMethod(ExecutableElement executableElement,
      Modifier[] modifiers) {

    MethodSpec.Builder methodSpecBuild =
        MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
            .addModifiers(modifiers);

    //添加方法返回值的 注解
    for (AnnotationMirror item : executableElement.getAnnotationMirrors()) {
      TypeElement typeElement = MoreElements.asType(item.getAnnotationType().asElement());
      //跳过实现类的Overrider重写注解
      if (Override.class.getSimpleName().equals(typeElement.getSimpleName().toString())) {
        continue;
      }

      if (ReverseSkip.class.getSimpleName().equals(typeElement.getSimpleName().toString())) {
        return null;
      }

      methodSpecBuild.addAnnotation(AnnotationSpec.get(item));
    }

    //检查是否 返回值为void的方法
    if (executableElement.getReturnType().getKind() == TypeKind.VOID) {
      methodSpecBuild.returns(void.class);
    } else {
      methodSpecBuild.returns(ClassName.get(executableElement.getReturnType()));
    }

    //添加方法参数 注解
    for (VariableElement variableElement : executableElement.getParameters()) {
      methodSpecBuild.addParameter(getParameterSpace(variableElement));
    }

    for (TypeMirror typeMirror : executableElement.getThrownTypes()) {
      methodSpecBuild.addException(ClassName.get(typeMirror));
    }

    return methodSpecBuild.build();
  }


  /**
   * 根据变量元素 构建参数 包括修饰符和参数注解
   */
  private ParameterSpec getParameterSpace(VariableElement element) {
    TypeName type = TypeName.get(element.asType());
    String name = element.getSimpleName().toString();

    ParameterSpec.Builder builder =
        ParameterSpec.builder(type, name).addModifiers(element.getModifiers());

    List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();

    //复制参数的注解
    for (AnnotationMirror itemAnnotation : annotationMirrors) {
      builder.addAnnotation(AnnotationSpec.get(itemAnnotation));
    }

    return builder.build();
  }

  private void error(Element element, String message, Object... args) {
    printMessage(Kind.ERROR, element, message, args);
  }

  private void printMessage(Kind kind, Element element, String message, Object[] args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnv.getMessager().printMessage(kind, message, element);
  }


}
