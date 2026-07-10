package NoDam.Demo.common.util;

import java.util.*;
import java.util.function.Function;

public class ListUtil {

    public static <I, O> List<O> map(List<I> input, Function<I, O> mapFunction) {
        return input.stream()
                .map(mapFunction)
                .toList();
    }

    public static <R, S, ID> List<S> sortByRequestOrder(
            List<R> requestList,
            Function<R, ID> requestToId,
            List<S> selectedList,
            Function<S, ID> selectToId
    ) {
        Map<ID, S> map = new HashMap<>(selectedList.size());

        for (S s : selectedList) {
            map.put(selectToId.apply(s), s);
        }

        return requestList
                .stream()
                .map((r)->map.get(requestToId.apply(r)))
                .toList();
    }

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

    public static <T> List<T> distinct(List<T> canDuplicate) {
        return canDuplicate.stream()
                .distinct()
                .toList();
    }

}
