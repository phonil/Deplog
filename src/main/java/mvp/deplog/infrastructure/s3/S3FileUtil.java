package mvp.deplog.infrastructure.s3;

import java.util.UUID;

public class S3FileUtil {

    // 파일 저장명 만들기
    public static String createSaveFileNameFromFileName(String originalFileName) {
        String ext = extractExtFromFileName(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    public static String createSaveFileNameFromContentType(String contentType) {
        String ext = extractExtFromContentType(contentType);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    public static String createSaveFileName() {
        return UUID.randomUUID().toString();
    }

    // 확장자명 구하기
    public static String extractExtFromFileName(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(pos + 1);
    }

    public static String extractExtFromContentType(String contentType) {
        int pos = contentType.lastIndexOf("/");
        return contentType.substring(pos + 1);
    }

    // 전체 경로 만들기
    public static String getFullPath(String bucket, String fileName) {
        return "https://" + bucket + ".s3.amazonaws.com/" + fileName;
    }

    // url로부터 파일 이름 추출 (delete 시 사용)
    public static String extractFileNameFromUrl(String filePath) {
        try {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        } catch (Exception e) {
            return null;
        }
    }
}
