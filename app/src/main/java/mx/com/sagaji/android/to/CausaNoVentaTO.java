package mx.com.sagaji.android.to;

import java.io.Serializable;

/**
 * Created by jbecerra.
 */
public class CausaNoVentaTO implements Serializable {
    public String causa = "";
    public String descripcion = "";

    public CausaNoVentaTO() {
    }

    public CausaNoVentaTO(String causa, String descripcion) {
        this.causa = causa;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}