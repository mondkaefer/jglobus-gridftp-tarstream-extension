package org.birncommunity.gridftp.tar.compress.test;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.gridftp.tar.impl.compress.CompressUntar;
import org.junit.Before;
import org.junit.Test;

public class CompressUntarTest {

    private Log log = LogFactory.getLog(CompressUntarTest.class);
    private CompressUntar compressUntar;
    private String testDir = "/TEST_DIR";
    private String nonExistingTestTar = "TEST_DIRR.tar";
    private String unwritableDir = "/root";

    @Before
    public void setUp() throws Exception {
        this.compressUntar = new CompressUntar();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUntarFromFile_tarFileIsNull() throws Exception {
        this.compressUntar.untarFromFile(null, new File("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUntarFromFile_directoryIsNull() throws Exception {
        this.compressUntar.untarFromFile(new File("test"), null);
    }

    @Test(expected = Exception.class)
    public void testUntarFromFile_unwritableDirectory() throws Exception {
        String dir = CompressUntarTest.class.getResource(this.testDir).getPath();
        String tar = dir + ".tar";
        if (new File(unwritableDir).canWrite()) {
            log.warn("Can write to " + unwritableDir + ". Skipping this test");
            throw new Exception();
        } else {
            this.compressUntar.untarFromFile(new File(tar), new File(unwritableDir));
        }
    }

    @Test(expected = Exception.class)
    public void testUntarFromFile_nonExistingTarFile() throws Exception {
        String dir = CompressUntarTest.class.getResource(this.testDir).getPath();
        this.compressUntar.untarFromFile(new File(nonExistingTestTar), new File(dir));
    }

}
