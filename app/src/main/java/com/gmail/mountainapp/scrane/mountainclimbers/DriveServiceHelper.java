package com.gmail.mountainapp.scrane.mountainclimbers;

/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    public Task<String> createFile() {
        return Tasks.call(mExecutor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                File metadata = new File()
                        .setParents(Collections.singletonList("root"))
                        .setMimeType("application/x-sqlite3")
                        .setName("Mountain Climbers backup");
                File googleFile = mDriveService.files().create(metadata).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }
                return googleFile.getId();
            }
        });
    }

    public Task<Boolean> readFile(final String fileId, final Context context) {
        Log.d("DRIVE", "reading file " + fileId);
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                File metadata = mDriveService.files().get(fileId).execute();
                String name = metadata.getName();
                try (FileOutputStream fos = new FileOutputStream(context.getDatabasePath(DataBaseHandler.BACKUP_NAME))) {
                    mDriveService.files().get(fileId).executeMediaAndDownloadTo(fos);
                    //Toast.makeText(context, context.getString(R.string.backup_success), Toast.LENGTH_SHORT).show();
                    return true;
                } catch (IOException e){
                    e.printStackTrace();
                    //Toast.makeText(context, context.getString(R.string.backup_fail), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }

    public Task<Void> saveFile(final String fileId, final String name, final byte[] content) {
        Log.d("DRIVE", "Saving file "+fileId);
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create a File containing any metadata changes.
                File metadata = new File().setName(name);

                // Convert content to an AbstractInputStreamContent instance.
                ByteArrayContent contentStream = new ByteArrayContent("text/plain", content);

                // Update the metadata and contents.
                mDriveService.files().update(fileId, metadata, contentStream).execute();
                return null;
            }
        });
    }

    public Task<Void> deletefile(final String fileId){
        Log.d("DRIVE", "deleting file " + fileId);
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mDriveService.files().delete(fileId);
                return null;
            }
        });
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, new Callable<FileList>() {
                    @Override
                    public FileList call() throws Exception {
                        return mDriveService.files().list().setSpaces("drive").execute();
                    }
                });
    }
}