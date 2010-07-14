package org.birncommunity.gridftp.tar.impl.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.util.FileNameUtil;
import org.birncommunity.util.FileSystemHelper;
import org.birncommunity.util.MethodParameterChecker;

public class CompressTar implements org.birncommunity.gridftp.tar.TarI {

    private Log log = LogFactory.getLog(CompressTar.class);

    public void tarDirectoryToStream(File dir, OutputStream os) throws Exception {

        new MethodParameterChecker().verifyNotNull(dir, os);
        this.checkDirectory(dir);
        FileNameUtil fnu = new FileNameUtil();
        try {
            TarArchiveOutputStream taos = new TarArchiveOutputStream(os);
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            this.loadDirectoryIntoStream(dir, taos, fnu.getParentDirName(dir.getAbsolutePath()));
            taos.finish();
            taos.close();
        } catch (Exception e) {
            try {
                os.close();
            } catch (Exception e2) {
                log.warn("Failed to close output stream after previous error.", e2);
            }
            throw e;
        }
    }

    public void tarDirectoryToFile(File dir, File tarFile) throws Exception {

        new MethodParameterChecker().verifyNotNull(dir, tarFile);
        if (tarFile.exists() && !tarFile.canWrite()) {
            throw new IOException(tarFile.getAbsolutePath() + " cannot be written.");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tarFile);
            this.tarDirectoryToStream(dir, fos);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private void loadDirectoryIntoStream(File dir, TarArchiveOutputStream taos, String basePath) throws IOException {

        TarArchiveEntry tae = new TarArchiveEntry(dir.getAbsolutePath().replace(basePath, "") + File.separator);
        tae.setModTime(dir.lastModified());
        taos.putArchiveEntry(tae);
        if (log.isDebugEnabled()) {
            log.debug("processing directory " + dir.getAbsolutePath());
        }
        List<File> files = new FileSystemHelper().getDirectoryEntries(dir);
        for (File file : files) {
            if (file.isDirectory()) {
                this.loadDirectoryIntoStream(file, taos, basePath);
            } else {
                this.loadFileIntoStream(file, taos, basePath);
            }
        }
    }

    private void loadFileIntoStream(File file, TarArchiveOutputStream taos, String basePath) throws IOException {

        InputStream is = new FileInputStream(file);
        try {
            long fileLength = file.length();
            int numRead = 0;
            long totalNumRead = 0;
            byte[] buffer = new byte[512];

            if (log.isDebugEnabled()) {
                log.debug("processing file " + file.getAbsolutePath());
            }
            TarArchiveEntry tae = new TarArchiveEntry(file.getAbsolutePath().replace(basePath, ""));
            tae.setSize(fileLength);
            tae.setModTime(file.lastModified());
            taos.putArchiveEntry(tae);
            while ((numRead = is.read(buffer)) >= 0) {
                taos.write(buffer, 0, numRead);
                totalNumRead += numRead;
            }

            if (totalNumRead != fileLength) {
                throw new IOException("Unable to read file " + file.getAbsolutePath() + " completely");
            }
            taos.closeArchiveEntry();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void checkDirectory(File dir) throws IOException {

        if (!dir.exists()) {
            throw new IOException("Directory " + dir.getAbsolutePath() + " does not exist.");
        } else if (!dir.isDirectory()) {
            throw new IOException("Directory " + dir.getAbsolutePath() + " is not a directory.");
        } else if (!dir.canRead()) {
            throw new IOException(dir.getAbsolutePath() + " cannot be read");
        }
    }

}
