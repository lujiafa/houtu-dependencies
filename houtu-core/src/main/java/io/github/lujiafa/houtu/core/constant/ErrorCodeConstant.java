package io.github.lujiafa.houtu.core.constant;

public interface ErrorCodeConstant {

    Integer SUCCESS = 0;
    String SUCCESS_MESSAGE = "SUCCESS";

    Integer INTERNAL_ERROR = 1;
    Integer SERVER_BUSY = 2;
    Integer NETWORK_ERROR = 3;
    Integer OPERATION_FAIL = 4;
    Integer REQUEST_INVALID = 5;
    Integer REQUEST_INVALID_IP = 6;
    Integer REQUEST_INVALID_DATA = 7;
    Integer REQUEST_REPEAT = 8;
    Integer REQUEST_TOO_FREQUENCY = 9;

    Integer USERNAME_NOT_EXIST = 10;
    Integer ACCOUNT_LOCKED = 11;
    Integer ACCOUNT_EXCEPTION = 12;
    Integer PASSWORD_ERROR = 13;
    Integer USERNAME_OR_PASSWORD_ERROR = 14;
    Integer SESSION_EXPIRED = 15;
    Integer SESSION_KICK_OUT_EXPIRED = 16;
    Integer INVALID_VERIFICATION_INFO = 17;
    Integer INVALID_SIGNATURE_INFO = 18;
    Integer ACCESS_PERMISSIONS_DENIED = 19;

    Integer PARAMETER_ERROR = 30;
    Integer PARAMETER_FORMAT_ERROR = 31;
    Integer NOT_SUPPORTED_PARAMETER_TYPE_CONVERSION = 32;

    Integer DATA_LOADING_FAILED = 40;
    Integer DATA_NOT_EXIST = 41;
    Integer DATA_ALREADY_EXIST = 42;

    String UNKNOWN_ERROR_MESSAGE = "error";

}
