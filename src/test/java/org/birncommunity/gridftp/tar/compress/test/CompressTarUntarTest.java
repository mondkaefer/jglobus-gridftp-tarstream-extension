package org.birncommunity.gridftp.tar.compress.test;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.gridftp.tar.impl.compress.CompressTar;
import org.birncommunity.gridftp.tar.impl.compress.CompressUntar;
import org.birncommunity.util.FileSystemHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class CompressTarUntarTest {

    private Log log = LogFactory.getLog(CompressTarUntarTest.class);
    private FileSystemHelper fsh;
    private CompressTar compressTar;
    private CompressUntar compressUntar;
    private String testDir = "/TEST_DIR";

    @Before
    public void setUp() throws Exception {
        this.fsh = new FileSystemHelper();
        this.compressTar = new CompressTar();
        this.compressUntar = new CompressUntar();
    }

    @Test
    public void testTarUntar() throws Exception {
        File dir = new File(CompressTarTest.class.getResource(this.testDir).getPath());
        File cvsDir = new File(dir, "CVS");
        File cvsIgnore = new File(dir, ".cvsignore");
        File dir2 = new File(dir.getAbsolutePath() + "_COMPARISON");
        File tar = new File(dir.getAbsolutePath() + ".tar");
        if (!dir.canRead() || !dir.isDirectory()) {
            fail(dir.getAbsolutePath() + " is no directory or cannot be read");
        }
        try {
            fsh.removeDirectory(dir2);
            if (!dir2.mkdirs()) {
                fail("Cannot create directory " + dir2.getAbsolutePath());
            }
            fsh.removeFile(tar);
            this.createCVSFiles(cvsDir, cvsIgnore);
            this.compressTar.tarDirectoryToFile(dir, tar);
            this.compressUntar.untarFromFile(tar, dir2);
            if (!fsh.isDirContentsEqual(dir, new File(dir2, dir.getName()))) {
                fail("Directories are not equal");
            }
            fsh.removeFile(tar);
            fsh.removeDirectory(dir2);
        } finally {
            this.removeCVSFiles(cvsDir, cvsIgnore);
        }
    }

    private void createCVSFiles(File cvsDir, File cvsIgnore) {
        if (!cvsDir.mkdir()) {
            fail("Failed to create CVS dir " + cvsDir.getAbsolutePath());
        }
        try {
            cvsIgnore.createNewFile();
        } catch (Exception e) {
            fail("Failed to create " + cvsIgnore.getAbsolutePath());
        }
    }

    private void removeCVSFiles(File cvsDir, File cvsIgnore) {
        if (!cvsDir.delete()) {
            fail("Failed to delete CVS dir " + cvsDir.getAbsolutePath());
        }
        if (!cvsIgnore.delete()) {
            fail("Failed to delete .cvsignore file " + cvsIgnore.getAbsolutePath());
        }
    }

}
