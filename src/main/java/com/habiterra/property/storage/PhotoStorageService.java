package com.habiterra.property.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorageService {
    String store(MultipartFile file);
    void delete(String url);
    Resource load(String url);
}

