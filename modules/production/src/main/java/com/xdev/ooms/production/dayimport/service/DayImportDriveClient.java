package com.xdev.ooms.production.dayimport.service;

import java.util.List;

/**
 * Abstraction over Google Drive so the sync job can run when a real client bean is provided.
 */
public interface DayImportDriveClient {

    boolean isAvailable();

    List<DriveFileRef> listXlsx(String folderId) throws Exception;

    byte[] download(String fileId) throws Exception;

    void moveToFolder(String fileId, String targetFolderId) throws Exception;

    record DriveFileRef(String id, String name) {
    }
}
