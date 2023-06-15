package net.mguenther.reactive.employee;

import static java.lang.String.format;

public class MissingParameterException extends RuntimeException {

    private static final String ERROR_MESSAGE_TEMPLATE = "The request sent by the client is faulty: '%s'.";

    public MissingParameterException(final String message) {
        super(format(ERROR_MESSAGE_TEMPLATE, message));
    }
}
