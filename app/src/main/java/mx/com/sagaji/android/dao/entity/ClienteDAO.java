package mx.com.sagaji.android.dao.entity;

import android.database.sqlite.SQLiteStatement;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Numero;

public class ClienteDAO implements DatabaseRecord, DatabaseRecordLoad {
    public String cliente = "";
    public String razonsocial = "";
    public String propietario = "";
    public String direccion = "";
    public String colonia = "";
    public String poblacion = "";
    public String entidadfederativa = "";
    public String telefono = "";
    public String codigopostal = "";
    public String rfc = "";
    public String codigobarras = "";
    public String aceptasustitutos = "";
    public String estado = "";
    public String perfil = "";
    public String separa = "";
    public String tipo = "";
    public double mnsaldo = 0.0;
    public double mnsepara = 0.0;
    public double mncobrado = 0.0;
    public double mnventa = 0.0;
    public double mnvencido = 0.0;
    public double mncobradocod = 0.0;
    public int diasgracia = 0;
    public String referencia = "";
    public int totfac = 0;
    public String formaenvio = "";

    public ClienteDAO() {
    }

    public ClienteDAO(String cliente) {
        this.cliente = cliente;
    }

    public String getTable() {
        return "Cliente";
    }

    public String getWhere() {
        return "cliente = '" + cliente + "'";
    }

    public String getOrder() {
        return "cliente";
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
        insertStmt.bindString(10, tokens[9].trim());
        insertStmt.bindString(11, tokens[10].trim());
        insertStmt.bindString(12, tokens[11].trim());
        insertStmt.bindString(13, tokens[12].trim());
        insertStmt.bindString(14, tokens[13].trim());
        insertStmt.bindString(15, tokens[14].trim());
        insertStmt.bindString(16, tokens[15].trim());
        insertStmt.bindDouble(17, Numero.getDoubleFromString(tokens[16].trim()));
        insertStmt.bindDouble(18, Numero.getDoubleFromString(tokens[17].trim()));
        insertStmt.bindDouble(19, Numero.getDoubleFromString(tokens[18].trim()));
        insertStmt.bindDouble(20, Numero.getDoubleFromString(tokens[19].trim()));
        insertStmt.bindDouble(21, Numero.getDoubleFromString(tokens[20].trim()));
        insertStmt.bindDouble(22, Numero.getDoubleFromString(tokens[21].trim()));
        insertStmt.bindLong(23, Numero.getIntFromString(tokens[22].trim()));
        insertStmt.bindString(24, tokens[23].trim());
        insertStmt.bindLong(25, Numero.getIntFromString(tokens[24].trim()));
        insertStmt.bindString(26, tokens[25].trim());
    }
}