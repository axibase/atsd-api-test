package com.axibase.tsd.api.model.sql.function.interpolate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FillMode {
    private static final Set<String> STRING_VALUE_SET = new HashSet<>();

    public static final FillMode NONE = new FillMode("NONE");
    public static final FillMode NAN = new FillMode("NAN");
    public static final FillMode EXTEND = new FillMode("EXTEND");

    public static final FillMode TRUE = new FillMode("TRUE");
    public static final FillMode YES = new FillMode("YES");
    public static final FillMode FALSE = new FillMode("FALSE");
    public static final FillMode NO = new FillMode("NO");

    private String mode;

    private FillMode(String mode, boolean extra) {
        this.mode = mode;
        if (!extra) {
            STRING_VALUE_SET.add(mode);
        }
    }

    private FillMode(String mode) {
        this(mode, false);
    }

    public static FillMode value(Double val) {
        String mode;
        if (val == null) {
            mode = "NAN";
        } else {
            mode = "VALUE " + String.valueOf(val);
        }
        return new FillMode(mode, true);
    }

    public static List<String> stringValues() {
        List<String> result = new ArrayList<>();
        result.addAll(STRING_VALUE_SET);
        return result;
    }

    @Override
    public String toString() {
        return mode;
    }
}
