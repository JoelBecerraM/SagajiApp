package mx.com.sagaji.android.dao.entity;

import java.util.Date;
import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Numero;

public class PromocionDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String codigo = "";
    public String descripcion = "";
    public int escala = 0;
    public String tipo = "";
    public double descuento = 0.0;
    public double preciooferta = 0.0;
    public Date fechainicio = new Date(0);
    public Date fechafin  = new Date(0);
    public String lentomovimiento = "";

    public PromocionDAO() {
    }

    public PromocionDAO(String codigo, int escala, String tipo) {
        this.codigo = codigo;
        this.escala = escala;
        this.tipo = tipo;
    }

    public String getTable() {
        return "Promocion";
    }

    public String getWhere() {
        return "codigo = '"+codigo+"' AND escala = "+escala+" AND tipo = '"+tipo+"'";
    }

    public String getOrder() {
        return "codigo, escala, tipo";
    }

    public void loadRecord(SQLiteStatement insertStmt, String[] tokens) {
        insertStmt.bindString(1, tokens[1].trim());
        insertStmt.bindString(2, tokens[2].trim());
        insertStmt.bindLong(3, Numero.getIntFromString(tokens[3].trim()));
        insertStmt.bindString(4, tokens[4].trim());
        insertStmt.bindDouble(5, Numero.getDoubleFromString(tokens[5].trim()));
        insertStmt.bindDouble(6, Numero.getDoubleFromString(tokens[6].trim()));
        insertStmt.bindString(7, tokens[7].trim()+" 00:00:00");
        insertStmt.bindString(8, tokens[8].trim()+" 00:00:00");
        insertStmt.bindString(9, tokens[9].trim());
    }
}
