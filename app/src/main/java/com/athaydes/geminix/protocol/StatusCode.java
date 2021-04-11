package com.athaydes.geminix.protocol;

enum StatusCode {
    INPUT_10,
    SENSITIVE_INPUT_11,
    SUCCESS_20,
    REDIRECT_TEMP_30,
    REDIRECT_PERM_31,
    FAILURE_TEMP_40,
    SERVER_UNAVAILABLE_41,
    CGI_ERROR_42,
    PROXY_ERROR_43,
    SLOW_DOWN_44,
    FAILURE_PERM_50,
    NOT_FOUND_51,
    GONE_52,
    PROXY_REQ_REFUSED_53,
    BAD_REQUEST_59,
    CLIENT_CERT_REQUIRED_60,
    CLIENT_CERT_UNAUTHORIZED_61,
    CLIENT_CERT_INVALID_62,
    UNKNOWN_INPUT_1,
    UNKNOWN_SUCCESS_2,
    UNKNOWN_REDIRECT_3,
    UNKNOWN_TEMP_FAILURE_4,
    UNKNOWN_PERM_FAILURE_5,
    UNKNOWN_CLIENT_CERT_6,
    ;

    public boolean isInput() {
        return this == INPUT_10 || this == SENSITIVE_INPUT_11 || this == UNKNOWN_INPUT_1;
    }

    public boolean isSuccess() {
        return this == SUCCESS_20 || this == UNKNOWN_SUCCESS_2;
    }

    public boolean isRedirect() {
        return this == REDIRECT_TEMP_30 || this == REDIRECT_PERM_31 || this == UNKNOWN_REDIRECT_3;
    }

    public boolean isTempFailure() {
        return this == FAILURE_TEMP_40 || this == SERVER_UNAVAILABLE_41 || this == CGI_ERROR_42 ||
                this == PROXY_ERROR_43 || this == SLOW_DOWN_44 || this == UNKNOWN_TEMP_FAILURE_4;
    }

    public boolean isPermFailure() {
        return this == FAILURE_PERM_50 || this == NOT_FOUND_51 || this == GONE_52 || this == PROXY_REQ_REFUSED_53 ||
                this == BAD_REQUEST_59 || this == UNKNOWN_PERM_FAILURE_5;
    }

    public boolean isClientCertRequired() {
        return this == CLIENT_CERT_REQUIRED_60 || this == CLIENT_CERT_UNAUTHORIZED_61 ||
                this == CLIENT_CERT_INVALID_62 || this == UNKNOWN_CLIENT_CERT_6;
    }

    public boolean isSecondDigitUnknown() {
        return this == UNKNOWN_INPUT_1 || this == UNKNOWN_SUCCESS_2 || this == UNKNOWN_REDIRECT_3 ||
                this == UNKNOWN_TEMP_FAILURE_4 || this == UNKNOWN_PERM_FAILURE_5 || this == UNKNOWN_CLIENT_CERT_6;
    }
}
