package mx.com.sagaji.android.dao.entity;

import com.atcloud.android.dao.engine.DatabaseRecord;

public class DetDevolucionDAO implements DatabaseRecord {
    public int folio = 0;
    public String documento = "";
    public String codigo = "";
    public String linea = "";
    public int cantidad = 0;
    public double precio = 0.0;
    public double total = 0.0;
    public String causa = "";
    public double descuento = 0.0;
    public String tipo = "";

    public DetDevolucionDAO() {
    }

    public DetDevolucionDAO(int folio, String documento, String codigo) {
        this.folio = folio;
        this.documento = documento;
        this.codigo = codigo;
    }

    public String getTable() {
        return "DetDevolucion";
    }

    public String getWhere() {
        return "folio = "+folio+" AND documento = '"+documento+"' AND codigo = '"+codigo+"'";
    }

    public String getOrder() {
        return "folio, documento, codigo";
    }
}
