package com.aperto.magkit.beans;

import java.util.Calendar;

/**
 * @author diana.racho (29.04.2008)
 */
public class DocumentInfo {
    private long _fileSize;
    private String _fileExtension;
    private Calendar _fileModificationDate;
    private String _fileName;


    public long getFileSize() {
        return _fileSize;
    }

    public void setFileSize(long fileSize) {
        _fileSize = fileSize;
    }

    public String getFileExtension() {
        return _fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        _fileExtension = fileExtension;
    }

    public Calendar getFileModificationDate() {
        return _fileModificationDate;
    }

    public void setFileModificationDate(Calendar fileModificationDate) {
        _fileModificationDate = fileModificationDate;
    }

    public String getFileName() {
        return _fileName;
    }

    public void setFileName(String fileName) {
        _fileName = fileName;
    }
}
