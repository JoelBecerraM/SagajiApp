package mx.com.sagaji.android.dao.entity;

import com.atcloud.android.dao.engine.DatabaseRecord;

public class DetCobranzaDAO implements DatabaseRecord {
    public int folio = 0;
    public int renglon = 0;
    public String documento = "";
    public String tipodocumento = "";
    public String tipopago = "";
    public String referencia = "";
    public String banco = "";
    public String fechacobro = "";
    public double pago = 0.0;
    public double importe = 0.0;
    public double prdescuento = 0.0;
    public double descuento = 0.0;

    public DetCobranzaDAO() {
    }

    public DetCobranzaDAO(int folio, int renglon) {
        this.folio = folio;
        this.renglon = renglon;
    }

    public String getTable() {
        return "DetCobranza";
    }

    public String getWhere() {
        return "folio = "+folio+" AND renglon = "+renglon;
    }

    public String getOrder() {
        return "folio, renglon";
    }
}