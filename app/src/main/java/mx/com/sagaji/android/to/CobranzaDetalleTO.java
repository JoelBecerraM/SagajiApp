package mx.com.sagaji.android.to;

public class CobranzaDetalleTO {
    public String documento = "";
    public String tipopago = "";
    public String referencia = "";
    public String banco = "";
    public String fechacobro = "";
    public double pago = 0.0;
    public double importe = 0.0;
    public double prdescuento = 0.0;
    public double descuento = 0.0;

    public String getTipoPago() {
        if (tipopago.compareTo("1")==0)
            return "Efe";
        else if (tipopago.compareTo("2")==0)
            return "FiD";
        else if (tipopago.compareTo("3")==0)
            return "Che";
        else if (tipopago.compareTo("4")==0)
            return "ChP";
        else if (tipopago.compareTo("5")==0)
            return "Tr";
        return "";
    }

    public String getTipoPagoExt() {
        if (tipopago.compareTo("1")==0)
            return "Efectivo";
        else if (tipopago.compareTo("2")==0)
            return "Ficha Deposito";
        else if (tipopago.compareTo("3")==0)
            return "Cheque";
        else if (tipopago.compareTo("4")==0)
            return "Chque Posfechado";
        else if (tipopago.compareTo("5")==0)
            return "Tr";
        return "";
    }

    public String getTipoDocumento() {
        /*if (tipodocumento.compareTo("1")==0)
            return "Fac";
        else if (tipodocumento.compareTo("2")==0)
            return "NoC";
        return "";*/
        return "Fac";
    }

    public String getTipoDocumentoExt() {
        /*if (tipodocumento.compareTo("1")==0)
            return "Factura";
        else if (tipodocumento.compareTo("2")==0)
            return "Nota Cargo";
        return "";*/
        return "Factura";
    }
}