package mx.com.sagaji.android.to;

import java.io.Serializable;

public class PedidoGuardarTO implements Serializable {
    private static final long serialVersionUID = 1L;
    public boolean salir = false;
    public String tipo = "";
    public String claveenvio = "";
    public String observaciones = "";
}
