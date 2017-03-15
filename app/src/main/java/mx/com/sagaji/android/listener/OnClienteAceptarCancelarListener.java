package mx.com.sagaji.android.listener;

import java.util.Date;

import mx.com.sagaji.android.to.CausaNoVentaTO;

/**
 * Created by jbecerra.
 */
public interface OnClienteAceptarCancelarListener {
    public void onAceptar();
    public void onCancelar();
    public void onGuardarCasaNoVenta(CausaNoVentaTO causaNoVentaTO);
    public void onFechaEntrega(Date fechaentrega);
    public void onDireccionEntrega(String direccion);
}