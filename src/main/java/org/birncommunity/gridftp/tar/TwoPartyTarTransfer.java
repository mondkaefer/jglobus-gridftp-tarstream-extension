package org.birncommunity.gridftp.tar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.util.FileNameUtil;
import org.birncommunity.util.MethodParameterChecker;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSinkStream;
import org.globus.ftp.DataSource;
import org.globus.ftp.DataSourceStream;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.ietf.jgss.GSSCredential;

public class TwoPartyTarTransfer {

    private Log log = LogFactory.getLog(TwoPartyTarTransfer.class);
    private String TAR_ALIAS = "tar";
    private String host;
    private int port;
    private GSSCredential credential;
    private TwoPartyTarClientFactoryI clientFactory;
    private boolean doIntegrityCheck;

    public TwoPartyTarTransfer(TwoPartyTarClientFactoryI clientFactory, String host, int port, GSSCredential credential) {

        MethodParameterChecker pc = new MethodParameterChecker();
        pc.verifyNotNull(clientFactory, host, port, credential);
        pc.verifyPositive(port);
        this.clientFactory = clientFactory;
        this.host = host;
        this.port = port;
        this.credential = credential;
        this.doIntegrityCheck = false;
    }

    public void downloadTarToFile(String sourceDir, String destFile) throws Exception {

        new MethodParameterChecker().verifyNotNull(sourceDir, destFile);
        String tarCommand = this.createDownloadTarSiteCommand(sourceDir);
        GridFTPClient client = null;
        try {
            client = clientFactory.createClient(host, port, credential, tarCommand, doIntegrityCheck);
            client.get(sourceDir, new File(destFile));
        } finally {
            if (client != null) {
                client.close(true);
            }
        }
    }

    public void downloadTarToPipe(String sourceDir, OutputStream os) throws Exception {

        new MethodParameterChecker().verifyNotNull(sourceDir, os);
        DataSink sink = null;
        GridFTPClient client = null;
        String tarCommand = this.createDownloadTarSiteCommand(sourceDir);
        try {
            client = clientFactory.createClient(host, port, credential, tarCommand, doIntegrityCheck);
            sink = new DataSinkStream(os);
            client.get(sourceDir, sink, null);
        } finally {
            if (os != null) {
                os.close();
            }
            if (sink != null) {
                sink.close();
            }
            if (client != null) {
                client.close(true);
            }
        }
    }

    public void uploadTarFromFile(String sourceFile, String destDir) throws Exception {

        new MethodParameterChecker().verifyNotNull(sourceFile, destDir);
        GridFTPClient client = null;
        String tarCommand = this.createUploadTarSiteCommand(destDir);
        try {
            client = clientFactory.createClient(host, port, credential, tarCommand, doIntegrityCheck);
            client.put(new File(sourceFile), destDir, false);
        } finally {
            if (client != null) {
                client.close(true);
            }
        }
    }

    public void uploadTarFromPipe(String destDir, InputStream is, OutputStream os) throws Exception {

        new MethodParameterChecker().verifyNotNull(destDir, is, os);
        DataSource ds = null;
        GridFTPClient client = null;
        String tarCommand = this.createUploadTarSiteCommand(destDir);
        try {
            client = clientFactory.createClient(host, port, credential, tarCommand, doIntegrityCheck);
            ds = new DataSourceStream(is);
            client.put(destDir, ds, null);
        } catch (Exception e) {
            if (os != null) {
                log.debug("Closing output stream to interrupt taring");
                os.close();
            }
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
            if (ds != null) {
                ds.close();
            }
            if (client != null) {
                client.close(true);
            }
        }
    }

    public String createDownloadTarSiteCommand(String sourceDir) {

        new MethodParameterChecker().verifyNotNull(sourceDir);
        FileNameUtil futil = new FileNameUtil();
        String parentDirName = futil.getUNIXParentDirName(sourceDir);
        String dirName = futil.getRelativeDirName(sourceDir);
        if (parentDirName == null || dirName == null) {
            throw new RuntimeException("Invalid source path for use with the tar feature: \"" + sourceDir + "\"");
        }
        StringBuffer sb = new StringBuffer();
        sb.append("SETDISKSTACK popen:argv=#");
        sb.append(TAR_ALIAS);
        sb.append("#cf#-#-C#");
        sb.append(parentDirName);
        sb.append("#");
        sb.append(dirName);
        return sb.toString();
    }

    public String createUploadTarSiteCommand(String destDir) {

        new MethodParameterChecker().verifyNotNull(destDir);
        StringBuffer sb = new StringBuffer();
        sb.append("SETDISKSTACK popen:argv=#");
        sb.append(TAR_ALIAS);
        sb.append("#xf#-#-C#");
        sb.append(destDir);
        sb.append(",ordering");
        return sb.toString();
    }

    public boolean isPopenDriverSupported() throws ServerException, ClientException, IOException {

        boolean popenDriverSupported = true;
        GridFTPClient client = null;
        try {
            client = new GridFTPClient(host, port);
            client.authenticate(credential);
            try {
                client.site(this.createUploadTarSiteCommand("/tmp"));
            } catch (Exception e) {
                popenDriverSupported = false;
            }
        } finally {
            if (client != null) {
                client.close(false);
            }
        }
        return popenDriverSupported;
    }

    public void enableDataIntegrityCheck(boolean doIntegrityCheck) {

        this.doIntegrityCheck = doIntegrityCheck;
    }

}
