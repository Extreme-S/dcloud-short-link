package org.example.service;

import org.springframework.web.multipart.MultipartFile;


public interface FileService {

    /**
     * 文件上传
     */
    String uploadUserImg(MultipartFile file);
}
