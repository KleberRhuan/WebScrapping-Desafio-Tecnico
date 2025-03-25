package com.kleberrhuan.intuitivecare.model;

import lombok.Getter;

@Getter
public enum FileType {
    PDF(".pdf"),
    DOC(".doc"),
    DOCX(".docx"),
    XLS(".xls"),
    XLSX(".xlsx"),
    CSV(".csv");

    private final String extension;
    
    FileType(String extension) {
        this.extension = extension;
    }

    public static FileType fromExtension(String extension) {
        for (FileType fileType : FileType.values()) {
            if (fileType.extension.equals(extension)) {
                return fileType;
            }
        }
        return null;
    }
}
