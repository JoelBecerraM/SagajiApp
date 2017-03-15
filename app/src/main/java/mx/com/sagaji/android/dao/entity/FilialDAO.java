package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;

public class FilialDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String filial = "";
    public String razonsocial = "";
    public String direccion = "";
    public String colonia = "";
    public String poblacion = "";
    public String estado = "";
    public String telefono = "";
    public String codigopostal = "";
    public String rfc = "";

    public FilialDAO() {
    }

    public FilialDAO(String filial) {
        this.filial = filial;
    }

    public String getTable() {
        return "Filial";
    }

    public String getWhere() {
        return "filial = '"+filial+"'";
    }

    public String getOrder() {
        return "filial";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
        insertStmt.bindString(3, tokens[2].trim());
        insertStmt.bindString(4, tokens[3].trim());
        insertStmt.bindString(5, tokens[4].trim());
        insertStmt.bindString(6, tokens[5].trim());
        insertStmt.bindString(7, tokens[6].trim());
        insertStmt.bindString(8, tokens[7].trim());
        insertStmt.bindString(9, tokens[8].trim());
    }
}