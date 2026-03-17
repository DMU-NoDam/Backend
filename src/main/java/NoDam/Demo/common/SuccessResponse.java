package NoDam.Demo.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SuccessResponse {
    private String message;
    private Object body;

    public SuccessResponse(String message, Object body) {
        this.message = message;
        this.body = body;
    }

}