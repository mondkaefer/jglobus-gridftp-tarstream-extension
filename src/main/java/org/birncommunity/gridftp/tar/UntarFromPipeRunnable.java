package org.birncommunity.gridftp.tar;

import java.io.File;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.util.MethodParameterChecker;

public class UntarFromPipeRunnable implements Runnable {

    private Log log = LogFactory.getLog(UntarFromPipeRunnable.class);
    private InputStream is;
    private String destDir;
    private UntarI untar;

    public UntarFromPipeRunnable(InputStream is, String destDir, UntarI untar) throws Exception {

        new MethodParameterChecker().verifyNotNull(destDir, is, untar);
        this.is = is;
        this.destDir = destDir;
        this.untar = untar;
    }

    public void run() {

        try {
            this.untar.untarFromStream(is, new File(destDir));
        } catch (Exception e) {
            try {
                if (this.is != null) {
                    this.is.close();
                }
            } catch (Exception e2) {
                log.warn("Failed to close input stream", e2);
            }
            throw new RuntimeException(e);
        }
    }

}
