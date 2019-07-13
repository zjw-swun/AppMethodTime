package com.zjw.customlint;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.util.UastExpressionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Omooo
 * Date:2019-07-04
 * Desc:Avoid using Log class directly
 */
@SuppressWarnings("UnstableApiUsage")
public class MyLogDetector extends Detector implements Detector.UastScanner {

    public static final Issue ISSUE = Issue.create(
            //自定义lint id尽量不要和android 原有Lint重名
            "MyLogDetector",
            "Log Usage",
            "Please use the unified LogUtil class!",
            Category.CORRECTNESS,
            6,
            Severity.FATAL,
            //Scope.JAVA_FILE_SCOPE ：kotlin 文件也能显示这个提示
            new Implementation(MyLogDetector.class, Scope.JAVA_FILE_SCOPE)
    );

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(UCallExpression.class);
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull JavaContext context) {
        return new LogHandler(context);
    }

    class LogHandler extends UElementHandler {

        private JavaContext context;

        LogHandler(JavaContext context) {
            this.context = context;
        }

        @Override
        public void visitCallExpression(@NotNull UCallExpression node) {
            if (!UastExpressionUtils.isMethodCall(node)) return;
            if (node.getReceiver() != null
                    && node.getMethodName() != null) {
                String methodName = node.getMethodName();
                if (methodName.equals("i")
                        || methodName.equals("d")
                        || methodName.equals("e")
                        || methodName.equals("v")
                        || methodName.equals("w")
                        || methodName.equals("wtf")) {
                    PsiMethod method = node.resolve();
                    if (context.getEvaluator().isMemberInClass(method, "android.util.Log")) {
                        reportAllocation(context, node);
                    }
                }
            }
        }
    }

    private void reportAllocation(JavaContext context, UCallExpression node) {
        context.report(ISSUE,
                node,
                context.getLocation(node),
                "\u21E2 Avoid using *Log* class directly"
        );
    }
}
