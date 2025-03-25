package com.kleberrhuan.intuitivecare.util;

import com.kleberrhuan.intuitivecare.model.FilelinkModel;

import java.io.IOException;

public interface DownloaderInterface {
    /**
     * Makes an HTTP request to download a file from the given URL and save it to the specified path.
     *
     * @param file object containing the URL and name of the file to download.
     * @param destinationDir Local dir where the file will be saved.
     * @throws IOException If an error occurs during the download.
     */
    void downloadFile(FilelinkModel file, String destinationDir) throws IOException;
}
