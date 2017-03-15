package mx.com.sagaji.android.dao.entity;

import com.atcloud.android.dao.engine.DatabaseRecord;

/**
 * Created by jbecerra.
 */
public class FolioDAO implements DatabaseRecord {
    public String tipo = "";
    public int folio = 0;

    public FolioDAO() {
    }

    public FolioDAO(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String getTable() {
        return "Folios";
    }

    @Override
    public String getWhere() {
        return "tipo = '"+tipo+"'";
    }

    @Override
    public String getOrder() {
        return "tipo";
    }

    @Override
    public String toString() {
        return tipo+";"+folio;
    }
}