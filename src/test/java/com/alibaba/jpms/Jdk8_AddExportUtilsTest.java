package com.alibaba.jpms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

@EnabledOnJre(JRE.JAVA_8)
public class Jdk8_AddExportUtilsTest {

    @Test
    public void testExport() {
        // jdk8 下不抛异常
        Assertions.assertFalse(AddExportUtils.addExport("java.base", "jdk.internal.misc"));
    }

}
