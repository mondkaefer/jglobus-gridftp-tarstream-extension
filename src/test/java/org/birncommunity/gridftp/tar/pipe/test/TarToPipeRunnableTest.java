package org.birncommunity.gridftp.tar.pipe.test;

import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.birncommunity.gridftp.tar.TarI;
import org.birncommunity.gridftp.tar.TarToPipeRunnable;
import org.birncommunity.gridftp.tar.UntarFromPipeRunnable;
import org.birncommunity.gridftp.tar.UntarI;
import org.birncommunity.gridftp.tar.impl.compress.CompressTar;
import org.birncommunity.gridftp.tar.impl.compress.CompressUntar;
import org.birncommunity.util.FileSystemHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class TarToPipeRunnableTest {

    private Log log = LogFactory.getLog(TarToPipeRunnableTest.class);
    private FileSystemHelper fsh;
    private String testDir = "/TEST_DIR";
    private TarI tar;
    private UntarI untar;

    @Before
    public void setUp() throws Exception {

        this.fsh = new FileSystemHelper();
        this.tar = new CompressTar();
        this.untar = new CompressUntar();
    }

    @Test
    public void testConstructor() throws Exception {
        new TarToPipeRunnable("test", new PipedOutputStream(), tar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_1stArgNull() throws Exception {
        new TarToPipeRunnable(null, new PipedOutputStream(), tar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_2ndArgNull() throws Exception {
        new TarToPipeRunnable("test", null, tar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_3rdArgNull() throws Exception {
        new TarToPipeRunnable("test", new PipedOutputStream(), null);
    }

    @Test
    public void testThreadPipe1() throws Exception {

        File dir = new File(TarToPipeRunnableTest.class.getResource(this.testDir).getPath());
        File dir2 = new File(dir.getAbsolutePath() + "_COMPARISON");

        if (!dir.canRead() || !dir.isDirectory()) {
            fail(dir.getAbsolutePath() + " is no directory or cannot be read");
        }
        fsh.removeDirectory(dir2);
        if (!dir2.mkdirs()) {
            fail("Cannot create directory " + dir2.getAbsolutePath());
        }
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        new Thread(new TarToPipeRunnable(dir.getAbsolutePath(), pipeOut, tar)).start();
        new UntarFromPipeRunnable(pipeIn, dir2.getAbsolutePath(), untar).run();
        if (!fsh.isDirContentsEqual(dir, new File(dir2, dir.getName()))) {
            fail("Directories are not equal");
        }
        fsh.removeDirectory(dir2);
    }

    @Test
    public void testThreadPipe2() throws Exception {

        File dir = new File(TarToPipeRunnableTest.class.getResource(this.testDir).getPath());
        File dir2 = new File(dir.getAbsolutePath() + "_COMPARISON");

        if (!dir.canRead() || !dir.isDirectory()) {
            fail(dir.getAbsolutePath() + " is no directory or cannot be read");
        }
        fsh.removeDirectory(dir2);
        if (!dir2.mkdirs()) {
            fail("Cannot create directory " + dir2.getAbsolutePath());
        }
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        Thread t1 = new Thread(new UntarFromPipeRunnable(pipeIn, dir2.getAbsolutePath(), untar));
        t1.start();
        new TarToPipeRunnable(dir.getAbsolutePath(), pipeOut, tar).run();
        pipeOut.flush();
        t1.join();
        if (!fsh.isDirContentsEqual(dir, new File(dir2, dir.getName()))) {
            fail("Directories are not equal");
        }
        fsh.removeDirectory(dir2);
    }

    @Test
    public void testThreadPipe3() throws Exception {

        File dir = new File(TarToPipeRunnableTest.class.getResource(this.testDir).getPath());
        File dir2 = new File(dir.getAbsolutePath() + "_COMPARISON");

        if (!dir.canRead() || !dir.isDirectory()) {
            fail(dir.getAbsolutePath() + " is no directory or cannot be read");
        }
        fsh.removeDirectory(dir2);
        if (!dir2.mkdirs()) {
            fail("Cannot create directory " + dir2.getAbsolutePath());
        }
        PipedOutputStream pipeOut = new PipedOutputStream();
        PipedInputStream pipeIn = new PipedInputStream(pipeOut);
        Thread t1 = new Thread(new UntarFromPipeRunnable(pipeIn, dir2.getAbsolutePath(), untar));
        Thread t2 = new Thread(new TarToPipeRunnable(dir.getAbsolutePath(), pipeOut, tar));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        if (!fsh.isDirContentsEqual(dir, new File(dir2, dir.getName()))) {
            fail("Directories are not equal");
        }
        fsh.removeDirectory(dir2);
    }

}
