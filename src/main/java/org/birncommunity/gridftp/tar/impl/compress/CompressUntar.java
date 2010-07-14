package org.birncommunity.gridftp.tar.impl.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.util.MethodParameterChecker;

/**
 * Always untars into the specified directory, even if the archive entries have leading "/"
 */
public class CompressUntar implements org.birncommunity.gridftp.tar.UntarI {

    private Log log = LogFactory.getLog(CompressUntar.class);

    public void untarFromStream(InputStream is, File destDir) throws IOException {

        new MethodParameterChecker().verifyNotNull(is, destDir);
        this.checkDirectory(destDir);
        TarArchiveInputStream tais = new TarArchiveInputStream(is);
        TarArchiveEntry tae = null;
        try {
            while ((tae = tais.getNextTarEntry()) != null) {
                if (tae.isDirectory()) {
                    this.handleDirectoryEntry(tae, destDir);
                } else {
                    this.handleFileEntry(tais, destDir, tae);
                }
            }
            tais.close();
        } catch (IOException e) {
            try {
                is.close();
            } catch (Exception e2) {
                log.warn("Failed to close input stream after previous error.", e2);
            }
            throw e;
        }
    }

    public void untarFromFile(File tarFile, File destDir) throws IOException {

        new MethodParameterChecker().verifyNotNull(tarFile, destDir);
        FileInputStream fis = new FileInputStream(tarFile);
        this.untarFromStream(fis, destDir);
    }

    private void handleDirectoryEntry(TarArchiveEntry tae, File destDir) throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("processing tar entry " + tae.getName());
        }
        File dir = new File(destDir, tae.getName());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can't create " + dir.getAbsolutePath());
            }
        }
    }

    // FIXME: Do we need to check for existence of the parent directory?
    private void handleFileEntry(TarArchiveInputStream tais, File destDir, TarArchiveEntry tae) throws IOException {

        FileOutputStream fos = new FileOutputStream(new File(destDir, tae.getName()));
        if (log.isDebugEnabled()) {
            log.debug("processing tar entry " + tae.getName());
        }
        try {
            long entryLength = tae.getSize();
            int numRead = 0;
            long totalNumWritten = 0;
            byte[] buffer = new byte[512];

            while ((numRead = tais.read(buffer)) >= 0) {
                fos.write(buffer, 0, numRead);
                totalNumWritten += numRead;
            }

            if (totalNumWritten != entryLength) {
                throw new IOException("Unable to write file " + tae.getName() + " completely");
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private void checkDirectory(File dir) throws IOException {

        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException(dir.getAbsolutePath() + " is not a directory.");
            } else if (!dir.canWrite()) {
                throw new IOException(dir.getAbsolutePath() + " cannot be written.");
            }
        } else {
            throw new IOException(dir.getAbsolutePath() + " does not exist.");
        }
    }

}
