package mx.com.sagaji.android.dao;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.atcloud.android.dao.engine.DatabaseServices;
import java.io.File;

import mx.com.sagaji.android.AndroidApplication;

/**
 * Created by jbecerra.
 */
public class DatabaseOperacionesOpenHelper {
    public static String LOGTAG = DatabaseOperacionesOpenHelper.class.getCanonicalName();
    private static DatabaseOperacionesOpenHelper singleton = null;
    private File database = null;

    private DatabaseOperacionesOpenHelper() {
        database = new File(AndroidApplication.getStorage(), "SQLiteOp.db");
        if (!database.exists()) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(database.getPath(), null);
            onCreate(db);
            db.close();
        }
    }

    public void init() {
        singleton = null;
    }

    public static DatabaseOperacionesOpenHelper getInstance() {
        if (singleton == null)
            singleton = new DatabaseOperacionesOpenHelper();
        return singleton;
    }

    public void dropDatabase() {
        database.delete();

        singleton = null;
    }

    public DatabaseServices getReadableDatabaseServices() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        return new DatabaseServices(db);
    }

    public DatabaseServices getWritableDatabaseServices() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        return new DatabaseServices(db);
    }

    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        return db;
    }

    private void onCreate(SQLiteDatabase db) {
        String sql = null;

        try {
            sql = "CREATE TABLE Folios ( " +
                    "tipo ntext," +
                    "folio int," +
                    "PRIMARY KEY (tipo)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE Visita ( " +
                    "folio int," +
                    "status nchar(2)," +
                    "fechainicio datetime," +
                    "fechacreacion datetime," +
                    "fechamodificacion datetime," +
                    "filial ntext," +
                    "intermediario ntext," +
                    "cliente ntext," +
                    "razonsocial ntext," +
                    "causanopedido ntext," +
                    "foliopedido int," +
                    "totalpedido double," +
                    "latitud double," +
                    "longitud double," +
                    "respuesta ntext," +
                    "PRIMARY KEY (folio)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE Pedido ( " +
                    "folio int," +
                    "status nchar(2)," +
                    "fechainicio datetime," +
                    "fechacreacion datetime," +
                    "fechamodificacion datetime," +
                    "filial ntext," +
                    "intermediario ntext," +
                    "cliente ntext," +
                    "nombre ntext," +
                    "partidas int," +
                    "cantidad int," +
                    "importe double," +
                    "importeiva double," +
                    "iva double," +
                    "total double," +
                    "respuesta ntext," +
                    "impresiones int," +
                    "fechaentrega datetime," +
                    "tipo ntext," +
                    "direccion ntext," +
                    "claveenvio ntext," +
                    "observaciones ntext," +
                    "autorizacion int," +
                    "PRIMARY KEY (folio)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE DetPedido ( " +
                    "folio int," +
                    "codigo ntext," +
                    "linea ntext," +
                    "orden int," +
                    "cantidad int," +
                    "precio double," +
                    "precioiva double," +
                    "importe double," +
                    "importeiva double," +
                    "priva double," +
                    "iva double," +
                    "total double," +
                    "PRIMARY KEY (folio, codigo)," +
                    "FOREIGN KEY (folio) REFERENCES Pedido (folio)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE Devolucion ( " +
                    "folio int," +
                    "status nchar(2)," +
                    "fechacreacion datetime," +
                    "fechamodificacion datetime," +
                    "filial ntext," +
                    "intermediario ntext," +
                    "cliente ntext," +
                    "nombre ntext," +
                    "partidas int," +
                    "cantidad int," +
                    "total double," +
                    "respuesta ntext," +
                    "impresiones int," +
                    "tipo ntext," +
                    "observaciones ntext," +
                    "PRIMARY KEY (folio)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE DetDevolucion ( " +
                    "folio int," +
                    "documento nchar(20)," +
                    "codigo nchar(15)," +
                    "linea ntext," +
                    "cantidad int," +
                    "precio double," +
                    "total double," +
                    "causa ntext," +
                    "descuento double," +
                    "tipo ntext," +
                    "PRIMARY KEY (folio, documento, codigo)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE CobranzaDeposito ( " +
                    "folio int," +
                    "referencia ntext," +
                    "banco ntext," +
                    "fechacobro ntext," +
                    "PRIMARY KEY (folio)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE Cobranza ( " +
                    "folio int," +
                    "status nchar(2)," +
                    "fechacreacion datetime," +
                    "fechamodificacion datetime," +
                    "filial ntext," +
                    "intermediario ntext," +
                    "cliente ntext," +
                    "nombre ntext," +
                    "tipo ntext," +
                    "lineas int," +
                    "piezas int," +
                    "total double," +
                    "respuesta ntext," +
                    "impresiones int," +
                    "observaciones ntext," +
                    "PRIMARY KEY (folio)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE DetCobranza ( " +
                    "folio int," +
                    "renglon int," +
                    "documento nchar(20)," +
                    "tipodocumento nchar," +
                    "tipopago ntext," +
                    "referencia ntext," +
                    "banco ntext," +
                    "fechacobro ntext," +
                    "pago double," +
                    "importe double," +
                    "prdescuento double," +
                    "descuento double," +
                    "PRIMARY KEY (folio, renglon)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE Ubicacion ( " +
                    "folio int," +
                    "status nchar(2)," +
                    "fechacreacion datetime," +
                    "filial ntext," +
                    "intermediario ntext," +
                    "cliente ntext," +
                    "nombre ntext," +
                    "latitud double," +
                    "longitud double," +
                    "precision double," +
                    "proveedor ntext," +
                    "respuesta ntext," +
                    "PRIMARY KEY (folio)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE UbicacionFoto ( " +
                    "folio int," +
                    "foto ntext, " +
                    "PRIMARY KEY (folio, foto)" +
                    ")";

            db.execSQL(sql);

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
    }
}