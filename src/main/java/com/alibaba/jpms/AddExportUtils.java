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
                javaLangAccessField = Class.forName("jdk.internal.access.SharedSecrets")
                        .getDeclaredField("javaLangAccess");
            } catch (Throwable e) {
                // ignore
            }
        }
        return javaLangAccessField;
    }

    private static Method addExports() {
        if (addExportsMethod == null) {
            try {
                addExportsMethod = Class.forName("jdk.internal.access.JavaLangAccess").getDeclaredMethod("addExports",
                        Module.class, String.class);
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
            return addExport(clazz.getModule(), packageToExport);
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
    public static boolean addExport(Module module, String packageToExport) {
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
                return addExport(findModule.get(), packageToExport);
            }
        } catch (Throwable e) {
            // ignore
        }
        return false;
    }

}
