package org.birncommunity.gridftp.tar.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.util.ConfigUtil;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class LocalCredentialHelper {

    private Log log = LogFactory.getLog(LocalCredentialHelper.class);
    private String unixChmod = "chmod";

    public GSSCredential getDefaultCredential() throws IOException, GSSException {

        return this.getCredential(new File(ConfigUtil.discoverProxyLocation()));
    }

    public void saveDefaultCredential(GSSCredential cred) throws GSSException, IOException {

        File f = new File(ConfigUtil.discoverProxyLocation());
        this.saveCredential(cred, f);
        this.restrictFilePermissions(f);
    }

    public GSSCredential getCredential(File proxyFile) throws IOException, GSSException {
        
        byte[] proxyBytes = new byte[(int) proxyFile.length()];
        FileInputStream in = new FileInputStream(proxyFile);
        in.read(proxyBytes);
        in.close();
        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        return manager.createCredential(proxyBytes, ExtendedGSSCredential.IMPEXP_OPAQUE,
                GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.INITIATE_AND_ACCEPT);
    }

    public void saveCredential(GSSCredential cred, File f) throws GSSException, IOException {

        byte[] proxyBytes = ((ExtendedGSSCredential) cred).export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        FileOutputStream out = new FileOutputStream(f);
        out.write(proxyBytes);
        out.close();
        this.restrictFilePermissions(f);
    }

    public void setUnixPathToChmod(String unixChmod) {
        
        this.unixChmod = unixChmod;
    }

    private void restrictFilePermissions(File f) throws IOException {
        
        // Restrict permissions if OS is a UNIX
        if (ConfigUtil.getOS() == ConfigUtil.UNIX_OS) {
            Runtime.getRuntime().exec(unixChmod + " 600 " + f.getAbsolutePath());
        }
    }
}
