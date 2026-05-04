package NoDam.Demo.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SuccessResponse<T> {
    private String message;
    private T body;

    public SuccessResponse(String message, T body) {
        this.message = message;
        this.body = body;
    }

}