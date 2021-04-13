package com.athaydes.geminix.client;

public sealed interface Response
        permits Response.Input, Response.Success, Response.Redirect,
        Response.TemporaryFailure, Response.PermanentFailure, Response.ClientCertRequired {

    StatusCode statusCode();

    final record Input(StatusCode statusCode, String prompt) implements Response {
        public Input {
            if (!statusCode.isInput()) {
                throw new IllegalArgumentException("statusCode must be INPUT");
            }
        }
    }

    final record Success(StatusCode statusCode, String mediaType, byte[] body) implements Response {
        public Success {
            if (!statusCode.isSuccess()) {
                throw new IllegalArgumentException("statusCode must be SUCCESS");
            }
        }
    }

    final record Redirect(StatusCode statusCode, String uri) implements Response {
        public Redirect {
            if (!statusCode.isRedirect()) {
                throw new IllegalArgumentException("statusCode must be REDIRECT");
            }
        }
    }

    final record TemporaryFailure(StatusCode statusCode, String errorMessage) implements Response {
        public TemporaryFailure {
            if (!statusCode.isTempFailure()) {
                throw new IllegalArgumentException("statusCode must be TEMP_FAILURE");
            }
        }
    }

    final record PermanentFailure(StatusCode statusCode, String errorMessage) implements Response {
        public PermanentFailure {
            if (!statusCode.isPermFailure()) {
                throw new IllegalArgumentException("statusCode must be PERM_FAILURE");
            }
        }
    }

    final record ClientCertRequired(StatusCode statusCode, String userMessage) implements Response {
        public ClientCertRequired {
            if (!statusCode.isClientCertRequired()) {
                throw new IllegalArgumentException("statusCode must be CLIENT_CERT_REQUIRED");
            }
        }
    }
}