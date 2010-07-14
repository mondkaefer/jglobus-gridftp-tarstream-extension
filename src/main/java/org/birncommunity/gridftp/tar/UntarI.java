package org.birncommunity.gridftp.tar;

import java.io.File;
import java.io.InputStream;

public interface UntarI {

    public void untarFromStream(InputStream is, File directory) throws Exception;

    public void untarFromFile(File source, File directory) throws Exception;

}
