package mx.com.sagaji.android.dao.entity;

import com.atcloud.android.dao.engine.DatabaseList;
import com.atcloud.android.dao.engine.DatabaseRecord;

import java.util.Date;

/**
 * Created by jbecerra.
 */
public class VisitaDAO implements DatabaseRecord, DatabaseList {
    public int folio = 0;
    public String status = "";
    public Date fechainicio = new Date(0);
    public Date fechacreacion = new Date(0);
    public Date fechamodificacion = new Date(0);
    public String filial = "";
    public String intermediario = "";
    public String cliente = "";
    public String razonsocial = "";
    public String causanopedido = "";
    public int foliopedido = 0;
    public double totalpedido = 0.0;
    public double latitud = 0.0;
    public double longitud = 0.0;
    public String respuesta = "";

    public VisitaDAO() {
    }

    public VisitaDAO(int folio) {
        this.folio = folio;
    }

    //
    // DatabaseRecord
    //
    public String getTable() {
        return "Visita";
    }

    public String getWhere() {
        return "folio = "+folio;
    }

    public String getOrder() {
        return "folio";
    }

    //
    // DatabaseList
    //
    public int getFolio() {
        return folio;
    }

    @Override
    public Date getFecha() {
        return fechamodificacion;
    }

    @Override
    public String getCliente() {
        return cliente;
    }

    @Override
    public String getNombre() {
        return razonsocial;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getTipo() {
        return "";
    }

    @Override
    public int getLineas() {
        return 0;
    }

    @Override
    public int getPiezas() {
        return 0;
    }

    @Override
    public double getImporte() {
        return 0.0d;
    }

    public double getImporteIva() {
        return 0.0d;
    }

    @Override
    public double getIva() {
        return 0.0d;
    }

    @Override
    public double getTotal() {
        return 0.0d;
    }

    @Override
    public String getRespuesta() {
        return respuesta;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return folio+";"+fechainicio+";"+fechacreacion;
    }
}