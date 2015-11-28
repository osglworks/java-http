package org.osgl.http;

import org.junit.Before;
import org.junit.Test;
import org.osgl.$;

public class CurrentTest extends TestBase {

    H.Session sess;

    @Before
    public void prepare() {
        sess = new H.Session();
    }

    @Test
    public void setAndGetSession() {
        Current.session(sess);
        same(sess, Current.session());
    }

    @Test
    public void setAndGetSessionInDifferentThreads() throws Exception {
        Current.session(sess);
        final $.Var<H.Session> bag = $.var(null);
        bag.set(Current.session());
        same(sess, bag.get());
        new Thread() {
            @Override
            public void run() {
                bag.set(Current.session());
            }
        }.start();
        Thread.sleep(100);
        assertNull(bag.get());
     }
}
