package mx.com.sagaji.android.to;

import com.atcloud.android.util.Numero;

public class DevolucionDetalleTO {
    public String documento = "";
    public String codigo = "";
    public String descripcion = "";
    public String unidadmedida = "";
    public String linea = "";
    public int cantidad = 0;
    public double precio = 0.0;
    public double total = 0.0;
    public String causa = "";
    public double descuento = 0.0;
    public String tipo = "";

    public void calcula() {
        total = Numero.redondea(precio * cantidad);
    }
}
