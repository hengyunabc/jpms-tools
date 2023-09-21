package com.alibaba.jpms;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

@EnabledForJreRange(min = JRE.JAVA_9)
public class AddExportUtilsTest {

    @Test
    public void testExport() throws ClassNotFoundException {
        Assertions.assertFalse(
                Class.forName("jdk.internal.access.JavaLangAccess").getModule().isExported("jdk.internal.misc"));

        AddExportUtils.addExport("java.base", "jdk.internal.misc");

        Assertions.assertTrue(
                Class.forName("jdk.internal.access.JavaLangAccess").getModule().isExported("jdk.internal.misc"));
    }

    @Test
    public void testVersion() throws IOException {
        try (InputStream inputStream = AddExportUtils.class.getClassLoader()
                .getResourceAsStream(AddExportUtils.class.getName().replace('.', '/') + ".class");

                DataInputStream dis = new DataInputStream(inputStream);) {
            // 跳过前 4 个字节（magic number）
            dis.skipBytes(4);

            // 读取主要版本号
            int minor = dis.readUnsignedShort();
            System.out.println("minor version: " + minor);

            int majorVersion = dis.readUnsignedShort();
            System.out.println("major version: " + majorVersion);

            Assertions.assertEquals(majorVersion, 52);
        }

    }
}
