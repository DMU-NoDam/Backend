package NoDam.Demo;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.domain.UserRole;
import NoDam.Demo.user.repository.UserRepository;
import NoDam.Demo.user.service.JWTService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "testController")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final JWTService jwtService;

    @GetMapping("/test")
    public ResponseEntity ping() {
        return ResponseEntity.ok().body(new SuccessResponse("success", Map.of("ping", "pong")));
    }

    @GetMapping("/test/public")
    public ResponseEntity testPublic() {
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

    @PostMapping("/test/user")
    public ResponseEntity createOrIssueTestUserToken(@RequestBody TestUserRequest request) {
        User user;

        if (request.getId() == null) {
            user = userRepository.save(
                    User.builder()
                            .name("testUser")
                            .role(UserRole.valueOf(request.getRole()))
                            .oAuthId("testUser")
                            .oAuthProvider("test")
                            .build()
            );
        } else {
            user = userRepository.findById(request.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        }

        return ResponseEntity.ok().body(new SuccessResponse(
                "success",
                Map.of(
                        "userId",  user.getId(),
                        "accessToken", jwtService.generateAccessToken(user.getId()),
                        "refreshToken", jwtService.generateRefreshToken(user.getId())
                )
        ));
    }

    @Getter
    @Setter
    public static class TestUserRequest {
        private Long id;
        private String role;
    }

}
