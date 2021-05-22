/*
 * Copyright 2021 ccy.All Rights Reserved
 */
package com.runningcode.noadapter.compiler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.runningcode.noadapter.annotation.IVHRegistry;
import com.runningcode.noadapter.annotation.ViewHolder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

@AutoService(Processor.class)
public class ViewHolderProcessor extends AbstractProcessor {
    private static final String PKG = "com.runningcode.noadapter.compiler";
    private static final String OPTION_MODULE_NAME = "moduleName";
    @Deprecated
    private static final int MODULE_SPACE = 1000; // 每个module持有的viewholder数量
    private Elements elementUtils;
    private boolean first = true;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ViewHolder.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        //        ClassName hashMap = ClassName.get("java.util", "HashMap");
        if (first) {
            first = false;
            System.out.println("============Enter ViewHolderProcessor===========");
            Messager messager = processingEnv.getMessager();
            String moduleName = processingEnv.getOptions().get(OPTION_MODULE_NAME);
            if (moduleName == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "No option " + OPTION_MODULE_NAME +
                        " passed to annotation processor");
                return false;
            }

            // 防止hashCode出现负数
            int moduleIdx = (moduleName.hashCode() & Integer.MAX_VALUE) + MODULE_SPACE;
            // 属性
            FieldSpec size =
                    FieldSpec.builder(TypeName.INT, "size",
                            Modifier.PRIVATE, Modifier.STATIC)
                            .build();

            //        FieldSpec modelList =
            //                FieldSpec.builder(ParameterizedTypeName.get(List.class, Class.class),
            //                "modelList",
            //                        Modifier.PRIVATE, Modifier.STATIC)
            //                        .initializer("new $T()", ArrayList.class)
            //                        .build();

            FieldSpec dataTypeMaps =
                    FieldSpec.builder(
                            ParameterizedTypeName.get(Map.class, Class.class, Integer.class),
                            "dataTypeMaps",
                            Modifier.PRIVATE, Modifier.STATIC)
                            .initializer("new $T()", HashMap.class)
                            .build();

            FieldSpec vhTypeMaps =
                    FieldSpec.builder(
                            ParameterizedTypeName.get(Map.class, Integer.class, Class.class),
                            "vhTypeMaps",
                            Modifier.PRIVATE, Modifier.STATIC)
                            .initializer("new $T()", HashMap.class)
                            .build();

            FieldSpec multiTypeMap =
                    FieldSpec.builder(ParameterizedTypeName.get(Map.class, Class.class,
                            String.class),
                            "multiTypeMap",
                            Modifier.PRIVATE, Modifier.STATIC)
                            .initializer("new $T()", HashMap.class)
                            .build();
            // 泛型嵌套
            //        ParameterizedTypeName subType = ParameterizedTypeName.get(List.class, Integer
            //        .class);
            //        FieldSpec multiValueMap =
            //                FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
            //                        ClassName.get(Class.class), subType),
            //                        "multiValueMap",
            //                        Modifier.PRIVATE, Modifier.STATIC)
            //                        .initializer("new $T()", HashMap.class)
            //                        .build();

            FieldSpec multiValueMap =
                    FieldSpec
                            .builder(ParameterizedTypeName.get(Map.class, Class.class, int[].class),
                                    "multiValueMap",
                                    Modifier.PRIVATE, Modifier.STATIC)
                            .initializer("new $T()", HashMap.class)
                            .build();

            Set<? extends Element> elements =
                    roundEnvironment.getElementsAnnotatedWith(ViewHolder.class);
            // 构造方法
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(initCode(elements, moduleIdx))
                    .build();

            // getGeneric方法
            MethodSpec getGeneric = MethodSpec.methodBuilder("getGeneric")
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(Class.class, "vhClass")
                    .returns(Class.class)
                    .addCode(getGenericCode())
                    .build();

