/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import common.FileDTO;
import java.io.File;


/**
 *This class i used to maintain information about a file.
 * 
 * @author Evan
 */
public class FileHandler implements FileDTO {

    private final String filename;
    private final String owner;
    private final boolean isPublic;
    private final boolean isWritable;
    private final long size;

    public FileHandler(String filename, String owner, long size, boolean isPublic, boolean isWritable) {
        this.size = size;
        this.filename = filename;
        this.owner = owner;
        this.isPublic = isPublic;
        this.isWritable = isWritable;
    }

    @Override
    public String getFileName() {
        return filename;
    }

    @Override
    public String getFileOwner() {
        return owner;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public boolean isWritable() {
        return isWritable;
    }

    @Override
    public long size() {
        return size;
    }

    public boolean deleteFile(String filepath) {
        File file = new File(filepath + filename);
        file.delete();
        return true;
    }

}
