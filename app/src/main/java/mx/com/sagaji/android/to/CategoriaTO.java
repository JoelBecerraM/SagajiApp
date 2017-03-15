package mx.com.sagaji.android.to;

/**
 * Created by jbecerra.
 */
public class CategoriaTO {
    public String clave = "";
    public String descripcion = "";

    public CategoriaTO() {
    }

    public CategoriaTO(String clave, String descripcion) {
        this.clave = clave;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}