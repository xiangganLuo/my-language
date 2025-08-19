package com.lxg.sema;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 简单的诊断信息收集器：记录语义错误，统一输出。
 * @author xiangganluo
 */
public class Diagnostics {
    private final List<String> errors = new ArrayList<>();

    public void error(String message) {
        errors.add(message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void printAll(PrintStream out) {
        for (String e : errors) {
            out.println("[ERROR] " + e);
        }
    }
} 