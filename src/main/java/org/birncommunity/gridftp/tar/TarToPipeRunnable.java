package org.birncommunity.gridftp.tar;

import java.io.File;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.util.MethodParameterChecker;

public class TarToPipeRunnable implements Runnable {

    private Log log = LogFactory.getLog(TarToPipeRunnable.class);
    private OutputStream os;
    private String sourceDir;
    private TarI tar;

    public TarToPipeRunnable(String sourceDir, OutputStream os, TarI tar) {

        new MethodParameterChecker().verifyNotNull(sourceDir, os, tar);
        this.os = os;
        this.sourceDir = sourceDir;
        this.tar = tar;
    }

    public void run() {

        try {
            this.tar.tarDirectoryToStream(new File(sourceDir), os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (this.os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    log.warn("Failed to close output stream", e);
                }
            }
        }
    }

}
