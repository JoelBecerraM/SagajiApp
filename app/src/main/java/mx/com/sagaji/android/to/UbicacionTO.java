package mx.com.sagaji.android.to;

import android.location.Location;
import java.io.Serializable;

/**
 * Created by jbecerra.
 */
public class UbicacionTO implements Serializable {
    public double latitud = 0.0d;
    public double longitud = 0.0d;
    public double accuracy = 0.0d;

    @Override
    public String toString() {
        return latitud + ";" + longitud + ";";
    }

    public void updateLocation(Location location) {
        latitud = location.getLatitude();
        longitud = location.getLongitude();
        accuracy = location.getAccuracy();
    }
}