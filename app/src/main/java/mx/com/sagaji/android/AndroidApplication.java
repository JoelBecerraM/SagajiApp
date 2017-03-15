package mx.com.sagaji.android;


import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import java.io.File;

import mx.com.sagaji.android.to.ConfiguracionTO;

/**
 * Created by jbecerra.
 */
public class AndroidApplication extends MultiDexApplication {
    private static Context context;
    private static File storage;
    private static ConfiguracionTO configuracionTO;

    @SuppressWarnings("static-access")
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
        this.storage = getApplicationStorage();
    }

    public static Context getAppContext() {
        return context;
    }

    public static File getStorage() {
        return storage;
    }

    private static File getApplicationStorage() {
        storage = context.getFilesDir();
        return storage;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void setConfiguracion(ConfiguracionTO cconfiguracionTO) {
        configuracionTO = cconfiguracionTO;
    }

    public static ConfiguracionTO getConfiguracion() {
        return configuracionTO;
    }
}