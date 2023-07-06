package project.seatsence.src.user.api;

import static project.seatsence.global.code.ResponseCode.USER_EMAIL_ALREADY_EXIST;
import static project.seatsence.global.code.ResponseCode.USER_NICKNAME_ALREADY_EXIST;
import static project.seatsence.global.constants.Constants.TOKEN_TYPE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.seatsence.global.config.security.TokenUtils;
import project.seatsence.global.exceptions.BaseException;
import project.seatsence.src.user.domain.User;
import project.seatsence.src.user.dto.request.UserSignInRequest;
import project.seatsence.src.user.dto.request.UserSignUpRequest;
import project.seatsence.src.user.dto.request.ValidateEmailRequest;
import project.seatsence.src.user.dto.request.ValidateNicknameRequest;
import project.seatsence.src.user.dto.response.UserSignInResponse;
import project.seatsence.src.user.dto.response.UserSignUpResponse;
import project.seatsence.src.user.dto.response.ValidateUserInformationResponse;
import project.seatsence.src.user.service.UserSignInService;
import project.seatsence.src.user.service.UserSignUpService;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "01. [User]")
@Validated
@RequiredArgsConstructor
public class UserApi {

    private final UserSignUpService userSignUpService;
    private final UserSignInService userSignInService;

    @Operation(summary = "이메일 검증 및 중복 확인")
    @PostMapping("/validate/email")
    public ValidateUserInformationResponse validateEmail(
            @Valid @RequestBody ValidateEmailRequest validateEmailRequest) {
        ValidateUserInformationResponse response =
                new ValidateUserInformationResponse(
                        userSignUpService.isUsableByEmailDuplicateCheck(
                                validateEmailRequest.getEmail()));
        return response;
    }

    @Operation(summary = "닉네임 검증 및 중복 확인")
    @PostMapping("/validate/nickname")
    public ValidateUserInformationResponse validateNickname(
            @Valid @RequestBody ValidateNicknameRequest validateNicknameRequest) {
        ValidateUserInformationResponse response =
                new ValidateUserInformationResponse(
                        userSignUpService.isUsableByNicknameDuplicateCheck(
                                validateNicknameRequest.getNickname()));
        return response;
    }

    @Operation(summary = "유저 회원가입")
    @PostMapping("/sign-up")
    public UserSignUpResponse userSignUp(@Valid @RequestBody UserSignUpRequest userSignUpRequest) {
        if (!userSignUpService.isUsableByEmailDuplicateCheck(userSignUpRequest.getEmail())) {
            throw new BaseException(USER_EMAIL_ALREADY_EXIST);
        }
        if (!userSignUpService.isUsableByNicknameDuplicateCheck(userSignUpRequest.getNickname())) {
            throw new BaseException(USER_NICKNAME_ALREADY_EXIST);
        }
        return userSignUpService.userSignUp(userSignUpRequest);
    }

    @Operation(summary = "유저 로그인")
    @PostMapping("/sign-in")
    public UserSignInResponse userSignIn(@Valid @RequestBody UserSignInRequest userSignInRequest) {

        User user = userSignInService.userSignIn(userSignInRequest);
        String token = TokenUtils.generateAccessToken(user);

        String accessToken = TOKEN_TYPE + token;

        return new UserSignInResponse(accessToken);
    }
}
