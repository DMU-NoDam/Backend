package NoDam.Demo.user.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.dto.request.RefreshTokenDto;
import NoDam.Demo.user.dto.request.UpdateUserInfoDto;
import NoDam.Demo.user.dto.response.UserInfoDto;
import NoDam.Demo.user.jwt.JWTException;
import NoDam.Demo.user.service.JWTService;
import NoDam.Demo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/public/token-refresh")
    public ResponseEntity<SuccessResponse<Map<String, String>>> refresh(@RequestBody @Valid RefreshTokenDto dto) {
        Long userId;
        try {
            userId = jwtService.decodeRefreshToken(dto.getToken());
        } catch (JWTException e) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        String newAccessToken = jwtService.generateAccessToken(userId);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        return ResponseEntity.ok().body(new SuccessResponse<>("success", Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        )));
    }

    @GetMapping("/api")
    public ResponseEntity<SuccessResponse<UserInfoDto>> getUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(new SuccessResponse<>("success", UserInfoDto.of(user)));
    }

    @PatchMapping("/api")
    @Operation(summary = "update user info")
    public ResponseEntity<SuccessResponse<UserInfoDto>> updateUserInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserInfoDto dto
    ) {
        User updatedUser = userService.updateUserInfo(user, dto);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", UserInfoDto.of(updatedUser)));
    }

    @DeleteMapping("/api")
    @Operation(summary = "delete user")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@AuthenticationPrincipal User user) {
        userService.deleteUser(user);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", null));
    }

}
