package com.samyookgoo.palgoosam.bid.controller.response;

import com.samyookgoo.palgoosam.bid.domain.Bid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BidResponse {
    private Long bidId;
    private String bidderEmail;
    private Integer bidPrice;
    private String createdAt;
    private String updatedAt;

    public static BidResponse from(Bid bid) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return BidResponse.builder()
                .bidId(bid.getId())
                .bidderEmail(maskName(bid.getBidder().getName()))
                .bidPrice(bid.getPrice())
                .createdAt(LocalDateTime.now().format(formatter))
                .updatedAt(bid.getUpdatedAt() != null ? bid.getUpdatedAt().format(formatter) : null)
                .build();
    }

    private static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        int length = name.length();

        if (length == 2) {
            return name.charAt(0) + "*";
        } else if (length >= 3) {
            StringBuilder masked = new StringBuilder();
            masked.append(name.charAt(0));
            masked.append("*".repeat(length - 2));
            masked.append(name.charAt(length - 1));
            return masked.toString();
        }

        return name;
    }
}
