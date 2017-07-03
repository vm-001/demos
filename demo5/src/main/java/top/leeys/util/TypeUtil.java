package top.leeys.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Lambda类型转换工具类
 *
 */
public class TypeUtil {
    /**
     * List类型转换
     * 
     * <p> List<String> stringList = Arrays.asList("1","2","3"); </p>
     * <p> List<Integer> integerList = convertList(stringList, s -> Integer.parseInt(s));</p>
     */
    public static <T, U> List<U> convertList(List<T> from, Function<T, U> func) {
        return from.stream().map(func).collect(Collectors.toList());
    }

    /**
     * Array类型转换
     * 
     * <p> String[] stringArr = {"1","2","3"};
     * <p> Double[] doubleArr = convertArray(stringArr, Double::parseDouble, Double[]::new);</p>
     */
    public static <T, U> U[] convertArray(T[] from, Function<T, U> func, IntFunction<U[]> generator) {
        return Arrays.stream(from).map(func).toArray(generator);
    }
}
