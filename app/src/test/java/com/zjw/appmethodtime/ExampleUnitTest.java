package com.zjw.appmethodtime;

import org.junit.Test;

import java.io.File;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testClassOperate(){
        ClassPool pool = ClassPool.getDefault();
        String fullClass = "/Users/hana/StudioProjects/AppMethodTime/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes/com/zjw/appmethodtime/MyApplication.class";

        String parentPath = new File(fullClass).getParentFile().getAbsolutePath();
        String className = "com.zjw.appmethodtime.MyApplication";
        ClassPath classPath = null;
        try {
            classPath = pool.appendClassPath(parentPath);
            CtClass c = pool.getCtClass(className);
            if (c.isFrozen()) {
                c.defrost();
            }
            CtMethod[] methods = c.getDeclaredMethods();
            for (CtMethod method : methods){
                System.out.println(method.getName() + method.getMethodInfo().getCodeAttribute().toString());
            }
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            if (classPath != null) {
                pool.removeClassPath(classPath);
            }
        }
    }
}