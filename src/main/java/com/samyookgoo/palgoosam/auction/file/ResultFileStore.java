package com.samyookgoo.palgoosam.auction.file;

import com.samyookgoo.palgoosam.auction.domain.Auction;
import com.samyookgoo.palgoosam.auction.domain.AuctionImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultFileStore {
    private String folderName;
    private String originalFileName;
    private String storeFileName;

    public static AuctionImage toEntity(ResultFileStore file, Auction auction, int imageSeq) {
        String imageUrl = "/uploads/" + file.getStoreFileName();
        return AuctionImage.builder()
                .auction(auction)
                .originalName(file.getOriginalFileName())
                .storeName(file.getStoreFileName())
                .imageSeq(imageSeq)
                .url(imageUrl)
                .build();
    }

}
