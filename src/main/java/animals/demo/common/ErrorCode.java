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
    PHONE_NOT_VERIFIED(400, HttpStatus.BAD_REQUEST, "인증되지 않은 번호입니다."),
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
    CURRENT_PASSWORD_MISMATCH(401, HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다."),

    POST_NOT_FOUND(404, HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "해당 게시글의 작성자만 분양 상태를 변경할 수 있습니다."),

    //휴대폰 인증
    EXIST_PHONE(409, HttpStatus.CONFLICT, "이미 가입된 휴대폰 번호입니다."),
    INVALID_PHONE_FORMAT(400, HttpStatus.BAD_REQUEST,"올바른 휴대폰 번호 형식이 아닙니다."),
    REQUIRED_PHONE_NUMBER(400, HttpStatus.BAD_REQUEST,"휴대폰 번호는 필수 입력값입니다."),
    REQUIRED_PHONE_VERIFY_CODE(400, HttpStatus.BAD_REQUEST, "휴대폰 번호와 인증번호는 필수 입력값입니다."),
    EXPIRED_VERIFY_CODE(400, HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    INVALID_VERIFY_CODE(400, HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),

    //인증번호
    SMS_SEND_FAILED(500, HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송에 실패했습니다."),

    //채팅방
    NOT_CREATE_CHATROOM(403, HttpStatus.FORBIDDEN, "차단된 유저와 채팅방을 생성할 수 없습니다."),
    ALREADY_BLOCKED_USER(403, HttpStatus.FORBIDDEN, "이미 차단된 유저입니다."),
    CHATROOM_NOT_FOUND(404, HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    BLOCKED_USER(403, HttpStatus.FORBIDDEN, "더 이상 채팅을 보낼 수 없습니다."),
    ADMIN_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 관리자입니다."),

    //공지사항
    NOTICE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 공지사항입니다."),

    //보호소 관리자 신청
    NULL_IMAGE(400, HttpStatus.BAD_REQUEST, "증빙서류를 첨부해주세요."),
    FACILITY_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 보호소입니다."),
    EXIST_REQUEST(409, HttpStatus.CONFLICT, "이미 신청한 보호소 관리자 요청이 존재합니다."),
    REQUEST_NOT_FOUND(404, HttpStatus.NOT_FOUND, "요청을 찾을 수 없습니다."),
    ALREADY_PROCESSED(409, HttpStatus.CONFLICT, "이미 처리된 신청 요청입니다.");

    private final Integer status;
    private final HttpStatus httpStatus;
    private final String message;
}
