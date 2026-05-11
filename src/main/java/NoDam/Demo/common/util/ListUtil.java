package NoDam.Demo.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ListUtil {

    public static <T, ID> List<T> sortByRequestOrder(
            List<ID> requestList,
            List<T> selectedList,
            Function<T, ID> getIdFunction
    ) {
        Map<ID, T> map = new HashMap<>(selectedList.size());

        for (T t : selectedList) {
            map.put(getIdFunction.apply(t), t);
        }

        return requestList
                .stream()
                .map(map::get)
                .toList();
    }

}
