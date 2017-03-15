package mx.com.sagaji.android.to;

import java.io.Serializable;

/**
 * Created by jbecerra.
 */
public class DireccionEntregaTO implements Serializable {
    public String direccion = "";
    public String descripcion = "";

    public DireccionEntregaTO() {
    }

    public DireccionEntregaTO(String direccion, String descripcion) {
        this.direccion = direccion;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}