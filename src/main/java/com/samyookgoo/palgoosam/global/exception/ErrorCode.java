package com.samyookgoo.palgoosam.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 인증/권한
    UNAUTHORIZED_USER(401, "A_001", "클라이언트 인증 부재, 로그인 해주세요."),
    USER_NOT_FOUND(404, "U_001", "해당 유저를 찾을 수 없습니다."),
    FORBIDDEN(403, "A_002", "해당 리소스에 접근할 수 없습니다."),
    CATEGORY_NOT_FOUND(404, "C_001", "카테고리가 존재하지 않습니다."),
    ADDRESS_NOT_FOUND(404, "ADR_001", "변경할 주소를 찾을 수 없습니다."),
    DELETE_ADDRESS_NOT_FOUND(404, "ADR_002", "삭제할 주소를 찾을 수 없습니다."),

    // 경매 등록/수정 검증
    AUCTION_NOT_FOUND(404, "AUC_001", "해당 경매 상품이 존재하지 않습니다."),
    AUCTION_UPDATE_LOCKED(400, "AUC_002", "경매 시작 10분 전부터는 수정이 불가능합니다."),
    AUCTION_TIME_REQUIRED(400, "AUC_003", "경매 시작 시간 및 경매 소요 시간은 필수입니다."),
    AUCTION_MAIN_IMAGE_REQUIRED(400, "AUC_004", "대표 이미지를 반드시 포함해야 합니다. (imageSeq == 0)"),
    AUCTION_CATEGORY_NOT_LEAF(400, "AUC_005", "카테고리를 소분류까지 모두 선택해야 합니다."),
    AUCTION_INVALID_MIN_BASE_PRICE(400, "AUC_006", "시작가는 0원 이상이어야 합니다."),
    AUCTION_INVALID_MAX_BASE_PRICE(400, "AUC_007", "시작가는 1억 원까지만 입력 가능합니다."),
    AUCTION_OPEN_TIME_INVALID(400, "AUC_008", "경매 오픈 시간은 현재 시각 이후부터 24시간 이내여야 합니다."),
    AUCTION_DURATION_INVALID(400, "AUC_009", "경매 소요 시간은 10분 이상, 24시간(1440분) 이내여야 합니다."),
    AUCTION_IMAGE_UPLOAD_FAILED(500, "AUC_010", "경매 이미지 업로드에 실패했습니다."),
    AUCTION_IMAGE_SAVE_FAILED(500, "AUC_011", "경매 이미지 저장 중 문제가 발생했습니다."),
    AUCTION_IMAGE_DELETE_FAILED(500, "AUC_012", "경매 이미지 삭제 중 문제가 발생했습니다."),
    AUCTION_DELETE_FORBIDDEN(403, "AUC_013", "본인의 경매만 삭제할 수 있습니다."),
    AUCTION_UPDATE_FORBIDDEN(403, "AUC_014", "본인의 경매만 수정할 수 있습니다."),
    AUCTION_DELETE_AFTER_START_FORBIDDEN(400, "AUC_015", "경매 시작 10분 전이 지나거나, 경매 중에는 삭제할 수 없습니다."),

    // 입찰 관련
    BID_NOT_FOUND(404, "BID_001", "해당 입찰 내역이 존재하지 않습니다."),
    BID_TIME_INVALID(400, "BID_002", "현재는 입찰 가능한 시간이 아닙니다."),
    BID_LESS_THAN_BASE(400, "BID_003", "시작가보다 높은 금액을 입력해야 합니다."),
    BID_NOT_HIGHEST(400, "BID_004", "현재 최고가보다 높은 금액을 입력해야 합니다."),
    BID_ALREADY_CANCELED(400, "BID_005", "이미 취소된 입찰입니다."),
    BID_CANCEL_EXPIRED(400, "BID_006", "입찰 후 1분 이내에만 취소할 수 있습니다."),
    BID_CANCEL_NOT_TOP(400, "BID_007", "최고 입찰가가 아니면 취소할 수 없습니다."),
    SELLER_CANNOT_BID(403, "BID_008", "판매자는 자신의 경매에 입찰할 수 없습니다."),
    BID_CANCEL_FORBIDDEN(403, "BID_009", "본인의 입찰만 취소할 수 있습니다."),
    BID_CANCEL_LIMIT_EXCEEDED(400, "BID_010", "입찰 취소는 1회까지만 가능합니다."),
    WINNING_BID_NOT_FOUND(400, "BID_011", "낙찰된 입찰이 존재하지 않습니다."),
    BID_EXCEEDS_MAXIMUM(400, "BID_012", "입찰 금액은 최대 10억까지 가능합니다."),

    // FCM
    FCM_TOKEN_MISSING(404, "FCM_001", "토큰이 없습니다."),
    FCM_TOKEN_INVALID(400, "FCM_002", "유효하지 않은 토큰입니다."),

    // 결제
    PAYMENT_NOT_FOUND(404, "PAY_001", "해당 주문을 찾을 수 없습니다."),
    ALREADY_PAID(400, "PAY_002", "이미 결제 처리된 주문입니다."),
    PAYMENT_AMOUNT_MISMATCH(400, "PAY_003", "결제 금액이 낙찰 금액과 일치하지 않습니다."),
    PAYMENT_IN_PROGRESS_OR_DONE(400, "PAY_004", "이미 결제 중이거나 완료된 주문입니다."),
    INVALID_DELIVERY_FEE(400, "PAY_005", "배송비가 올바르지 않습니다."),
    NOT_WINNING_BIDDER(403, "PAY_006", "낙찰자만 결제할 수 있습니다."),
    TOSS_PAYMENT_FAILED(502, "PAY_008", "Toss 결제 오류가 발생했습니다."),
    TOSS_PAYMENT_EMPTY_RESPONSE(502, "PAY_009", "Toss 응답이 비어 있습니다."),
    PAYMENT_NOT_COMPLETED(400, "PAY_010", "낙찰자의 결제가 완료되지 않아 삭제할 수 없습니다."),
    INVALID_PAYMENT_STATUS(400, "PAY_011", "현재 결제 상태에서는 이 작업을 수행할 수 없습니다."),

    // 검색 기록
    SEARCH_HISTORY_NOT_FOUND(404, "SEARCH_HIST_001", "해당 검색 기록을 찾을 수 없습니다."),
    SEARCH_HISTORY_BLANK_BAD_REQUEST(400, "SEARCH_HIST_002", "처리할 수 없는 공백 요청입니다."),
    SEARCH_HISTORY_ALREADY_DELETED_BAD_REQUEST(400, "SEARCH_HIST_003", "이미 삭제된 검색 기록입니다."),

    // 배송지
    DELIVERY_ADDRESS_NOT_FOUND(404, "DELIVERY_ADDR_001", "해당 배송지 정보를 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(500, "SYS_001", "서버가 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(405, "SYS_002", "API는 열려있으나 메소드는 사용 불가합니다."),
    INVALID_INPUT_VALUE(400, "SYS_003", "적절하지 않은 요청 값입니다."),
    INVALID_TYPE_VALUE(400, "SYS_004", "요청 값의 타입이 잘못되었습니다."),
    ENTITY_NOT_FOUND(400, "SYS_005", "지정한 Entity를 찾을 수 없습니다."),
    AUTH_ERROR(400, "AU_001", "인증 관련 오류가 발생했습니다.");

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }
}
