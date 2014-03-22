package org.osgl.http;

import org.osgl.cache.CacheService;
import org.osgl.cache.CacheServiceProvider;
import org.osgl.util.E;

import java.util.Locale;

/**
 * Created by luog on 20/03/2014.
 */
public class Config {

    private static CacheServiceProvider cacheServiceProvider;

    public static void setCacheServiceProvider(CacheServiceProvider cacheProvider) {
        E.NPE(cacheProvider);
        Config.cacheServiceProvider = cacheProvider;
    }

    public static CacheService cacheService() {
        if (null == cacheServiceProvider) {
            cacheServiceProvider = CacheServiceProvider.Impl.Auto;
        }
        return cacheServiceProvider.get();
    }

    private static Locale defLocale = Locale.getDefault();

    public static void setDefaultLocale(Locale locale) {
        E.NPE(locale);
        defLocale = locale;
    }

    public static Locale defaultLocale() {
        return defLocale;
    }

}
