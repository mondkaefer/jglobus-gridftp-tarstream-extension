package org.birncommunity.gridftp.tar.compress.test;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.gridftp.tar.impl.compress.CompressTar;
import org.junit.Before;
import org.junit.Test;

public class CompressTarTest {

    private Log log = LogFactory.getLog(CompressTarTest.class);
    private CompressTar compressTar;
    private String testDir = "/TEST_DIR";
    private String nonExistingTestDir = "TEST_DIRR";
    private String unwritableTar = "/root/bla.tar";
    private String unreadableDir = "/root";

    @Before
    public void setUp() throws Exception {
        this.compressTar = new CompressTar();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTarDirectoryToFile_1stArgNull() throws Exception {
        this.compressTar.tarDirectoryToFile(null, new File("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTarDirectoryToFile_2ndArgNull() throws Exception {
        this.compressTar.tarDirectoryToFile(new File("test"), null);
    }

    @Test(expected = Exception.class)
    public void testTarDirectoryToFile_nonExistingDirectory() throws Exception {
        String dir = CompressTarTest.class.getResource(this.testDir).getPath();
        String tar = dir + ".tar";
        this.compressTar.tarDirectoryToFile(new File(nonExistingTestDir), new File(tar));
    }

    @Test(expected = Exception.class)
    public void testTarDirectoryToFile_unreadableDirectory() throws Exception {

        String dir = CompressTarTest.class.getResource(this.testDir).getPath();
        String tar = dir + ".tar";
        if (new File(unreadableDir).canRead()) {
            log.warn("Can read from " + unreadableDir + ". Skipping this test");
            throw new Exception();
        } else {
            this.compressTar.tarDirectoryToFile(new File(unreadableDir), new File(tar));
        }
    }

    @Test(expected = Exception.class)
    public void testTarDirectoryToFile_unwritableTarFile() throws Exception {
        String dir = CompressTarTest.class.getResource(this.testDir).getPath();
        File tmp = new File(unwritableTar);
        if (tmp.exists()) {
            if (tmp.canWrite()) {
                log.warn("Can write to " + unwritableTar + ". Skipping this test");
                throw new Exception();
            } else {
                this.compressTar.tarDirectoryToFile(new File(dir), new File(unwritableTar));
            }
        } else {
            tmp.createNewFile();
            if (tmp.exists()) {
                log.warn("Can create " + unwritableTar + ". Skipping this test");
                tmp.delete();
                throw new Exception();
            } else {
                this.compressTar.tarDirectoryToFile(new File(dir), new File(unwritableTar));
            }
        }
    }

}
