package mx.com.sagaji.android.dao.entity;

import com.atcloud.android.dao.engine.DatabaseRecord;

public class DetPedidoDAO implements DatabaseRecord {
    public int folio = 0;
    public String codigo = "";
    public String linea = "";
    public int orden = 0;
    public int cantidad = 0;
    public double precio = 0.0;
    public double precioiva = 0.0;
    public double importe = 0.0;
    public double importeiva = 0.0;
    public double priva = 0.0;
    public double iva = 0.0;
    public double total = 0.0;

    public DetPedidoDAO() {
    }

    public DetPedidoDAO(int folio, String codigo) {
        this.folio = folio;
        this.codigo = codigo;
    }

    public String getTable() {
        return "DetPedido";
    }

    public String getWhere() {
        return "folio = "+folio+" AND codigo = '"+codigo+"'";
    }

    public String getOrder() {
        return "folio, codigo";
    }
}
