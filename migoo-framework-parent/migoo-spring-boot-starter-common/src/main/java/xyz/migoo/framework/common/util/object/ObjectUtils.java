package xyz.migoo.framework.common.util.object;

import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Object 工具类
 *
 * @author xiaomi
 */
public class ObjectUtils {

    /**
     * 深拷贝对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T object) {
        if (object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.close();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Clone failed", e);
        }
    }

    /**
     * 深拷贝对象并修改
     */
    public static <T extends Serializable> T clone(T object, Consumer<T> consumer) {
        T result = clone(object);
        if (result != null) {
            consumer.accept(result);
        }
        return result;
    }

    public static <T extends Comparable<T>> T max(T obj1, T obj2) {
        if (obj1 == null) {
            return obj2;
        }
        if (obj2 == null) {
            return obj1;
        }
        return obj1.compareTo(obj2) > 0 ? obj1 : obj2;
    }

    public static <T> T defaultIfNull(T... array) {
        for (T item : array) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }
    
    @SafeVarargs
    public static <T> boolean equalsAny(T obj, T... array) {
        return Arrays.asList(array).contains(obj);
    }

}
