package com.kleberrhuan.intuitivecare.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Archiver {
    void archiveFiles(List<Path> files, Path destDir, String archiveName) throws IOException;
}
