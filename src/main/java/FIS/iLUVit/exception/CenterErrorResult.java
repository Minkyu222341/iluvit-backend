package FIS.iLUVit.exception;

import FIS.iLUVit.exception.exceptionHandler.ErrorResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CenterErrorResult implements ErrorResult {
    CENTER_NOT_EXIST(HttpStatus.I_AM_A_TEAPOT, "해당 아이디를 가진 센터가 존재하지 않습니다."),
    CENTER_WRONG_ADDRESS(HttpStatus.BAD_REQUEST, "잘못된 시설 주소입니다"),
    CENTER_ADDRESS_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류 입니다"),
    AUTHENTICATION_FAILED(HttpStatus.FORBIDDEN, "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
