package mx.com.sagaji.android.dao.entity;

import com.atcloud.android.dao.engine.DatabaseRecord;

import java.util.Date;

/**
 * Created by jbecerra.
 */
public class UbicacionDAO implements DatabaseRecord {
    public int folio = 0;
    public String status = "";
    public Date fechacreacion = new Date(0);
    public int sucursal = 0;
    public String vendedor = "";
    public String cliente = "";
    public String nombre = "";
    public double latitud = 0.0;
    public double longitud = 0.0;
    public double precision = 0.0;
    public String proveedor = "";
    public String respuesta = "";

    public UbicacionDAO() {
    }

    public UbicacionDAO(int folio) {
        this.folio = folio;
    }

    //
    // DatabaseRecord
    //
    public String getTable() {
        return "Ubicacion";
    }

    public String getWhere() {
        return "folio = "+folio;
    }

    public String getOrder() {
        return "folio";
    }

    @Override
    public String toString() {
        return folio+";"+fechacreacion;
    }
}