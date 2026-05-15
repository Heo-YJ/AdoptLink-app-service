package animals.demo.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    //공통
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    //회원가입
    DUPLICATE_LOGIN_ID(409, HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),
    DUPLICATE_NICKNAME(409, HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다."),
    DUPLICATE_PHONE(409, HttpStatus.CONFLICT, "이미 사용중인 휴대폰 번호입니다."),
    PASSWORD_MISMATCH(400, HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    //로그인
    ID_PASSWORD_MISMATCH(401, HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    MISSING_REQUIRED_VALUE(400, HttpStatus.BAD_REQUEST, "아이디와 비밀번호는 필수 입력값입니다."),

    //토큰 재발급
    INVALID_REFRESH_TOKEN(401, HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    EXPIRED_TOKEN(403, HttpStatus.FORBIDDEN, "만료된 토큰입니다."),

    //비밀번호 변경
    NEW_PASSWORD_NOT_CONFIRM(400, HttpStatus.BAD_REQUEST, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    CURRENT_PASSWORD_MISMATCH(401, HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");



    private final Integer status;
    private final HttpStatus httpStatus;
    private final String message;
}
