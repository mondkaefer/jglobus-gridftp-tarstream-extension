package org.birncommunity.gridftp.tar;

import org.globus.ftp.GridFTPClient;
import org.ietf.jgss.GSSCredential;

public interface TwoPartyTarClientFactoryI {

    public GridFTPClient createClient(String host, int port, GSSCredential cred, String tarCommand,
        boolean doIntegrityCheck) throws Exception;

}
