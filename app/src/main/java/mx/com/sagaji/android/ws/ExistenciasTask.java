package mx.com.sagaji.android.ws;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.atcloud.android.util.Message;
import java.util.ArrayList;

import mx.com.sagaji.android.PedidosActivity;
import mx.com.sagaji.android.to.ExistenciaTO;
import mx.com.sagaji.android.util.PostResponse;
import mx.com.sagaji.android.util.XMLParser;

public class ExistenciasTask extends AsyncTask<String, Void, String> {
    public static String LOGTAG = ExistenciasTask.class.getCanonicalName();

    private Exception exception;
    private SagajiWebService sagajiWebService;
    private Activity activity;
    private Object[] parametros;

    public ExistenciasTask(SagajiWebService sagajiWebService, Activity activity, Object[] parametros) {
        this.sagajiWebService = sagajiWebService;
        this.activity = activity;
        this.parametros = parametros;
    }

    public String doInBackground(String... urls) {
        try {
            exception = null;
            PostResponse postResponse = sagajiWebService.existencias(parametros[0], parametros[1]);
            Log.d(LOGTAG, "PostResponse: "+postResponse.responseCode+";"+postResponse.responseMessage+";"+postResponse.contentLength);
            return postResponse.response;
        } catch(Exception e) {
            exception = e;
            Log.e(LOGTAG, e.getMessage(), e);
            return e.getMessage();
        }
    }

    protected void onPostExecute(String response) {
        sagajiWebService.closeWaitMessage();
        try {
            Log.d(LOGTAG, "response: "+response);

            if (exception!=null)
                throw exception;

            String xmlResponse;
            try {
                xmlResponse = sagajiWebService.getXMLResponse(response, "existencias");
                //Log.d(LOGTAG, "XMLResponse: "+xmlResponse);
                Log.d(LOGTAG, "XMLResponse head: " + xmlResponse.substring(0, 150));
                Log.d(LOGTAG, "XMLResponse tail: " + xmlResponse.substring(xmlResponse.length() - 150));
            } catch (Exception e) {
                throw new Exception("No hay Documentos del Cliente.");
            }

            XMLParser xmlParser = new XMLParser();
            ArrayList<ExistenciaTO> existencias = xmlParser.parseExistencia(xmlResponse);

            if (activity instanceof PedidosActivity) {
                ((PedidosActivity)activity).tengoExistencias(existencias);
            }

        } catch(Exception e) {
            Log.d(LOGTAG, e.getMessage(), e);
            //Message.alert(activity, e.getMessage());
        }
    }
}
