package org.birncommunity.gridftp.tar;

import java.io.File;
import java.io.OutputStream;

public interface TarI {

    public void tarDirectoryToStream(File directory, OutputStream os) throws Exception;

    public void tarDirectoryToFile(File directory, File dest) throws Exception;

}
