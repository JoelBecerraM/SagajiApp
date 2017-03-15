package mx.com.sagaji.android.to;

public class FacturaDevolucionDetalleTO {
    public String DOC;
    public int PARTIDA;
    public String CODPROD;
    public int CANTIDAD;
    public String UM;
    public double PU;
    public double SUBTOTAL;
    public String DESCRIP;

    @Override
    public String toString() {
        return CODPROD+";"+DESCRIP;
    }
}
