package com.zjw.customlint;

import com.android.tools.lint.checks.LogDetector;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


/**
 * Created by Omooo
 * Date:2019-07-04
 */
@SuppressWarnings("UnstableApiUsage")
public class CustomIssueRegistry extends IssueRegistry {

    @NotNull
    @Override
    public List<Issue> getIssues() {

        return Arrays.asList(
                //自定义lint 尽量不要和android 原有Lint重名 例如
                //com.android.tools.lint.checks.LogDetector
                MyLogDetector.ISSUE
           );
    }

    @Override
    public int getApi() {
        return ApiKt.CURRENT_API;
    }
}
