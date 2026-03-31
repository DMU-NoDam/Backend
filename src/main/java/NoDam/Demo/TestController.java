package NoDam.Demo;

import NoDam.Demo.common.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "testController")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity ping() {
        return ResponseEntity.ok().body(new SuccessResponse("success", Map.of("ping", "pong")));
    }

    @GetMapping("/domain/visit")
    public ResponseEntity testVisitor() {
        return ResponseEntity.ok().body(new SuccessResponse("success", Map.of("ping", "pong")));
    }

    @GetMapping("/domain/api")
    public ResponseEntity testApi() {
        return ResponseEntity.ok().body(new SuccessResponse("success", Map.of("ping", "pong")));
    }

    @GetMapping("/domain/admin")
    public ResponseEntity testAdmin() {
        return ResponseEntity.ok().body(new SuccessResponse("success", Map.of("ping", "pong")));
    }

}
