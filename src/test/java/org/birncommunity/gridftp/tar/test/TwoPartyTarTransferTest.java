package org.birncommunity.gridftp.tar.test;

import org.birncommunity.gridftp.tar.util.test.LocalCredentialHelper;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.birncommunity.gridftp.tar.PopenDriverUnsupportedException;
import org.birncommunity.gridftp.tar.TwoPartyTarClientFactoryI;
import org.birncommunity.gridftp.tar.TwoPartyTarTransfer;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSource;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.MarkerListener;
import org.ietf.jgss.GSSCredential;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TwoPartyTarTransferTest {

    private String tarAlias;
    private TwoPartyTarTransfer tptt;
    private GridFTPClient gridFtpClient;
    private TwoPartyTarClientFactoryI clientFactory;

    @Before
    public void setUp() throws Exception {
        this.tarAlias = "tar";
        String pathToCert = TwoPartyTarTransfer.class.getResource("/proxy.pem").getPath();
        gridFtpClient = mock(GridFTPClient.class);
        clientFactory = mock(TwoPartyTarClientFactoryI.class);
        tptt = new TwoPartyTarTransfer(clientFactory, "localhost", 2811, new LocalCredentialHelper()
            .getCredential(new File(pathToCert)));
        gridFtpClient = mock(GridFTPClient.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUploadTarSiteCommand_argNull() {
        tptt.createUploadTarSiteCommand(null);
    }

    @Test
    public void testCreateUploadTarSiteCommand() {
        String destDirName = "/tmp/dir";
        String expectedString = "SETDISKSTACK popen:argv=#" + tarAlias + "#xf#-#-C#" + destDirName + ",ordering";
        assertEquals(expectedString, tptt.createUploadTarSiteCommand(destDirName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDownloadTarSiteCommand_argNull() {
        tptt.createDownloadTarSiteCommand(null);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDownloadTarSiteCommand_Root() {
        tptt.createDownloadTarSiteCommand("/");
    }

    @Test
    public void testDownloadTarSiteCommand() {
        String parentDirName = "/tmp/parent";
        String dirName = "dir";
        String expectedString = "SETDISKSTACK popen:argv=#" + tarAlias + "#cf#-#-C#" + parentDirName + "#" + dirName;
        assertEquals(expectedString, tptt.createDownloadTarSiteCommand(parentDirName + "/" + dirName));
    }

    @Test(expected = PopenDriverUnsupportedException.class)
    public void testDownloadTarToFile_popenNotSupported() throws Exception {
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenThrow(new PopenDriverUnsupportedException(new Exception()));
        tptt.downloadTarToFile("/test", "/test");
    }

    @Test(expected = IOException.class)
    public void testDownloadTarToFile_ioException() throws Exception {
        doThrow(new IOException()).when(gridFtpClient).get(anyString(), any(File.class));
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenReturn(gridFtpClient);
        tptt.downloadTarToFile("/test", "/test");
    }

    @Test(expected = PopenDriverUnsupportedException.class)
    public void testDownloadTarToPipe_popenNotSupported() throws Exception {
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenThrow(new PopenDriverUnsupportedException(new Exception()));
        tptt.downloadTarToPipe("/test", new PipedOutputStream());
    }

    @Test(expected = IOException.class)
    public void testDownloadTarToPipe_ioException() throws Exception {
        doThrow(new IOException()).when(gridFtpClient).get(anyString(), any(DataSink.class), any(MarkerListener.class));
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenReturn(gridFtpClient);
        tptt.downloadTarToPipe("/test", new PipedOutputStream());
    }

    @Test(expected = PopenDriverUnsupportedException.class)
    public void testUploadTarFromFile_popenNotSupported() throws Exception {
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenThrow(new PopenDriverUnsupportedException(new Exception()));
        tptt.uploadTarFromFile("/test", "/test");
    }

    @Test(expected = IOException.class)
    public void testUploadTarFromFile_ioException() throws Exception {
        doThrow(new IOException()).when(gridFtpClient).put(any(File.class), anyString(), anyBoolean());
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenReturn(gridFtpClient);
        tptt.uploadTarFromFile("/test", "/test");
    }

    @Test(expected = PopenDriverUnsupportedException.class)
    public void testUploadTarFromPipe_popenNotSupported() throws Exception {
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenThrow(new PopenDriverUnsupportedException(new Exception()));
        tptt.uploadTarFromPipe("/test", new PipedInputStream(), new PipedOutputStream());
    }

    @Test(expected = IOException.class)
    public void testUploadTarFromPipe_ioException() throws Exception {
        doThrow(new IOException()).when(gridFtpClient).put(anyString(), any(DataSource.class),
            any(MarkerListener.class));
        when(clientFactory.createClient(anyString(), anyInt(), any(GSSCredential.class), anyString(), anyBoolean()))
            .thenReturn(gridFtpClient);
        tptt.uploadTarFromPipe("/test", new PipedInputStream(), new PipedOutputStream());
    }

}
