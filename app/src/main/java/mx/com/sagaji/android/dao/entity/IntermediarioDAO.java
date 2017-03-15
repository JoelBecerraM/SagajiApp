package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;

import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;

public class IntermediarioDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String intermediario = "";
    public String nombre = "";
    public String plaza = "";

    public IntermediarioDAO() {
    }

    public IntermediarioDAO(String intermediario) {
        this.intermediario = intermediario;
    }

    public String getTable() {
        return "Intermediario";
    }

    public String getWhere() {
        return "intermediario = '"+intermediario+"'";
    }

    public String getOrder() {
        return "intermediario";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
        insertStmt.bindString(3, tokens[2].trim());
    }
}