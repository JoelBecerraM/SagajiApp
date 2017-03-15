package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;

public class DireccionEntregaDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String cliente = "";
    public String direccion = "";
    public String descripcion = "";

    public DireccionEntregaDAO() {
    }

    public DireccionEntregaDAO(String cliente, String direccion) {
        this.cliente = cliente;
        this.direccion = direccion;
    }

    public String getTable() {
        return "DireccionEntrega";
    }

    public String getWhere() {
        return "cliente = '"+cliente+"' AND direccion = '"+direccion+"'";
    }

    public String getOrder() {
        return "cliente, direccion";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
        insertStmt.bindString(3, tokens[2].trim());
    }
}