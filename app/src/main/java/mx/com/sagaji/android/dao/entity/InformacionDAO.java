package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Numero;
import java.util.Date;

/**
 * Created by jbecerra.
 */
public class InformacionDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String filial = "";
    public String intermediario = "";
    public String token = "";
    public Date fechaultimasincronizacion = new Date(0);
    public Date fechacentral = new Date(0);

    public InformacionDAO() {
    }

    public InformacionDAO(String filial, String intermediario) {
        this.filial = filial;
        this.intermediario = intermediario;
    }

    public String getTable() {
        return "Informacion";
    }

    public String getWhere() {
        return "filial = " + filial + " AND intermediario = '" + intermediario + "'";
    }

    public String getOrder() {
        return "filial, intermediario";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
        insertStmt.bindString(3, tokens[2].trim());
        insertStmt.bindString(4, tokens[3].trim());
        insertStmt.bindString(5, tokens[4].trim());
    }
}
