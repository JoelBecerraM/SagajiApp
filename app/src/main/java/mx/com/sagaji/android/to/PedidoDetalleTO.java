package mx.com.sagaji.android.to;

import com.atcloud.android.util.Numero;

public class PedidoDetalleTO {
    public String codigo = "";
    public String descripcion = "";
    public String unidadmedida = "";
    public String linea = "";
    public String lentomovimiento = "";
    public String status = "";
    public boolean promocion = false;
    public int cantidadsurtir = 0;
    public int cantidad = 0;
    public double precio = 0.0;
    public double precioiva = 0.0;
    public double importe = 0.0;
    public double importeiva = 0.0;
    public double priva = 0.0;
    public double iva = 0.0;
    public double totalsurtir = 0.0;
    public double total = 0.0;
    
    public void calcula() {
        precioiva = Numero.redondea(precio * (1.0 + priva));
        importe = Numero.redondea(precio * cantidad);
        importeiva = Numero.redondea(precioiva * cantidad);
        iva = Numero.redondea(importeiva - importe);
        totalsurtir = Numero.redondea(precioiva * cantidadsurtir);
        total = Numero.redondea(importeiva);
    }
}
