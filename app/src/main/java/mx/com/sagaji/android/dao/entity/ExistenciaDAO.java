package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Numero;

public class ExistenciaDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String filial = "";
    public String codigo = "";
    public int existencia = 0;

    public ExistenciaDAO() {
    }

    public ExistenciaDAO(String filial, String codigo) {
        this.filial = filial;
        this.codigo = codigo;
    }

    public String getTable() {
        return "Existencia";
    }

    public String getWhere() {
        return "filial = '"+filial+"' AND codigo = '"+codigo+"'";
    }

    public String getOrder() {
        return "filial, codigo";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
        insertStmt.bindLong(3, Numero.getIntFromString(tokens[2].trim()));
    }
}
