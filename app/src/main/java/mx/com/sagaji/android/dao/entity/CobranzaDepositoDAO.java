package mx.com.sagaji.android.dao.entity;

import com.atcloud.android.dao.engine.DatabaseRecord;

public class CobranzaDepositoDAO implements DatabaseRecord {
    public int folio = 0;
    public String referencia = "";
    public String banco = "";
    public String fechacobro = "";

    public CobranzaDepositoDAO() {
    }

    public CobranzaDepositoDAO(int folio) {
        this.folio = folio;
    }

    //
    // DatabaseRecord
    //
    public String getTable() {
        return "CobranzaDeposito";
    }

    public String getWhere() {
        return "folio = "+folio;
    }

    public String getOrder() {
        return "folio";
    }

    //
    //
    //
    @Override
    public String toString() {
        return folio+";"+fechacobro;
    }
}