package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;

public class MotivoDevolucionDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String clave = "";
    public String descripcion = "";

    public MotivoDevolucionDAO() {
    }

    public MotivoDevolucionDAO(String clave) {
        this.clave = clave;
    }

    public String getTable() {
        return "MotivoDevolucion";
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
