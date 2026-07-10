package NoDam.Demo.common.util;

public class StringUtil {

    public static boolean isEmpty(String... strs) {
        for (String str : strs)
            if (isEmpty(str))
                return true;
        return false;
    }

    public static boolean isEmpty(String str) {
        if(str == null || str.isEmpty())
            return true;
        return false;
    }

}
