package mx.com.sagaji.android.dao.entity;

import java.util.Date;
import com.atcloud.android.dao.engine.DatabaseList;
import com.atcloud.android.dao.engine.DatabaseRecord;

public class PedidoDAO implements DatabaseRecord, DatabaseList {
    public int folio = 0;
    public String status = "";
    public Date fechainicio = new Date(0);
    public Date fechacreacion = new Date(0);
    public Date fechamodificacion = new Date(0);
    public String filial = "";
    public String intermediario = "";
    public String cliente = "";
    public String nombre = "";
    public int partidas = 0;
    public int cantidad = 0;
    public double importe = 0.0;
    public double importeiva = 0.0;
    public double iva = 0.0;
    public double total = 0.0;
    public String respuesta = "";
    public int impresiones = 0;
    public Date fechaentrega = new Date(0);
    public String tipo = "";
    public String direccion = "";
    public String claveenvio = "";
    public String observaciones = "";
    public int autorizacion = 0;

    public PedidoDAO() {
    }

    public PedidoDAO(int folio) {
        this.folio = folio;
    }

    //
    // DatabaseRecord
    //
    public String getTable() {
        return "Pedido";
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
        return partidas;
    }

    public int getPiezas() {
        return cantidad;
    }

    @Override
    public double getImporte() {
        return 0;
    }

    @Override
    public double getIva() {
        return iva;
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
