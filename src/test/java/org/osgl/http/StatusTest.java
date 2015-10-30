package org.osgl.http;

import org.junit.Test;
import org.osgl._;
import org.osgl.util.C;

import java.io.*;
import java.util.List;

public class StatusTest extends TestBase {

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionShallBeThrownOutIfCodeIsLessThan100() {
        H.status(99);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionShallBeThrownOutIfCodeIsLargerThan599() {
        H.status(600);
    }

    @Test
    public void testInformationalStatus() {
        int n = _.random(C.range(100, 200));
        H.Status s = H.status(n);
        yes(s.isInformational());
    }

    @Test
    public void testClientErrorStatus() {
        int n = _.random(C.range(400, 500));
        H.Status s = H.status(n);
        yes(s.isClientError());
        yes(s.isError());
    }

    @Test
    public void testServerErrorStatus() {
        int n = _.random(C.range(500, 600));
        H.Status s = H.status(n);
        yes(s.isServerError());
        yes(s.isError());
    }

    @Test
    public void testRedirectionStatus() {
        int n = _.random(C.range(300, 400));
        H.Status s = H.status(n);
        yes(s.isRedirect());
    }

    @Test
    public void testSuccessfulStatus() {
        int n = _.random(C.range(200, 300));
        H.Status s = H.status(n);
        yes(s.isSuccess());
    }


    @Test
    public void predefinedStatusShallBeCached() {
        H.Status s1 = H.status(H.Status.OK.code());
        same(s1, H.Status.OK);
    }

    @Test
    public void customStatusShallNotBeCached() {
        H.Status s1 = H.status(550);
        H.Status s2 = H.status(550);
        assertNotSame(s1, s2);
    }

    @Test
    public void thereShallBePredefinedStatus() {
        no(H.Status.predefined().isEmpty());
    }

    @Test
    public void predefinedListShallBeSorted() {
        List<H.Status> l = H.Status.predefined();
        for (int i = 0; i < l.size() - 1; ) {
            H.Status s1 = l.get(i++);
            H.Status s2 = l.get(i);
            yes(s1.code() < s2.code());
        }
    }

    @Test
    public void valuesArrayShallMatchPredefinedList() {
        H.Status[] sa = H.Status.values();
        List<H.Status> l1 = H.Status.predefined();
        List<H.Status> l2 = C.listOf(sa);
        eq(l1, l2);
    }

    @Test
    public void deserializePredefinedStatusShallBeManaged() throws IOException, ClassNotFoundException {
        H.Status ok = H.Status.OK;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ok);
        byte[] ba = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object ok2 = ois.readObject();
        same(ok, ok2);
    }
}
