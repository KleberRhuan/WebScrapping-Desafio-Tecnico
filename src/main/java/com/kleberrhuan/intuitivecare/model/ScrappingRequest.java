package com.kleberrhuan.intuitivecare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class ScrappingRequest {
    private String url;
    private List<FileModel> files;
    
    public List<String> getFilesTypeExtensions() {
        return files.stream()
                .map(FileModel::fileType)
                .map(FileType::getExtension)
                .toList();
    }

}