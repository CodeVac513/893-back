package com.samyookgoo.palgoosam.auction.scheduler;

import com.samyookgoo.palgoosam.auction.repository.AuctionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionStatusScheduler {
    private final AuctionRepository auctionRepository;

    @Scheduled(fixedDelay = 60000)  // 1분마다 실행
    @Transactional
    public void updateAuctionStatus() {
        LocalDateTime now = LocalDateTime.now();

        int activeUpdated = auctionRepository.updateStatusToActive(now);
        int completedUpdated = auctionRepository.updateStatusToCompleted(now);

        log.info("경매 상태 업데이트: ACTIVE {}건", activeUpdated);
        log.info("경매 상태 업데이트: COMPLETED {}건", completedUpdated);
    }
}
