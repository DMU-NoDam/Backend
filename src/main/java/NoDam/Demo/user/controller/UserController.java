package NoDam.Demo.user.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.dto.request.LoginDto;
import NoDam.Demo.user.dto.request.RefreshTokenDto;
import NoDam.Demo.user.dto.request.RegisterDto;
import NoDam.Demo.user.dto.response.UserInfoDto;
import NoDam.Demo.user.service.JWTService;
import NoDam.Demo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "userController")
public class UserController {

    private final UserService userService;
    private final JWTService jwtService;

    @PostMapping("/public/register")
    @Operation(summary = "register")
    public ResponseEntity register(@RequestBody @Valid RegisterDto dto) {
        userService.registerWithEmail(dto.getEmail(), dto.getPassword(), dto.getName());
        return ResponseEntity.ok().body("success");
    }

    @PostMapping("/public/login")
    public ResponseEntity login(@RequestBody @Valid LoginDto dto) {
        User user = userService.login(dto.getEmail(), dto.getPassword());

        String accessToken = jwtService.generateAccessToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse(
                "success",
                Map.of("accessToken", accessToken, "refreshToken", refreshToken)
        ));
    }

    @PostMapping("/public/token-refresh")
    public ResponseEntity refresh(@RequestBody @Valid RefreshTokenDto dto) {
        Long userId = jwtService.decodeRefreshToken(dto.getToken());

        String newAccessToken = jwtService.generateAccessToken(userId);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        return ResponseEntity.ok().body(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        ));
    }

    @GetMapping("/api")
    public ResponseEntity getUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(UserInfoDto.of(user));
    }

}
