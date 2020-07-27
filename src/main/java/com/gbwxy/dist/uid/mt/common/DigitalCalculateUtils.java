package com.gbwxy.dist.uid.mt.common;

import java.util.Stack;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/22
 */
public class DigitalCalculateUtils {
    private static final long THIRTY_SIX = 36L;
    private static final char[] baseSet36 = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public DigitalCalculateUtils() {
    }

    public static String transferFrom10toSpecifyLength(long number, int expectLength) {
        String longId = String.valueOf(number);
        String result = "";
        String first;
        String second;
        Long p2;
        if (expectLength >= 3 && expectLength <= 6) {
            first = longId.substring(0, 6);
            second = longId.substring(6, 12);
            String third = longId.substring(12);
            //p2 = Long.valueOf(first);
            //Long p2 = Long.valueOf(second);
            p2 = Long.valueOf(second);
            Long p3 = Long.valueOf(third);
            result = String.valueOf(p2 ^ p2 ^ p3);
            return subInstanceIdLen(result, expectLength);
        } else if (expectLength >= 7) {
            first = longId.substring(0, 8);
            second = longId.substring(8, 16);
            Long p1 = Long.valueOf(first);
            p2 = Long.valueOf(second);
            result = String.valueOf(p1 ^ p2);
            return subInstanceIdLen(result, expectLength);
        } else {
            return String.valueOf(number);
        }
    }

    private static String subInstanceIdLen(String result, int expectLength) {
        String len = "";
        if (expectLength == 3) {
            len = sub(result, 3);
        } else if (expectLength == 4) {
            len = sub(result, 4);
        } else if (expectLength == 5) {
            len = sub(result, 5);
        } else if (expectLength == 6) {
            len = sub(result, 6);
        } else if (expectLength == 7) {
            len = sub(result, 7);
        } else if (expectLength == 8) {
            len = sub(result, 8);
        }

        return len;
    }

    private static String sub(String result, int expect) {
        String len = "";
        if (result.length() >= expect) {
            len = result.substring(0, expect);
        } else {
            len = result + "0";
        }

        return len;
    }

    public static String transferFrom10to6Characters(long number) {
        String longId = String.valueOf(number);
        String first = longId.substring(0, 6);
        String second = longId.substring(6, 12);
        String third = longId.substring(12);
        Long p1 = Long.valueOf(first);
        Long p2 = Long.valueOf(second);
        Long p3 = Long.valueOf(third);
        String result = String.valueOf(p1 ^ p2 ^ p3);
        if (result.length() >= 6) {
            result = result.substring(0, 6);
        } else if (result.length() < 6) {
            result = result + "0";
        }

        return result;
    }

    public static String transferFrom10to36(long number) {
        long rest = number;
        if (number <= 0L) {
            return "a";
        } else {
            Stack stack;
            for(stack = new Stack(); rest != 0L; rest /= 36L) {
                stack.add(baseSet36[(new Long(rest - rest / 36L * 36L)).intValue()]);
            }

            StringBuilder result = new StringBuilder(stack.size());

            while(!stack.isEmpty()) {
                result.append(stack.pop());
            }

            return result.toString();
        }
    }
}
