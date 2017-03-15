package mx.com.sagaji.android.dao;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.atcloud.android.dao.engine.DatabaseServices;
import java.io.File;

import mx.com.sagaji.android.AndroidApplication;

/**
 * Created by jbecerra.
 */
public class DatabaseOpenHelper {
    public static String LOGTAG = DatabaseOpenHelper.class.getCanonicalName();
    private static DatabaseOpenHelper singleton = null;
    private File database = null;

    private DatabaseOpenHelper() {
        database = new File(AndroidApplication.getStorage(), "SQLite.db");
        if (!database.exists()) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(database.getPath(), null);
            onCreate(db);
            db.close();
        }
    }

    public void init() {
        singleton = null;
    }

    public static DatabaseOpenHelper getInstance() {
        if (singleton == null)
            singleton = new DatabaseOpenHelper();
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

            sql = "CREATE TABLE Parametro ( " +
                    "parametro ntext," +
                    "valor ntext," +
                    "activo int," +
                    "PRIMARY KEY (parametro)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE Filial ( " +
                    "filial ntext," +
                    "razonsocial ntext," +
                    "direccion ntext," +
                    "colonia ntext," +
                    "poblacion ntext," +
                    "estado ntext," +
                    "telefono ntext," +
                    "codigopostal ntext," +
                    "rfc ntext," +
                    "PRIMARY KEY (filial)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE Informacion ( " +
                    "filial ntext," +
                    "intermediario ntext," +
                    "token ntext," +
                    "fechaultimasincronizacion datetime," +
                    "fechacentral datetime," +
                    "PRIMARY KEY (filial, intermediario)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE Intermediario ( " +
                    "intermediario ntext," +
                    "nombre ntext," +
                    "plaza ntext," +
                    "PRIMARY KEY (intermediario)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE Cliente ( " +
                    "cliente ntext," +
                    "razonsocial ntext," +
                    "propietario ntext," +
                    "direccion ntext," +
                    "colonia ntext," +
                    "poblacion ntext," +
                    "entidadfederativa ntext," +
                    "telefono ntext," +
                    "codigopostal ntext," +
                    "rfc ntext," +
                    "codigobarras ntext," +
                    "aceptasustitutos ntext," +
                    "estado ntext," +
                    "perfil ntext," +
                    "separa ntext," +
                    "tipo ntext," +
                    "mnsaldo double," +
                    "mnsepara double," +
                    "mncobrado double," +
                    "mnventa double," +
                    "mnvencido double," +
                    "mncobradocod double," +
                    "diasgracia int," +
                    "referencia ntext," +
                    "totfac int, "+
                    "formaenvio ntext," +
                    "PRIMARY KEY (cliente)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE DireccionEntrega ( " +
                    "cliente ntext," +
                    "direccion ntext," +
                    "descripcion ntext," +
                    "PRIMARY KEY (cliente, direccion)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE Promocion ( " +
                    "codigo ntext," +
                    "descripcion ntext," +
                    "escala int," +
                    "tipo nchar," +
                    "descuento double," +
                    "preciooferta double," +
                    "fechainicio datetime," +
                    "fechafin datetime," +
                    "lentomovimiento nchar," +
                    "PRIMARY KEY (codigo, escala, tipo)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE INDEX PromocionINDX1 ON Promocion(descripcion)";
            db.execSQL(sql);

            sql = "CREATE TABLE ProductoNuevo ( " +
                    "codigo ntext," +
                    "descripcion ntext," +
                    "lentomovimiento ntext," +
                    "PRIMARY KEY (codigo)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE INDEX ProductoNuevoINDX1 ON ProductoNuevo(descripcion)";
            db.execSQL(sql);

            sql = "CREATE TABLE Producto ( " +
                    "codigo ntext," +
                    "descripcion ntext," +
                    "unidadmedida ntext," +
                    "precio double," +
                    "existencia int," +
                    "linea ntext," +
                    "aceptadevoluciones ntext," +
                    "codigosustituto ntext," +
                    "codigobarras ntext," +
                    "PRIMARY KEY (codigo)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE INDEX ProductoINDX1 ON Producto(descripcion)";
            db.execSQL(sql);
            sql = "CREATE INDEX ProductoINDX2 ON Producto(codigobarras)";
            db.execSQL(sql);

            sql = "CREATE TABLE Equivalente ( " +
                    "codigo ntext," +
                    "equivalente ntext," +
                    "descripcion ntext," +
                    "precio double," +
                    "cantidad int," +
                    "lineaproveedor ntext," +
                    "linea ntext," +
                    "PRIMARY KEY (codigo, equivalente)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE INDEX EquivalenteINDX1 ON Equivalente(descripcion)";
            db.execSQL(sql);

            sql = "CREATE TABLE Existencia ( " +
                    "filial ntext," +
                    "codigo ntext," +
                    "existencia int," +
                    "PRIMARY KEY (filial, codigo)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE INDEX ExistenciaINDX1 ON Existencia(codigo)";
            db.execSQL(sql);

            sql = "CREATE TABLE PrioridadEnvio ( " +
                    "clave ntext," +
                    "descripcion ntext," +
                    "PRIMARY KEY (clave)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE TipoPedido ( " +
                    "clave ntext," +
                    "descripcion ntext," +
                    "PRIMARY KEY (clave)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE MotivoDevolucion ( " +
                    "clave ntext," +
                    "descripcion ntext," +
                    "PRIMARY KEY (clave)" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE TABLE DescuentoCobranza ( " +
                    "linea ntext," +
                    "prdescuento double," +
                    "prdescuentomax double," +
                    "plazo int," +
                    "plazomax int," +
                    "monto double," +
                    "montomax double," +
                    "identificador ntext," +
                    "opcion ntext" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE INDEX DescuentoCobranzaINDX1 ON DescuentoCobranza(linea, prdescuento, prdescuentomax)";
            db.execSQL(sql);

            sql = "CREATE TABLE NegociacionEspecial ( " +
                    "cliente ntext," +
                    "prdescuento double," +
                    "prdescuentomax double," +
                    "plazo int," +
                    "plazomax int," +
                    "monto double," +
                    "montomax double," +
                    "linea ntext," +
                    "fechainicio datetime," +
                    "fechafin datetime," +
                    "prcomision double" +
                    ")";
            db.execSQL(sql);

            sql = "CREATE INDEX NegociacionEspecialINDX1 ON NegociacionEspecial(cliente, prdescuento, prdescuentomax)";
            db.execSQL(sql);

            sql = "CREATE TABLE Documento ( " +
                    "documento ntext," +
                    "tipodocumento nchar," +
                    "cliente ntext," +
                    "fechadocumento datetime," +
                    "fechavencimiento datetime," +
                    "saldo double," +
                    "cobrado double," +
                    "total double," +
                    "plazo int," +
                    "negesp nchar," +
                    "filial nchar," +
                    "PRIMARY KEY (documento)" +
                    ")";
            db.execSQL(sql);

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
    }
}