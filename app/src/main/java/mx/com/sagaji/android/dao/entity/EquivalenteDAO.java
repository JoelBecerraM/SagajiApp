package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Numero;

public class EquivalenteDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String codigo = "";
    public String equivalente = "";
    public String descripcion = "";
    public double precio = 0.0;
    public int cantidad = 0;
    public String lineaproveedor = "";
    public String linea = "";

    public EquivalenteDAO() {
    }

    public EquivalenteDAO(String codigo, String equivalente) {
        this.codigo = codigo;
        this.equivalente = equivalente;
    }

    public String getTable() {
        return "Equivalente";
    }

    public String getWhere() {
        return "codigo = '"+codigo+"' AND equivalente = '"+equivalente+"'";
    }

    public String getOrder() {
        return "codigo, equivalente";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[1].trim());
        insertStmt.bindString(2, tokens[2].trim());
        insertStmt.bindString(3, tokens[3].trim());
        insertStmt.bindDouble(4, Numero.getDoubleFromString(tokens[4].trim()));
        insertStmt.bindLong(5, Numero.getIntFromString(tokens[5].trim()));
        insertStmt.bindString(6, tokens[6].trim());
        insertStmt.bindString(7, tokens[7].trim());
    }
}
