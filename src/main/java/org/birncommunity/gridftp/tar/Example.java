package org.birncommunity.gridftp.tar;

import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.util.MethodParameterChecker;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.Session;
import org.globus.util.ConfigUtil;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;

public class Example {

    private static Log log = LogFactory.getLog(Example.class);
    private String host;
    private int port;
    private String source;
    private String dest;
    private String direction;

    public static void main(String[] args) throws Exception {

        if (args.length != 5 || args[0].equals("-h") || args[0].equals("--help")) {
            log.error("Usage: java " + Example.class.getName()
                + " <host> <port> <direction> <sourceFile> <base destFile>");
            log.error("host: GridFTP server hostname");
            log.error("port: GridFTP server port");
            log.error("direction: \"download\" || \"upload\"");
            log.error("sourceFile: source file");
            log.error("destFile: destination file");
            log.error("");
            log.error("A user proxy certificate needs to be in place in /tmp");
            log.error("");
            log.error("Example: java " + Example.class.getName()
                + " chi-vm-4.isi.edu 2811 download /tmp/testfile /tmp/testfile");
            log.error("This will transfer chi-vm-4.isi.edu/tmp/testfile into /tmp/testfile");
            System.exit(1);
        }

        String host = args[0];
        int port = new Integer(args[1]).intValue();
        String direction = args[2];
        String source = args[3];
        String dest = args[4];

        new Example(host, port, source, dest, direction).doTransfer();
    }

    public Example(String host, int port, String source, String dest, String direction) {

        new MethodParameterChecker().verifyNotNull(host, port, source, dest, direction);
        if (!direction.equals("download") && !direction.equals("upload")) {
            throw new IllegalArgumentException("Invalid direction: \"download\" || \"upload\"");
        }
        this.host = host;
        this.port = port;
        this.source = source;
        this.dest = dest;
        this.direction = direction;
    }

    public void doTransfer() throws Exception {

        GridFTPClient client = null;
        try {
            client = new GridFTPClient(host, port);
            client.authenticate(this.getDefaultCredential());
            client.setType(Session.TYPE_IMAGE);
            client.setPassive();
            client.setLocalActive();
            if (direction.equals("download")) {
                client.get(source, new File(dest));
            } else {
                client.put(new File(source), dest, false);
            }
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
