/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;

public class testfunction {
    private static boolean isOverlap(BigDecimal[] a, BigDecimal[] b) {
        return a[1].compareTo(b[0]) >= 0 && a[1].compareTo(b[1]) <= 0 || a[0].compareTo(b[0]) >= 0 && a[0].compareTo(b[1]) <= 0;
    }

    public static void main(String[] args) {
        String test2 = "9597(abc)";
        String[] a = test2.split("\\(");
        for (int i = 0; i < a.length; ++i) {
            System.out.println(a[i]);
        }
        String a2 = "1500593446";
        int aa = Integer.valueOf(a2);
        System.out.println(aa);
        BigDecimal[] arrA = new BigDecimal[]{new BigDecimal(2), new BigDecimal(6)};
        BigDecimal[] arrB = new BigDecimal[]{new BigDecimal(2), new BigDecimal(5)};
        System.out.println(testfunction.isOverlap(arrB, arrA));
        HashSet<Integer> setA = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4, 5));
        HashSet<Integer> setB = new HashSet<Integer>(Arrays.asList(3, 4, 5, 6));
        String[] arr = new String[]{"a", "b", "c"};
        HashSet<String> set = new HashSet<String>(Arrays.asList(arr));
        for (int i = 0; i < arr.length; ++i) {
            if (!set.contains(arr[i])) continue;
            System.out.println("true");
        }
    }
}

