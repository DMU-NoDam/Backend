package NoDam.Demo.common.domain;

import java.util.function.Supplier;

public class DomainResult <R> {

    private final boolean isSuccess;
    private final R result;

    public DomainResult(R result, boolean isSuccess) {
        this.result = result;
        this.isSuccess = isSuccess;
    }

    public static <T> DomainResult<T> success(T result) {
        return new DomainResult<T>(result, true);
    }

    public static <T> DomainResult<T> fail() {
        return new DomainResult<>(null, false);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isFail() {
        return !isSuccess;
    }

    public R getResult() {
        return result;
    }

    public <X extends Throwable> R orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isSuccess)
            return result;

        throw exceptionSupplier.get();
    }

}
