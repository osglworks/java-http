package org.osgl.http;

import org.junit.Test;
import org.osgl.util.C;

import java.util.List;

public class StatusTest extends TestBase {

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionShallBeThrownOutIfCodeIsLessThan100() {
        H.Status.valueOf(99);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionShallBeThrownOutIfCodeIsLargerThan599() {
        H.Status.valueOf(600);
    }

    @Test
    public void predefinedStatusShallBeCached() {
        H.Status s1 = H.Status.valueOf(H.Status.OK.code());
        same(s1, H.Status.OK);
    }

    @Test
    public void customStatusShallNotBeCached() {
        H.Status s1 = H.Status.valueOf(550);
        H.Status s2 = H.Status.valueOf(550);
        assertNotSame(s1, s2);
    }

    @Test
    public void thereShallBePredefinedStatus() {
        no(H.Status.predefined().isEmpty());
    }

    @Test
    public void predefinedShallBeSorted() {
        List<H.Status> l = H.Status.predefined();
        for (int i = 0, j = 1; i < l.size() - 1; ++i, ++j) {
            H.Status si = l.get(i);
            H.Status sj = l.get(j);
            yes(si.code() < sj.code());
        }
    }

    @Test
    public void valuesShallMatchPredefined() {
        H.Status[] sa = H.Status.values();
        List<H.Status> l1 = H.Status.predefined();
        List<H.Status> l2 = C.listOf(sa);
        eq(l1, l2);
    }
}
