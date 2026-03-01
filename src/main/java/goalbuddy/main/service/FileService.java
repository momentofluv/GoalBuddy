package goalbuddy.main.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final String EXTERNAL_PATH = "D:/GoalBuddy_profile/";

    public String uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        // 확장자 타입 체크
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

        // 허용할 확장자 화이트리스트
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        try {
            // 파일명 난독화 (UUID+확장자)
            String savedFileName = UUID.randomUUID().toString() + "." + extension;
            File targetFile = new File(EXTERNAL_PATH + savedFileName);

            file.transferTo(targetFile);

            return "/uploads/" + savedFileName; // WebConfig로 설정

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
        }
    }
}
