package com.samyookgoo.palgoosam.bid.exception;

import com.samyookgoo.palgoosam.global.exception.ApiException;
import com.samyookgoo.palgoosam.global.exception.ErrorCode;

public class BidConflictException extends ApiException {
    public BidConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}