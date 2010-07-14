package org.birncommunity.gridftp.tar.pipe.test;

import java.io.PipedInputStream;
import org.birncommunity.gridftp.tar.TwoPartyTarTransfer;
import org.birncommunity.gridftp.tar.UntarFromPipeRunnable;
import org.birncommunity.gridftp.tar.UntarI;
import org.birncommunity.gridftp.tar.impl.compress.CompressUntar;
import org.junit.Before;
import org.junit.Test;

public class UntarFromPipeRunnableTest {

    private UntarI untar;
    TwoPartyTarTransfer tptt;

    @Before
    public void setUp() throws Exception {
        this.untar = new CompressUntar();
    }

    @Test
    public void testConstructor() throws Exception {
        new UntarFromPipeRunnable(new PipedInputStream(), "test", untar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_1stArgNull() throws Exception {
        new UntarFromPipeRunnable(null, "test", untar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_2ndArgNull() throws Exception {
        new UntarFromPipeRunnable(new PipedInputStream(), null, untar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_3rdArgNull() throws Exception {
        new UntarFromPipeRunnable(new PipedInputStream(), "test", null);
    }
}
