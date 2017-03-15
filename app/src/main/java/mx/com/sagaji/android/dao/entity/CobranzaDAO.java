package mx.com.sagaji.android.dao.entity;

import java.util.Date;
import com.atcloud.android.dao.engine.DatabaseList;
import com.atcloud.android.dao.engine.DatabaseRecord;

public class CobranzaDAO implements DatabaseRecord, DatabaseList {
    public int folio = 0;
    public String status = "";
    public Date fechacreacion = new Date(0);
    public Date fechamodificacion = new Date(0);
    public String filial = "";
    public String intermediario = "";
    public String cliente = "";
    public String nombre = "";
    public String tipo = "";
    public int lineas = 0;
    public int piezas = 0;
    public double total = 0.0;
    public String respuesta = "";
    public int impresiones = 0;
    public String observaciones = "";

    public CobranzaDAO() {
    }

    public CobranzaDAO(int folio) {
        this.folio = folio;
    }

    //
    // DatabaseRecord
    //
    public String getTable() {
        return "Cobranza";
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

    public Date getFecha() {
        return fechamodificacion;
    }

    public String getCliente() {
        return cliente;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public String getStatus() {
        return status;
    }

    public String getTipo() {
        return tipo;
    }

    public int getLineas() {
        return lineas;
    }

    public int getPiezas() {
        return piezas;
    }

    @Override
    public double getImporte() {
        return 0;
    }

    @Override
    public double getIva() {
        return 0;
    }

    public double getTotal() {
        return total;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //
    //
    //
    @Override
    public String toString() {
        return folio+";"+fechamodificacion;
    }
}