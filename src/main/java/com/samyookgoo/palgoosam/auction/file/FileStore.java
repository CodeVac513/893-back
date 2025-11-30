package com.samyookgoo.palgoosam.auction.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String uploadPath;

    public List<ResultFileStore> storeFiles(List<MultipartFile> multipartFiles) {
        List<ResultFileStore> resultFileStores = new ArrayList<>();

        if (multipartFiles != null && !multipartFiles.isEmpty()) {
            for (MultipartFile multipartFile : multipartFiles) {
                resultFileStores.add(storeFile(multipartFile));
            }
        }

        return resultFileStores;
    }

    public ResultFileStore storeFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFileName);
        String folderPath = makeFolder();

        String fullPath = folderPath + File.separator + storeFileName;

        try {
            multipartFile.transferTo(new File(fullPath));
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패 : " + originalFileName, e);
        }

        return new ResultFileStore(folderPath, originalFileName, storeFileName);
    }

    private String createStoreFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        return uuid + "_" + originalFileName;
    }

    private String makeFolder() {
        File file = new File(uploadPath);

        if (!file.exists()) {
            file.mkdirs();
        }

        return uploadPath;
    }

    public void delete(String storeFileName) {
        File file = new File(uploadPath + File.separator + storeFileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
