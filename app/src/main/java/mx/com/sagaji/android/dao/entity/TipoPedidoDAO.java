package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;

import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;

public class TipoPedidoDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String clave = "";
    public String descripcion = "";

    public TipoPedidoDAO() {
    }

    public TipoPedidoDAO(String clave) {
        this.clave = clave;
    }

    public String getTable() {
        return "TipoPedido";
    }

    public String getWhere() {
        return "clave = '"+clave+"'";
    }

    public String getOrder() {
        return "clave";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
    }
}
