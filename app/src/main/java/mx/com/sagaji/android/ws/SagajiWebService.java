package mx.com.sagaji.android.ws;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import mx.com.sagaji.android.listener.OnMensajeListener;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Constantes;
import mx.com.sagaji.android.util.PostResponse;
import mx.com.sagaji.android.util.Sincronizacion;

public class SagajiWebService implements OnMensajeListener {
    public static String LOGTAG = SagajiWebService.class.getCanonicalName();
    private String webServiceURI = "";

    private ConfiguracionTO configuracionTO;
    private Sincronizacion sincronizacion;
    private ProgressDialog progressDialog;

    @Override
    public void onMensaje(String message) {
        Log.d(LOGTAG, message);
    }

    public void waitMessage(Activity activity, String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void closeWaitMessage() {
        if (progressDialog!=null)
            progressDialog.dismiss();
    }

    public void setParametros(ConfiguracionTO configuracionTO) {
        this.configuracionTO = configuracionTO;
        sincronizacion = new Sincronizacion();
        sincronizacion.setParametros(this);
        webServiceURI = configuracionTO.parametros.get(Constantes.PARAMETRO_WEBSERVICEURI);
    }

    public PostResponse facturasDevolucion(Object access, Object cliente) throws Exception {
        String request = "?acces="+access+"&cliente="+cliente;
        return sincronizacion.getResponse(webServiceURI+"/lsta_fact", request);
    }

    public PostResponse facturasDevolucionDetalle(Object access, Object cliente, Object asesor, Object documento) throws Exception {
        String request = "?acces="+access+"&cliente="+cliente+"&asesor="+asesor+"&documento="+documento;
        return sincronizacion.getResponse(webServiceURI+"/lsta_factdet", request);
    }

    public PostResponse facturasCobranza(Object access, Object cliente) throws Exception {
        String request = "?acces="+access+"&cliente="+cliente;
        return sincronizacion.getResponse(webServiceURI+"/lsta_FactCob", request);
    }

    public PostResponse existencias(Object access, Object codigo) throws Exception {
        String request = "?acces="+access+"&producto="+codigo;
        return sincronizacion.getResponse(webServiceURI+"/exist_sucursales", request);
    }

    //
    //
    //

    public String getXMLResponse(String response, String responseType) {
        response = response.replaceAll("NewDataSet", responseType);
        response = response.replaceAll("Table", "detalles");
        int startIndex = response.indexOf("<" + responseType);
        int endIndex = response.indexOf("</" + responseType);
        if (endIndex==-1) {
            endIndex = response.indexOf("<" + responseType + " />");
            endIndex++;
        }
        endIndex = endIndex + 3 + responseType.length();
        String xmlResponse = response.substring(startIndex, endIndex);
        return xmlResponse;
    }

    public String getServiceURI() {
        return webServiceURI;
    }

    public String getSucursal(String filial) {
        if (filial.equals("01"))
            return "MEXICO";
        if (filial.equals("02"))
            return "LEON";
        if (filial.equals("03"))
            return "MONTERREY";
        if (filial.equals("04"))
            return "OAXACA";
        if (filial.equals("05"))
            return "PUEBLA";
        if (filial.equals("08"))
            return "TUXTLA";
        return "MEXICO";
    }

    public String getAccess() {
        return "987456";
    }
}