package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Numero;

/**
 * Created by jbecerra.
 */
public class ParametroDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String parametro = "";
    public String valor = "";
    public int activo = 0;

    public ParametroDAO() {
    }

    public ParametroDAO(String parametro) {
        this.parametro = parametro;
    }

    @Override
    public String getTable() {
        return "Parametro";
    }

    @Override
    public String getWhere() {
        return "parametro = '"+parametro+"'";
    }

    @Override
    public String getOrder() {
        return "parametro";
    }

    @Override
    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
        insertStmt.bindLong(3, Numero.getIntFromString(tokens[2].trim()));
    }

    @Override
    public String toString() {
        return parametro+";"+valor;
    }
}
