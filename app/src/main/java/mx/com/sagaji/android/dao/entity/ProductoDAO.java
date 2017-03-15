package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;

import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Numero;

public class ProductoDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String codigo = "";
    public String descripcion = "";
    public String unidadmedida = "";
    public double precio = 0.0;
    public int existencia = 0;
    public String linea = "";
    public String aceptadevoluciones = "";
    public String codigosustituto = "";
    public String codigobarras = "";

    public ProductoDAO() {
    }

    public ProductoDAO(String codigo) {
        this.codigo = codigo;
    }

    public String getTable() {
        return "Producto";
    }

    public String getWhere() {
        return "codigo = '" + codigo + "'";
    }

    public String getOrder() {
        return "codigo";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[0].trim());
        insertStmt.bindString(2, tokens[1].trim());
        insertStmt.bindString(3, tokens[2].trim());
        insertStmt.bindDouble(4, Numero.getDoubleFromString(tokens[3].trim()));
        insertStmt.bindLong(5, Numero.getIntFromString(tokens[4].trim()));
        insertStmt.bindString(6, tokens[5].trim());
        insertStmt.bindString(7, tokens[6].trim());
        insertStmt.bindString(8, tokens[7].trim());
        insertStmt.bindString(9, tokens[8].trim());
    }
}