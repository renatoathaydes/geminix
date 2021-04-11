package com.athaydes.geminix.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static com.athaydes.geminix.SpecialCharacters.*;
import static com.athaydes.geminix.protocol.StatusCode.*;

public class ResponseParser {

    public Response parse(InputStream is) throws IOException, ResponseParseError {
        var status = parseStatus(is);
        if (is.read() != ' ') {
            throw new ResponseParseError("Expected whitespace after status");
        }
        var scanner = new Scanner(is, StandardCharsets.UTF_8);
        var meta = scanner.nextLine();

        if (status.isInput()) {
            return new Response.Input(status, meta);
        }
        if (status.isSuccess()) {
            var body = is.readAllBytes();
            return new Response.Success(status, meta, body);
        }
        if (status.isRedirect()) {
            return new Response.Redirect(status, meta);
        }
        if (status.isTempFailure()) {
            return new Response.TemporaryFailure(status, meta);
        }
        if (status.isPermFailure()) {
            return new Response.PermanentFailure(status, meta);
        }
        return new Response.ClientCertRequired(status, meta);
    }

    StatusCode parseStatus(InputStream is) throws IOException, ResponseParseError {
        var first = is.read();
        switch (first) {
            case ASCII_1:
                return switch (is.read()) {
                    case ASCII_0 -> INPUT_10;
                    case ASCII_1 -> SENSITIVE_INPUT_11;
                    case ASCII_2, ASCII_3, ASCII_4, ASCII_5, ASCII_6, ASCII_7, ASCII_8, ASCII_9 -> UNKNOWN_INPUT_1;
                    default -> throw new ResponseParseError("second status code digit: not a digit");
                };
            case ASCII_2:
                return switch (is.read()) {
                    case ASCII_0 -> SUCCESS_20;
                    case ASCII_1, ASCII_2, ASCII_3, ASCII_4, ASCII_5, ASCII_6, ASCII_7, ASCII_8, ASCII_9 -> UNKNOWN_SUCCESS_2;
                    default -> throw new ResponseParseError("second status code digit: not a digit");
                };
            case ASCII_3:
                return switch (is.read()) {
                    case ASCII_0 -> REDIRECT_TEMP_30;
                    case ASCII_1 -> REDIRECT_PERM_31;
                    case ASCII_2, ASCII_3, ASCII_4, ASCII_5, ASCII_6, ASCII_7, ASCII_8, ASCII_9 -> UNKNOWN_REDIRECT_3;
                    default -> throw new ResponseParseError("second status code digit: not a digit");
                };
            case ASCII_4:
                return switch (is.read()) {
                    case ASCII_0 -> FAILURE_TEMP_40;
                    case ASCII_1 -> SERVER_UNAVAILABLE_41;
                    case ASCII_2 -> CGI_ERROR_42;
                    case ASCII_3 -> PROXY_ERROR_43;
                    case ASCII_4 -> SLOW_DOWN_44;
                    case ASCII_5, ASCII_6, ASCII_7, ASCII_8, ASCII_9 -> UNKNOWN_TEMP_FAILURE_4;
                    default -> throw new ResponseParseError("second status code digit: not a digit");
                };
            case ASCII_5:
                return switch (is.read()) {
                    case ASCII_0 -> FAILURE_PERM_50;
                    case ASCII_1 -> NOT_FOUND_51;
                    case ASCII_2 -> GONE_52;
                    case ASCII_3 -> PROXY_REQ_REFUSED_53;
                    case ASCII_9 -> BAD_REQUEST_59;
                    case ASCII_4, ASCII_5, ASCII_6, ASCII_7, ASCII_8 -> UNKNOWN_PERM_FAILURE_5;
                    default -> throw new ResponseParseError("second status code digit: not a digit");
                };
            case ASCII_6:
                return switch (is.read()) {
                    case ASCII_0 -> CLIENT_CERT_REQUIRED_60;
                    case ASCII_1 -> CLIENT_CERT_UNAUTHORIZED_61;
                    case ASCII_2 -> CLIENT_CERT_INVALID_62;
                    case ASCII_3, ASCII_4, ASCII_5, ASCII_6, ASCII_7, ASCII_8, ASCII_9 -> UNKNOWN_CLIENT_CERT_6;
                    default -> throw new ResponseParseError("second status code digit: not a digit");
                };
            case ASCII_0, ASCII_7, ASCII_8, ASCII_9:
                throw new ResponseParseError("first status code digit: invalid digit " +
                        "(not in 1-6 range: " + (first - 48) + ")");
            default:
                throw new ResponseParseError("first status code digit: not a digit");
        }
    }

}

