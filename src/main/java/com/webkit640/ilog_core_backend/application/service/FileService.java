package com.webkit640.ilog_core_backend.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {
    private static final String UPLOAD_DIR = "/home/webkit/uploads/";

    public String upload(MultipartFile file){
        try{
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            if(file == null || file.isEmpty()){
                throw new CustomException(ErrorCode.FILE_EMPTY);
            }
            String safeName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String filename = UUID.randomUUID() + "_" + safeName;

            Path dest = uploadPath.resolve(filename);
            file.transferTo(dest.toFile());
            return "/uploads/" + filename;
        }catch(IOException e){
            log.error("File upload failed: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    public void delete(String fileUrl){
        if(fileUrl == null || fileUrl.isBlank()) return;

        try{
            String fileName = Paths.get(fileUrl).getFileName().toString();
            Path filePath = Paths.get(UPLOAD_DIR,fileName);

            Files.deleteIfExists(filePath);
        } catch (IOException e){
            e.printStackTrace();
            throw new CustomException(ErrorCode.FILE_DELETE_FAIL);
        }
    }
}
