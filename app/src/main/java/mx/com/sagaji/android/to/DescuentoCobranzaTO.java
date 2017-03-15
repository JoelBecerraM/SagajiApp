package mx.com.sagaji.android.to;

public class DescuentoCobranzaTO {
    public String linea = "";
    public double prdescuento = 0.0;
    public double prdescuentomax = 0.0;
    public int plazo = 0;
    public int plazomax = 0;
    public double monto = 0.0;
    public double montomax = 0.0;
    public String identificador = "";
    public String opcion = "";

    @Override
    public String toString() {
        return linea;
    }
}