            // getItemViewType
            MethodSpec getItemViewType = MethodSpec.methodBuilder("getItemViewType")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Object.class, "data")
                    .returns(int.class)
                    .addCode(getItemViewTypeCode(moduleIdx))
                    .build();

            // indexOf
            MethodSpec indexOf = MethodSpec.methodBuilder("indexOf")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Class.class, "cls")
                    .addParameter(int.class, "anInt")
                    .returns(int.class)
                    .addCode(indexOfCode())
                    .build();

            // getVHClass
            MethodSpec getVHClass = MethodSpec.methodBuilder("getVHClass")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "type")
                    .returns(Class.class)
                    .addStatement(CodeBlock.of("return vhTypeMaps.get(type)"))
                    .build();
            // 类
            TypeSpec clazz = TypeSpec.classBuilder("ViewHolderRegistry_" + moduleName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(IVHRegistry.class)
                    .addField(size)
                    //                .addField(vhList)
                    //                .addField(modelList)
                    .addField(dataTypeMaps)
                    .addField(vhTypeMaps)
                    .addField(multiTypeMap)
                    .addField(multiValueMap)
                    // 新版本去掉，改成构造方法
                    //                .addStaticBlock(initCode(elements))
                    .addMethod(constructor)
                    .addMethod(getGeneric)
                    .addMethod(indexOf)
                    .addMethod(getItemViewType)
                    .addMethod(getVHClass)
                    .build();
            try {
                System.out.println("============APT开始创建文件===========");
                JavaFile build = JavaFile.builder(PKG, clazz)
                        .indent("    ")
                        .addFileComment("\nAutomatically generated file. DO NOT MODIFY\n")
                        .skipJavaLangImports(false)
                        .build();
//                System.out.println(build.toString());
                build.writeTo(processingEnv.getFiler());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("not first process! IGNORE!");
        }
        return false;
    }

    private CodeBlock indexOfCode() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("int[] ints = multiValueMap.get(cls)");
        builder.beginControlFlow("for (int i = 0; i < ints.length; i++)");
        builder.beginControlFlow("if (ints[i] == anInt)");
        builder.addStatement("return i");
        builder.endControlFlow();
        builder.endControlFlow();
        builder.addStatement("return -1");
        return builder.build();
    }

    /**
     * public static Integer getItemViewType(Object data) {
     * Class<?> cls = data.getClass();
     * int[] values = multiValueMap.get(cls);
     * if (values != null && multiTypeMap.get(cls) != null) {
     * try {
     * Field field = cls.getDeclaredField(multiTypeMap.get(cls));
     * field.setAccessible(true);
     * int anInt = field.getInt(data);
     * int i = indexOf(cls, anInt);
     * if (i != -1) {
     * return i * size;
     * } else {
     * System.err.println("find type value by " + cls + " is error.");
     * }
     * <p>
     * } catch (NoSuchFieldException e) {
     * e.printStackTrace();
     * } catch (IllegalAccessException e) {
     * e.printStackTrace();
     * }
     * }
     * return modelList.indexOf(cls);
     * }
     *
     * @param moduleIdx
     * @return
     */
    private CodeBlock getItemViewTypeCode(int moduleIdx) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("Class<?> cls = data.getClass()");
        builder.addStatement("$T values = multiValueMap.get(cls)", int[].class);

        builder.beginControlFlow("if (values != null && multiTypeMap.get(cls) != null)");
        builder.add(getTryCatchCode(moduleIdx));
        builder.endControlFlow();
        builder.addStatement("return dataTypeMaps.get(cls) == null ? -1 : dataTypeMaps.get(cls)");
        return builder.build();
    }

    private CodeBlock getTryCatchCode(int moduleIdx) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.beginControlFlow("try");
        builder.addStatement("$T field = cls.getDeclaredField(multiTypeMap.get(cls))", Field.class);
        builder.addStatement("field.setAccessible(true)");
        builder.addStatement("int anInt = field.getInt(data)");
        builder.addStatement("int i = indexOf(cls, anInt) + 1");
        builder.beginControlFlow("if (i != 0)");
        builder.addStatement("return i + size + $L", moduleIdx);
        builder.nextControlFlow("else");
        builder.addStatement("System.err.println(getClass().getSimpleName()+\" find type value by"
                + " \" + cls + \" is error"
                + ".\")");
        builder.endControlFlow();
        builder.nextControlFlow("catch ($T e)", NoSuchFieldException.class);
        builder.addStatement("e.printStackTrace()");
        builder.nextControlFlow("catch ($T e)", IllegalAccessException.class);
        builder.addStatement("e.printStackTrace()");
        builder.endControlFlow();
        return builder.build();
    }

    private CodeBlock getGenericCode() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$T type = vhClass.getGenericSuperclass()", Type.class);
        builder.beginControlFlow("if(type instanceof $T)", ParameterizedType.class)
                .addStatement("$T parameterizedType = ($T) type", ParameterizedType.class,
                        ParameterizedType.class)
                .addStatement("Type[] types = parameterizedType.getActualTypeArguments()")
                .beginControlFlow("if (types.length > 0) ")
                .addStatement("return (Class<?>) types[0]")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return null");
        return builder.build();
    }

    private CodeBlock initCode(Set<? extends Element> elements, int moduleIdx) {

        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("size = $L", elements.size());
        List<Element> multiValueList = new ArrayList<>();
        int i = 0;
        for (Element element : elements) {
            if (element.getAnnotation(ViewHolder.class).type() > 0) {
                multiValueList.add(element);
            } else {
                int idx = moduleIdx + i;
                builder.addStatement("vhTypeMaps.put($L,$L.class)", idx,
                        element.asType());
                builder.addStatement("dataTypeMaps.put(getGeneric($L.class),$L)",
                        element.asType(), idx);
                i++;
            }
        }

        i = 0;
        // 一对多
        if (multiValueList.size() > 0) {
            Map<String, List<Integer>> map = new HashMap<>();
            for (Element element : multiValueList) {
                builder.addStatement("vhTypeMaps.put($L + $L + size,$L.class)", i + 1, moduleIdx,
                        element.asType());
                ViewHolder annotation = element.getAnnotation(ViewHolder.class);

                String cls = "";
                try {
                    // Get The Annotation's class filed.
                    // Yes, this is a total hack of a solution,
                    // and I'm not sure why the API developers decided to go this direction with
                    // the annotation processor feature.
                    // However, There is a number of people implement this (including myself),
                    annotation.cls();
                } catch (MirroredTypeException mte) {
                    cls = mte.getTypeMirror().toString();
                }

                builder.addStatement("multiTypeMap.put($L.class,$S)", cls, annotation.filed());

                if (map.get(cls) == null) {
                    List<Integer> list = new ArrayList<>();
                    map.put(cls, list);
                }

                map.get(cls).add(annotation.type());
                System.out.println(
                        "multiValueList -->" + element.toString() + ":" + map.get(element));

                i++;
            }

            Set<Map.Entry<String, List<Integer>>> entries = map.entrySet();
            for (Map.Entry<String, List<Integer>> entry : entries) {
                builder.addStatement("multiValueMap.put($L.class,new int[]$L)", entry.getKey(),
                        listToStr(entry.getValue()));
            }
        }

        //        builder.beginControlFlow("for (int i = 0; i < vhList.size(); i++)")
        //                .addStatement("vhTypeMaps.put(i, vhList.get(i))")
        //                .addStatement("vhModelMaps.put(modelList.get(i), i)")
        //                .endControlFlow();

        return builder.build();
    }

    private String listToStr(List<Integer> values) {
        StringBuilder sb = new StringBuilder("{");
        for (Integer value : values) {
            sb.append(",").append(value);
        }
        sb.append("}");
        return sb.toString().replaceFirst(",", "");
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }
}