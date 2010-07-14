package org.birncommunity.gridftp.tar;

import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.Session;
import org.ietf.jgss.GSSCredential;

public class TwoPartyTarClientFactoryImpl implements TwoPartyTarClientFactoryI {

    public GridFTPClient createClient(String host, int port, GSSCredential cred, String tarCommand,
        boolean doIntegrityCheck) throws Exception {

        GridFTPClient client = null;
        client = new GridFTPClient(host, port);
        client.authenticate(cred);
        if (doIntegrityCheck) {
            client.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
        }
        client.setType(Session.TYPE_IMAGE);

        try {
            client.site(tarCommand);
        } catch (Exception e) {
            throw new PopenDriverUnsupportedException(e);
        }
        client.setPassive();
        client.setLocalActive();
        return client;
    }

}
