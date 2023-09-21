package com.alibaba.jpms;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 
 * @see jdk.internal.access.JavaLangAccess
 * @author hengyunabc 2023-09-20
 *
 */
public class AddExportUtils {

    private static Field javaLangAccessField;

    private static Method addExportsMethod;

    private static Field javaLangAccess() {
        if (javaLangAccessField == null) {
            try {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName("jdk.internal.access.SharedSecrets");
                } catch (Throwable e) {
                    // 有某些版本 JDK package 是下面这个
                    // https://github.com/openjdk/jdk/commit/9ffe7e1205ea42ffccc9622b3e1c5436cc9898f5
                    clazz = Class.forName("jdk.internal.misc.SharedSecrets");
                }
                javaLangAccessField = clazz.getDeclaredField("javaLangAccess");
            } catch (Throwable e) {
                // ignore
            }
        }
        return javaLangAccessField;
    }

    private static Method addExports() {
        if (addExportsMethod == null) {
            try {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName("jdk.internal.access.JavaLangAccess");
                } catch (Throwable e) {
                    // 有某些版本 JDK package 是下面这个
                    // https://github.com/openjdk/jdk/commit/9ffe7e1205ea42ffccc9622b3e1c5436cc9898f5
                    clazz = Class.forName("jdk.internal.misc.JavaLangAccess");
                }
                try {
                    addExportsMethod = clazz.getDeclaredMethod("addExports", Module.class, String.class);
                } catch (Throwable e) {
                    // 有某些版本 JDK 没有 addExports 函数，改用 addExportsToAllUnnamed
                    // https://github.com/openjdk/jdk/commit/9ffe7e1205ea42ffccc9622b3e1c5436cc9898f5
                    addExportsMethod = clazz.getDeclaredMethod("addExportsToAllUnnamed", Module.class, String.class);
                }
            } catch (Throwable e) {
                // ignore
            }
        }
        return addExportsMethod;
    }

    /**
     * 获取 clazz 所在的 module，再无条件 export module 里的 package
     * 
     * @see jdk.internal.access.JavaLangAccess#addExports
     * 
     * @param clazz
     * @param packageToExport
     * @return
     */
    public static boolean addExport(Class<?> clazz, String packageToExport) {
        try {
            return exportPackage(clazz.getModule(), packageToExport);
        } catch (Throwable e) {
            // ignore
        }
        return false;
    }

    /**
     * 无条件 export module 里的 package
     * 
     * @see jdk.internal.access.JavaLangAccess#addExports
     */
    public static boolean exportPackage(Module module, String packageToExport) {
        try {
            Lookup implLookup = UnsafeUtils.implLookup();
            Object o = implLookup.unreflectVarHandle(javaLangAccess()).get();
            MethodHandle addExportsMethodHandle = implLookup.unreflect(addExports());

            addExportsMethodHandle.invoke(o, module, packageToExport);
            return true;
        } catch (Throwable e) {
            // ignore
        }
        return false;
    }

    /**
     * 无条件 export module 里的 package
     * 
     * @see jdk.internal.access.JavaLangAccess#addExports
     */
    public static boolean addExport(String moduleName, String packageToExport) {
        try {
            Optional<Module> findModule = ModuleLayer.boot().findModule(moduleName);
            if (findModule.isPresent()) {
                return exportPackage(findModule.get(), packageToExport);
            }
        } catch (Throwable e) {
            // ignore
        }
        return false;
    }

}
