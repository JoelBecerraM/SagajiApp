package mx.com.sagaji.android.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class ActivityUtil {

    public static void rotar(Activity activity){
        int orientation = activity.getResources().getConfiguration().orientation;
        switch(orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }
}
