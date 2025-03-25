package com.kleberrhuan.intuitivecare.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileModel(
        @NotBlank(message = "File name is required")
        String name,
        @NotNull
        FileType fileType
) {
        public String getFullName() {
                return name + fileType.getExtension();
        }
}
