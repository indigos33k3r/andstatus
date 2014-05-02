/**
 * Copyright (C) 2014 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.app.backup;

import android.app.backup.BackupDataOutput;

import org.andstatus.app.data.DbUtils;
import org.andstatus.app.util.MyLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** Allowing to instantiate and to mock BackupDataOutput class */
public class MyBackupDataOutput {
    final static String HEADER_FILE_SUFFIX = "_header";
    final static String KEY_DATA_SIZE = "data_size";
    private File backupFolder;
    private BackupDataOutput backupDataOutput;
    private int sizeToWrite = 0;
    private int sizeWritten = 0;
    private File dataFile = null;

    public MyBackupDataOutput(BackupDataOutput backupDataOutput) {
        this.backupDataOutput = backupDataOutput;
    }
    
    public MyBackupDataOutput(File backupFolder) {
        this.backupFolder = backupFolder;
    }

    /** {@link BackupDataOutput#writeEntityHeader(String, int)} */
    public int writeEntityHeader(String key, int dataSize) throws IOException {
        if (backupDataOutput != null) {
            return backupDataOutput.writeEntityHeader(key, dataSize);
        } else {
            return writeEntityHeader2(key, dataSize);
        }
    }

    private int writeEntityHeader2(String key, int dataSize) throws IOException {
        MyLog.v(this, "Writing header for '" + key + "', size=" + dataSize);
        sizeToWrite = dataSize;
        sizeWritten = 0;
        writeHeaderFile(key, dataSize);
        createDataFile(key, dataSize);
        return key.length();
    }

    private void writeHeaderFile(String key, int dataSize) throws IOException {
        File headerFile = new File(backupFolder, key + HEADER_FILE_SUFFIX);
        createFileIfNeeded(dataSize, headerFile);
        JSONObject jso = new JSONObject();
        try {
            jso.put(KEY_DATA_SIZE, dataSize);
            byte[] bytes = jso.toString(2).getBytes("UTF-8");
            writeBytesToFile(headerFile, bytes, bytes.length);
        } catch (JSONException e) {
            throw new FileNotFoundException(e.getLocalizedMessage());
        }
    }

    private void createFileIfNeeded(int dataSize, File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new FileNotFoundException("Couldn't delete " + file.getAbsolutePath());
            }
        }
        if (dataSize >= 0) {
            if (!file.createNewFile()) {
                throw new FileNotFoundException("Couldn't create " + file.getAbsolutePath());
            }
        }
    }
    
    private void createDataFile(String key, int dataSize) throws IOException {
        dataFile = new File(backupFolder, key);
        createFileIfNeeded(dataSize, dataFile);
    }

    /** {@link BackupDataOutput#writeEntityData(byte[], int)} */
    public int writeEntityData(byte[] data, int size) throws IOException {
        if (backupDataOutput != null) {
            return backupDataOutput.writeEntityData(data, size);
        } else {
            return writeEntityData2(data, size);
        }
    }

    private int writeEntityData2(byte[] data, int size) throws IOException {
        if (!dataFile.exists()) {
            throw new FileNotFoundException("Output file doesn't exist " + dataFile.getAbsolutePath());
        }
        if (size < 0) {
            throw new FileNotFoundException("Wrong number of bytes to write: " + size);
        }
        writeBytesToFile(dataFile, data, size);
        sizeWritten += size;
        if (sizeWritten >= sizeToWrite) {
            try {
                if (sizeWritten > sizeToWrite) {
                    throw new FileNotFoundException("Data is longer than expected: written=" + sizeWritten 
                            + ", expected=" + sizeToWrite );
                }
            } finally {
                dataFile = null;
                sizeWritten = 0;
            }
        }
        return size;
    }

    private int writeBytesToFile(File file, byte[] data, int size) {
        MyLog.v(this, "Writing data to '" + file.getName() + "', size=" + size);
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file, true));
            out.write(data, 0, size);
        } catch (Exception e) {
            MyLog.d(this, file.getAbsolutePath(), e);
        } finally {
            DbUtils.closeSilently(out, file.getAbsolutePath());
        }        
        return 0;
    }
    
}
