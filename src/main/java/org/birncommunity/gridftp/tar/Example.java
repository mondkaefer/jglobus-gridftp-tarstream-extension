package org.birncommunity.gridftp.tar;

import java.io.File;
import java.io.FileInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.gridftp.tar.impl.compress.CompressTar;
import org.birncommunity.gridftp.tar.impl.compress.CompressUntar;
import org.birncommunity.util.FileNameUtil;
import org.birncommunity.util.MethodParameterChecker;
import org.globus.ftp.GridFTPClient;
import org.globus.util.ConfigUtil;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;

public class Example {

    private static Log log = LogFactory.getLog(Example.class);
    private String host;
    private int port;
    private String sourceDir;
    private String destDir;
    private String direction;

    public static void main(String[] args) throws Exception {

        if (args.length != 5 || args[0].equals("-h") || args[0].equals("--help")) {
            log.error("Usage: java " + Example.class.getName() + " <host> <port> <direction> <sourceDir> <destDir>");
            log.error("host: GridFTP server hostname");
            log.error("port: GridFTP server port");
            log.error("direction: \"download\" || \"upload\"");
            log.error("sourceDir: source directory to be transfered");
            log.error("destDir: destination directory where the source directory will be stored to");
            log.error("");
            log.error("A user proxy certificate needs to be in place in /tmp");
            log.error("");
            log.error("Example: java " + Example.class.getName()
                + " chi-vm-4.isi.edu 2811 download /tmp/testdir /tmp/testdir");
            log.error("This will transfer chi-vm-4.isi.edu/tmp/testdir into /tmp/testdir");
            log.error("Note: The path to the directory on the GridFTP server must be give in UNIX style");
            System.exit(1);
        }

        String host = args[0];
        int port = new Integer(args[1]).intValue();
        String direction = args[2];
        String sourceDir = args[3];
        String destDir = args[4];

        Example tt = new Example(host, port, sourceDir, destDir, direction);
        tt.doTransfer();
    }

    public Example(String host, int port, String sourceDir, String destDir, String direction) {

        new MethodParameterChecker().verifyNotNull(host, port, sourceDir, destDir, direction);
        if (!direction.equals("download") && !direction.equals("upload")) {
            throw new IllegalArgumentException("Invalid direction: \"download\" || \"upload\"");
        }
        this.host = host;
        this.port = port;
        this.sourceDir = sourceDir;
        this.destDir = destDir;
        this.direction = direction;
    }

    /*
     * Create remote destination directory if it does not yet exist.
     */
    private void checkRemoteDirectory(String destDir) throws Exception {

        GridFTPClient client = null;
        try {
            FileNameUtil fnu = new FileNameUtil();
            String tmpDir = destDir;
            List<String> dirs = new LinkedList<String>();
            client = new GridFTPClient(this.host, this.port);
            client.authenticate(this.getDefaultCredential());
            dirs.add(tmpDir);
            while ((tmpDir = fnu.getUNIXParentDirName(tmpDir)) != null) {
                dirs.add(tmpDir);
            }
            for (int i = dirs.size() - 1; i > -1; i--) {
                if (!client.exists(dirs.get(i))) {
                    client.makeDir(dirs.get(i));
                }
            }
        } catch (Exception e) {
            throw new Exception("Cannot create remote destination directory.", e);
        } finally {
            try {
                if (client != null) {
                    client.close(true);
                }
            } catch (Exception e) {
                log.error("Can't close connection.", e);
            }
        }
    }

    /*
     * Create local destination directory if it does not yet exist.
     */
    private void checkLocalDirectory(String destDir) throws Exception {

        File f = new File(destDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("Cannot create local destination directory " + destDir);
            }
        }
    }

    /*
     * Two threads are used for a download/upload: In a download the data is being transfered from the server
     * into a pipe and another thread reads data from that pipe and untars it. In an upload the data is tared
     * into a pipe, and another threads reads the data from that pipe and transfers it to the server.
     */
    private void doTransfer() throws Exception {

        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            GSSCredential credential = this.getDefaultCredential();
            TwoPartyTarClientFactoryI clientFactory = new TwoPartyTarClientFactoryImpl();
            TwoPartyTarTransfer tptt = new TwoPartyTarTransfer(clientFactory, host, port, credential);
            if (tptt.isPopenDriverSupported()) {
                if (direction.equals("download")) {
                    this.checkLocalDirectory(destDir);
                    executorService.execute(new UntarFromPipeRunnable(pipeIn, destDir, new CompressUntar()));
                    tptt.downloadTarToPipe(sourceDir, pipeOut);
                } else {
                    this.checkRemoteDirectory(destDir);
                    executorService.execute(new TarToPipeRunnable(sourceDir, pipeOut, new CompressTar()));
                    tptt.uploadTarFromPipe(destDir, pipeIn, pipeOut);
                }
            } else {
                log.error("Popen driver is not supported by the GridFTP server " + host + ":" + port);
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    public GSSCredential getDefaultCredential() throws Exception {

        File proxyFile = new File(ConfigUtil.discoverProxyLocation());
        byte[] proxyBytes = new byte[(int) proxyFile.length()];
        FileInputStream in = new FileInputStream(proxyFile);
        in.read(proxyBytes);
        in.close();
        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        return manager.createCredential(proxyBytes, ExtendedGSSCredential.IMPEXP_OPAQUE,
            GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.INITIATE_AND_ACCEPT);
    }

}

