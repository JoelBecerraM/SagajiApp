package mx.com.sagaji.android;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Message;
import com.atcloud.android.util.Numero;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.dao.GenericDAO;
import mx.com.sagaji.android.dao.entity.CobranzaDAO;
import mx.com.sagaji.android.dao.entity.DetCobranzaDAO;
import mx.com.sagaji.android.dao.entity.DetDevolucionDAO;
import mx.com.sagaji.android.dao.entity.DetPedidoDAO;
import mx.com.sagaji.android.dao.entity.DevolucionDAO;
import mx.com.sagaji.android.dao.entity.InformacionDAO;
import mx.com.sagaji.android.dao.entity.PedidoDAO;
import mx.com.sagaji.android.dao.entity.UbicacionDAO;
import mx.com.sagaji.android.dao.entity.VisitaDAO;
import mx.com.sagaji.android.delegate.TaskDelegate;
import mx.com.sagaji.android.listener.OnMensajeListener;
import mx.com.sagaji.android.to.CatalogoTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.BasicNameValue;
import mx.com.sagaji.android.util.Constantes;
import mx.com.sagaji.android.util.PostResponse;
import mx.com.sagaji.android.util.Sincronizacion;
import mx.com.sagaji.android.ws.SagajiWebService;

public class EnviaOperacionesTask extends AsyncTask<String, String, String> implements OnMensajeListener {
    public static String LOGTAG = EnviaOperacionesTask.class.getCanonicalName();

    private DatabaseServices dsOp;
    private DatabaseServices ds;
    private ConfiguracionTO configuracionTO;
    private Sincronizacion sincronizacion;
    private SagajiWebService sagajiWebService;
    private Activity activity;
    private EditText edtLog;
    private File externalStorage;
    private long operaciones;
    private boolean enviooperacionesOK;
    private String mensajeError;
    private TaskDelegate taskDelegate;

    public EnviaOperacionesTask(Activity activity, ConfiguracionTO configuracionTO, EditText edtLog, long operaciones) {
        this.activity = activity;
        this.configuracionTO = configuracionTO;
        this.edtLog = edtLog;
        this.operaciones = operaciones;

        sincronizacion = new Sincronizacion();
        sincronizacion.setParametros(this);

        sagajiWebService = new SagajiWebService();
        sagajiWebService.setParametros(configuracionTO);
    }

    @Override
    public String doInBackground(String... urls) {
        enviaOperaciones();
        return "OK";
    }

    @Override
    protected void onPostExecute(String result) {
        sincronizacion.guardaLog(edtLog.getText().toString().getBytes());

        if (taskDelegate!=null)
            taskDelegate.taskCompletionResult(activity.RESULT_OK);

        if (enviooperacionesOK)
            Message.mensaje(activity, "Envio de Operaciones terminado correctamente.");
        else
            Message.alert(activity, "Ocurrio un error al Enviar las Operaciones.\n"+mensajeError);
    }

    public void setTaskDelegate(TaskDelegate taskDelegate) {
        this.taskDelegate = taskDelegate;
    }

    private void enviaOperaciones() {
        enviooperacionesOK = false;
        publishProgress("Inicia el envio de operaciones ...");

        externalStorage = AndroidApplication.getStorage();

        String verifica = sincronizacion.verificaConfiguracion(configuracionTO);
        if (verifica==null) {
            if (enviaOperaciones(true))
                enviooperacionesOK = true;
        } else {
            publishProgress(verifica);
        }

        publishProgress("Fin");
    }

    private boolean enviaOperaciones(boolean confirmado) {
        ArrayList<CatalogoTO> catalogos = new ArrayList<>();
        int totalBytes = 0;

        if (!sincronizacion.checaURL(configuracionTO)) {
            publishProgress("Fallo al checar la URL, salgo del envio de operaciones.");
            return false;
        };

        publishProgress("Obtengo los catalogos de ["+configuracionTO.url+"] ...");

        String url = configuracionTO.url+"/sagaji/servlets/ResponseController";
        String id = "&filial="+configuracionTO.filial+"&intermediario="+configuracionTO.intermediario;

        publishProgress("Fecha Última Sincronización ...");
        File informacion = new File(externalStorage, "informacion.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=fechaUltimaSincronizacion"+id, informacion.getAbsolutePath());
            catalogos.add(new CatalogoTO(new InformacionDAO(), informacion));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());

            //
            // Salgo si falla este catalogo
            //
            return false;
        }

        boolean pasa = verificaToken(informacion);
        if (!pasa) {
            //rutinaDePanico(externalStorage);
            publishProgress("El password central ha cambiado, no se puede continuar con la Sincronización.");
            return false;
        }
        
        boolean errores = false;

        ds = DatabaseOpenHelper.getInstance().getReadableDatabaseServices();
        dsOp = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

        if ((operaciones & Constantes.ENVIAR_VISITAS) > 0) {
            try {
                totalBytes += enviaVisitas();
            } catch(Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);

                errores = true;
                mensajeError = "Error enviando las visitas ["+e.getMessage()+"]";
                publishProgress(mensajeError);
            }
        }
        if ((operaciones & Constantes.ENVIAR_PEDIDOS) > 0) {
            try {
                totalBytes += enviaPedidos();
            } catch(Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);

                errores = true;
                mensajeError = "Error enviando los pedidos ["+e.getMessage()+"]";
                publishProgress(mensajeError);
            }
        }
        if ((operaciones & Constantes.ENVIAR_DEVOLUCIONES) > 0) {
            try {
                totalBytes += enviaDevoluciones();
            } catch(Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);

                errores = true;
                mensajeError = "Error enviando las devoluciones ["+e.getMessage()+"]";
                publishProgress(mensajeError);
            }
        }
        if ((operaciones & Constantes.ENVIAR_COBRANZAS) > 0) {
            try {
                totalBytes += enviaCobranzas();
            } catch(Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);

                errores = true;
                mensajeError = "Error enviando la cobranza ["+e.getMessage()+"]";
                publishProgress(mensajeError);
            }
        }
        if ((operaciones & Constantes.ENVIAR_UBICACIONES) > 0) {
            try {
                totalBytes += enviaUbicaciones();
            } catch(Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);

                errores = true;
                mensajeError = "Error enviando las ubicaciones ["+e.getMessage()+"]";
                publishProgress(mensajeError);
            }
        }

        dsOp.close();
        ds.close();

        publishProgress("Total de bytes Enviados: "+Numero.getIntNumero(totalBytes));

        return !errores;
    }

    private int enviaVisitas() throws Exception {
        int totalBytes = 0;
        publishProgress("Enviando Visitas ...");

        int transmisionvisitas =  GenericDAO.obtenerSiguienteFolio(dsOp, Constantes.FOLIO_TRANSMISION_VISITA);

        String url = configuracionTO.url + "/centraltp/VisitaRequestController";

        // Inicio de Transmision
        publishProgress("Inicio de Transmisión de Visitas ...");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filial", configuracionTO.filial);
        jsonObject.put("intermediario", configuracionTO.intermediario);
        jsonObject.put("transmision", transmisionvisitas);
        jsonObject.put("estado", "inicio");

        PostResponse postResponse = sincronizacion.postService(url, jsonObject.toString());
        totalBytes += postResponse.contentLength;
        publishProgress("Respuesta "+postResponse.response);

        if (!sincronizacion.okRespuesta(postResponse.response))
            throw new Exception("Inicio de Transmisión de Visitas incorrecto ["+postResponse.response+"]");

        boolean errores = false;

        // Envia todas las visitas
        jsonObject = new JSONObject();
        jsonObject.put("filial", configuracionTO.filial);
        jsonObject.put("intermediario", configuracionTO.intermediario);
        jsonObject.put("transmision", transmisionvisitas);

        JSONArray jsonArray = new JSONArray();

        // Envia todas las visitas
        List<DatabaseRecord> array = dsOp.select(new VisitaDAO(), "status = '" + Constantes.ESTADO_TERMINADO + "'");
        for(Object object : array) {
            VisitaDAO visitaDAO = (VisitaDAO) object;

            publishProgress("Enviando Visita [" + visitaDAO.toString() + "] ...");

            JSONObject jsonObjetDetalle = new JSONObject();
            jsonObjetDetalle.put("intermediariovisita", visitaDAO.intermediario);
            jsonObjetDetalle.put("folio", visitaDAO.folio);
            jsonObjetDetalle.put("cliente", visitaDAO.cliente);
            //jsonObjetDetalle.put("nombrecliente", visitaDAO.nombre);
            jsonObjetDetalle.put("fechainicio", Fecha.getFechaHora(visitaDAO.fechainicio));
            jsonObjetDetalle.put("fechacreacion", Fecha.getFechaHora(visitaDAO.fechacreacion));
            jsonObjetDetalle.put("fechaultimasincronizacion", Fecha.getFechaHora(configuracionTO.fechaultimasincronizacion));
            jsonObjetDetalle.put("causanopedido", visitaDAO.causanopedido);
            jsonObjetDetalle.put("foliopedido", visitaDAO.foliopedido);
            jsonObjetDetalle.put("totalpedido", visitaDAO.totalpedido);
            jsonObjetDetalle.put("latitud", visitaDAO.latitud);
            jsonObjetDetalle.put("longitud", visitaDAO.longitud);
            jsonObjetDetalle.put("version", "v:"+configuracionTO.version);
            jsonArray.put(jsonObjetDetalle);
        }

        jsonObject.put("detalles", jsonArray);

        try {
            postResponse = sincronizacion.postService(url, jsonObject.toString());
            totalBytes += postResponse.contentLength;
            publishProgress("Respuesta "+postResponse.response);

            if (sincronizacion.okRespuesta(postResponse.response)) {
                for(Object object : array) {
                    VisitaDAO visitaDAO = (VisitaDAO) object;

                    visitaDAO.respuesta = postResponse.response;
                    visitaDAO.status = Constantes.ESTADO_ENVIADO;
                }

            } else {
                publishProgress("Respuesta invalida -" + postResponse.response + "-");
                errores = true;
            }

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al enviar las visitas "+e.getMessage());
        }

        // Fin de Transmision
        publishProgress("Fin de Transmisión de Visitas ...");

        jsonObject = new JSONObject();
        jsonObject.put("filial", configuracionTO.filial);
        jsonObject.put("intermediario", configuracionTO.intermediario);
        jsonObject.put("transmision", transmisionvisitas);
        jsonObject.put("estado", "fin");

        postResponse = sincronizacion.postService(url, jsonObject.toString());
        totalBytes += postResponse.contentLength;
        publishProgress("Respuesta "+postResponse.response);

        if (!sincronizacion.okRespuesta(postResponse.response))
            throw new Exception("Fin de Transmisión de Visitas incorrecto ["+postResponse.response+"]");

        // Escribe la Confirmacion de Visita Enviada
        dsOp.beginTransaction();
        try {
            for(Object object : array) {
                VisitaDAO visitaDAO = (VisitaDAO) object;

                dsOp.update(visitaDAO);
            }

            dsOp.commit();
        } catch(Exception e) {
            dsOp.rollback();

            Log.e(LOGTAG, e.getMessage(), e);

            String mensaje = "Error escribiendo la confirmación de visita enviado ["+e.getMessage()+"]";
            publishProgress(mensaje);
            Message.alert(activity, mensaje);
        }

        if (errores)
            throw new Exception("Error al transmitir las visitas");

        return totalBytes;
    }

    private int enviaPedidos() throws Exception {
        int totalBytes = 0;
        publishProgress("Enviando Pedidos ...");

        String url = sagajiWebService.getServiceURI()+"/inspedido";

        // Inicio de Transmision
        boolean errores = false;

        // Envia cada uno de los pedidos
        List<DatabaseRecord> array = dsOp.select(new PedidoDAO(), "status = '" + Constantes.ESTADO_TERMINADO + "'");
        for(Object object : array) {
            PedidoDAO pedidoDAO = (PedidoDAO)object;

            publishProgress("Enviando Pedido [" + pedidoDAO.toString() + "] ...");

            double totalLE = 0.0d;

            StringBuilder detalleSB = new StringBuilder();
            List<DatabaseRecord> arrayDetalles = dsOp.select(new DetPedidoDAO(), "folio = " + pedidoDAO.folio, "orden");
            for (DatabaseRecord detalle : arrayDetalles) {
                DetPedidoDAO detPedidoDAO = (DetPedidoDAO)detalle;

                detalleSB.append(detPedidoDAO.codigo).append(",");
                detalleSB.append(detPedidoDAO.cantidad);
                detalleSB.append("|");

                if (detPedidoDAO.linea.compareTo("LE")==0)
                    totalLE += Numero.redondea(((DetPedidoDAO) detalle).total);
            }
            detalleSB.deleteCharAt(detalleSB.length() - 1);

            ArrayList<BasicNameValue> values = new ArrayList<>();
            values.add(new BasicNameValue("acces", sagajiWebService.getAccess()));
            values.add(new BasicNameValue("cliente", pedidoDAO.cliente));
            values.add(new BasicNameValue("asesor", pedidoDAO.intermediario));
            values.add(new BasicNameValue("sucursal", sagajiWebService.getSucursal(pedidoDAO.filial)));
            values.add(new BasicNameValue("tippedido", pedidoDAO.tipo));
            values.add(new BasicNameValue("agrupacion", String.valueOf(pedidoDAO.folio)));
            values.add(new BasicNameValue("domentreg", pedidoDAO.direccion));
            values.add(new BasicNameValue("penvio", pedidoDAO.claveenvio));
            values.add(new BasicNameValue("detalle", detalleSB.toString()));

            try {
                PostResponse postResponse = sincronizacion.postService(url, values);
                totalBytes += postResponse.contentLength;

                String xmlResponse;
                try {
                    xmlResponse = sagajiWebService.getXMLResponse(postResponse.response, "respuesta");
                    //Log.d(LOGTAG, "XMLResponse: "+xmlResponse);
                    Log.d(LOGTAG, "XMLResponse head: " + xmlResponse.substring(0, 150));
                    Log.d(LOGTAG, "XMLResponse tail: " + xmlResponse.substring(xmlResponse.length() - 150));
                } catch (Exception e) {
                    publishProgress("PostResponse -" + postResponse.responseCode+";"+postResponse.responseMessage+";"+postResponse.response + "-");
                    throw new Exception("No hay Detalles en la Respuesta.");
                }

                boolean okRespuesta = xmlResponse.startsWith("<respuesta")
                        && xmlResponse.endsWith("</respuesta>");
                if (okRespuesta) {
                    publishProgress("Respuesta correcta -" + xmlResponse + "-");
                    pedidoDAO.respuesta = "[OK]";
                    pedidoDAO.status = Constantes.ESTADO_ENVIADO;

                } else {
                    publishProgress("PostResponse -" + postResponse.responseCode+";"+postResponse.responseMessage+";"+postResponse.response + "-");
                    publishProgress("Respuesta invalida -" + xmlResponse + "-");
                    errores = true;
                }

            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);
                publishProgress("Error al enviar el pedido "+e.getMessage());
            }
        }

        // Inicio de Transmision
        publishProgress("Fin de Transmisión de Pedidos ...");

        // Escribe la Confirmacion de Pedido Enviado
        dsOp.beginTransaction();
        try {
            for(Object object : array) {
                PedidoDAO pedidoDAO = (PedidoDAO) object;

                dsOp.update(pedidoDAO);
            }

            dsOp.commit();
        } catch(Exception e) {
            dsOp.rollback();

            Log.e(LOGTAG, e.getMessage(), e);

            String mensaje = "Error escribiendo la confirmación de pedido enviado ["+e.getMessage()+"]";
            publishProgress(mensaje);
            Message.alert(activity, mensaje);
        }

        if (errores)
            throw new Exception("Error al transmitir los pedidos");

        return totalBytes;
    }

    private int enviaDevoluciones() throws Exception {
        int totalBytes = 0;
        publishProgress("Enviando Devoluciones ...");

        String url = sagajiWebService.getServiceURI()+"/insdev";

        // Inicio de Transmision
        boolean errores = false;

        // Envia cada uno de las devoluciones
        List<DatabaseRecord> array = dsOp.select(new DevolucionDAO(), "status = '" + Constantes.ESTADO_TERMINADO + "'");
        for(Object object : array) {
            DevolucionDAO devolucionDAO = (DevolucionDAO) object;

            publishProgress("Enviando Devoluciones [" + devolucionDAO.toString() + "] ...");

            String documento = "";
            StringBuilder detalleSB = new StringBuilder();
            List<DatabaseRecord> arrayDetalles = dsOp.select(new DetDevolucionDAO(), "folio = " + devolucionDAO.folio, "codigo");
            for (DatabaseRecord detalle : arrayDetalles) {
                DetDevolucionDAO detDevolucionDAO = (DetDevolucionDAO)detalle;

                documento = detDevolucionDAO.documento;
                detalleSB.append(detDevolucionDAO.codigo).append(",");
                detalleSB.append(detDevolucionDAO.cantidad).append(",");
                detalleSB.append(detDevolucionDAO.causa);
                detalleSB.append("|");
            }
            detalleSB.deleteCharAt(detalleSB.length() - 1);

            ArrayList<BasicNameValue> values = new ArrayList<>();
            values.add(new BasicNameValue("acces", sagajiWebService.getAccess()));
            values.add(new BasicNameValue("cliente", devolucionDAO.cliente));
            values.add(new BasicNameValue("asesor", devolucionDAO.intermediario));
            values.add(new BasicNameValue("sucursal", sagajiWebService.getSucursal(devolucionDAO.filial)));
            values.add(new BasicNameValue("documento", documento));
            values.add(new BasicNameValue("detalle", detalleSB.toString()));

            try {
                PostResponse postResponse = sincronizacion.postService(url, values);
                totalBytes += postResponse.contentLength;

                String xmlResponse;
                try {
                    xmlResponse = sagajiWebService.getXMLResponse(postResponse.response, "respuesta");
                    //Log.d(LOGTAG, "XMLResponse: "+xmlResponse);
                    Log.d(LOGTAG, "XMLResponse head: " + xmlResponse.substring(0, 150));
                    Log.d(LOGTAG, "XMLResponse tail: " + xmlResponse.substring(xmlResponse.length() - 150));
                } catch (Exception e) {
                    publishProgress("PostResponse -" + postResponse.responseCode+";"+postResponse.responseMessage+";"+postResponse.response + "-");
                    throw new Exception("No hay Detalles en la Respuesta.");
                }

                boolean okRespuesta = xmlResponse.startsWith("<respuesta")
                        && xmlResponse.endsWith("</respuesta>");
                if (okRespuesta) {
                    publishProgress("Respuesta correcta -" + xmlResponse + "-");
                    devolucionDAO.respuesta = "[OK]";
                    devolucionDAO.status = Constantes.ESTADO_ENVIADO;

                } else {
                    publishProgress("PostResponse -" + postResponse.responseCode+";"+postResponse.responseMessage+";"+postResponse.response + "-");
                    publishProgress("Respuesta invalida -" + xmlResponse + "-");
                    errores = true;
                }

            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);
                publishProgress("Error al enviar la devolucion "+e.getMessage());
            }
        }

        // Inicio de Transmision
        publishProgress("Fin de Transmisión de Devolución ...");

        // Escribe la Confirmacion de Devolucion Enviada
        dsOp.beginTransaction();
        try {
            for(Object object : array) {
                DevolucionDAO devolucionDAO = (DevolucionDAO)object;

                dsOp.update(devolucionDAO);
            }

            dsOp.commit();
        } catch(Exception e) {
            dsOp.rollback();

            Log.e(LOGTAG, e.getMessage(), e);

            String mensaje = "Error escribiendo la confirmación de devolucion enviada ["+e.getMessage()+"]";
            publishProgress(mensaje);
            Message.alert(activity, mensaje);
        }

        if (errores)
            throw new Exception("Error al transmitir la devolucion");

        return totalBytes;
    }

    private int enviaCobranzas() throws Exception {
        int totalBytes = 0;
        publishProgress("Enviando Cobranza ...");

        String url = sagajiWebService.getServiceURI()+"/inscbrnza";

        // Inicio de Transmision
        boolean errores = false;

        // Envia cada una de las cobranza
        List<DatabaseRecord> array = dsOp.select(new CobranzaDAO(), "status = '" + Constantes.ESTADO_TERMINADO + "'");
        for(Object object : array) {
            CobranzaDAO cobranzaDAO = (CobranzaDAO)object;

            publishProgress("Enviando Cobranza [" + cobranzaDAO.toString() + "] ...");

            StringBuilder detalleSB = new StringBuilder();
            List<DatabaseRecord> arrayDetalles = dsOp.select(new DetCobranzaDAO(), "folio = " + cobranzaDAO.folio, "renglon");
            for (DatabaseRecord detalle : arrayDetalles) {
                DetCobranzaDAO detCobranzaDAO = (DetCobranzaDAO)detalle;

                detalleSB.append(detCobranzaDAO.documento).append(",");
                detalleSB.append(detCobranzaDAO.pago).append(",");
                detalleSB.append(detCobranzaDAO.descuento);
                detalleSB.append("|");
            }
            detalleSB.deleteCharAt(detalleSB.length() - 1);

            ArrayList<BasicNameValue> values = new ArrayList<>();
            values.add(new BasicNameValue("acces", sagajiWebService.getAccess()));
            values.add(new BasicNameValue("cliente", cobranzaDAO.cliente));
            values.add(new BasicNameValue("asesor", cobranzaDAO.intermediario));
            values.add(new BasicNameValue("sucursal", sagajiWebService.getSucursal(cobranzaDAO.filial)));
            values.add(new BasicNameValue("referencia", "REFERENCIA"));
            values.add(new BasicNameValue("grupo", "22"));
            values.add(new BasicNameValue("detalle", detalleSB.toString()));

            try {
                PostResponse postResponse = sincronizacion.postService(url, values);
                totalBytes += postResponse.contentLength;

                String xmlResponse;
                try {
                    xmlResponse = sagajiWebService.getXMLResponse(postResponse.response, "respuesta");
                    //Log.d(LOGTAG, "XMLResponse: "+xmlResponse);
                    Log.d(LOGTAG, "XMLResponse head: " + xmlResponse.substring(0, 150));
                    Log.d(LOGTAG, "XMLResponse tail: " + xmlResponse.substring(xmlResponse.length() - 150));
                } catch (Exception e) {
                    publishProgress("PostResponse -" + postResponse.responseCode+";"+postResponse.responseMessage+";"+postResponse.response + "-");
                    throw new Exception("No hay Detalles en la Respuesta.");
                }

                boolean okRespuesta = xmlResponse.startsWith("<respuesta")
                        && xmlResponse.endsWith("</respuesta>");
                if (okRespuesta) {
                    publishProgress("Respuesta correcta -" + xmlResponse + "-");
                    cobranzaDAO.respuesta = "[OK]";
                    cobranzaDAO.status = Constantes.ESTADO_ENVIADO;

                } else {
                    publishProgress("Respuesta invalida -" + xmlResponse + "-");
                    publishProgress("PostResponse -" + postResponse.responseCode+";"+postResponse.responseMessage+";"+postResponse.response + "-");
                    errores = true;
                }

            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);
                publishProgress("Error al enviar la cobranza "+e.getMessage());
            }
        }

        // Inicio de Transmision
        publishProgress("Fin de Transmisión de Cobranza ...");

        // Escribe la Confirmacion de Cobranza Enviada
        dsOp.beginTransaction();
        try {
            for(Object object : array) {
                CobranzaDAO cobranzaDAO = (CobranzaDAO)object;

                dsOp.update(cobranzaDAO);
            }

            dsOp.commit();
        } catch(Exception e) {
            dsOp.rollback();

            Log.e(LOGTAG, e.getMessage(), e);

            String mensaje = "Error escribiendo la confirmación de cobranza enviada ["+e.getMessage()+"]";
            publishProgress(mensaje);
            Message.alert(activity, mensaje);
        }

        if (errores)
            throw new Exception("Error al transmitir la cobranza");

        return totalBytes;
    }

    private int enviaUbicaciones() throws Exception {
        int totalBytes = 0;
        publishProgress("Enviando Ubicaciones ...");

        int transmisionubicaciones =  GenericDAO.obtenerSiguienteFolio(dsOp, Constantes.FOLIO_TRANSMISION_UBICACION);

        String url = configuracionTO.url + "/centraltp/UbicacionRequestController";

        // Inicio de Transmision
        publishProgress("Inicio de Transmisión de Visitas ...");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filial", configuracionTO.filial);
        jsonObject.put("intermediario", configuracionTO.intermediario);
        jsonObject.put("transmision", transmisionubicaciones);
        jsonObject.put("estado", "inicio");

        PostResponse postResponse = sincronizacion.postService(url, jsonObject.toString());
        totalBytes += postResponse.contentLength;
        publishProgress("Respuesta "+postResponse.response);

        if (!sincronizacion.okRespuesta(postResponse.response))
            throw new Exception("Inicio de Transmisión de Ubicaciones incorrecto ["+postResponse.response+"]");

        boolean errores = false;

        // Envia todas las ubicaciones
        jsonObject = new JSONObject();
        jsonObject.put("filial", configuracionTO.filial);
        jsonObject.put("intermediario", configuracionTO.intermediario);
        jsonObject.put("transmision", transmisionubicaciones);

        JSONArray jsonArray = new JSONArray();

        // Envia todas las ubicaciones
        List<DatabaseRecord> array = dsOp.select(new UbicacionDAO(), "status = '"+Constantes.ESTADO_TERMINADO+"'");
        for(Object object : array) {
            UbicacionDAO ubicacionDAO = (UbicacionDAO)object;

            publishProgress("Enviando Ubicacion [" + ubicacionDAO.toString() + "] ...");

            JSONObject jsonObjetDetalle = new JSONObject();
            jsonObjetDetalle.put("intermediario", ubicacionDAO.vendedor);
            jsonObjetDetalle.put("folio", ubicacionDAO.folio);
            jsonObjetDetalle.put("cliente", ubicacionDAO.cliente);
            jsonObjetDetalle.put("nombrecliente", ubicacionDAO.nombre);
            jsonObjetDetalle.put("fechacreacion", Fecha.getFechaHora(ubicacionDAO.fechacreacion));
            jsonObjetDetalle.put("latitud", ubicacionDAO.latitud);
            jsonObjetDetalle.put("longitud", ubicacionDAO.longitud);
            jsonObjetDetalle.put("precision", ubicacionDAO.precision);
            jsonObjetDetalle.put("proveedor", ubicacionDAO.proveedor);
            jsonObjetDetalle.put("version", "v:"+configuracionTO.version);
            jsonArray.put(jsonObjetDetalle);

            // Envia las fotos tomadas de la ubicacion
            /*List<DatabaseRecord> arrayFotos = dsOp.select(new UbicacionFotoDAO(), "folio = "+ubicacionDAO.folio);
            if (!arrayFotos.isEmpty()) {
                publishProgress("Enviando las Fotos de la Ubicación [" + ubicacionDAO.toString() + "] ...");

                try {
                    UploadResponse uploadResponse = enviaUbicacionFotos(ubicacionDAO, transmisionubicaciones, arrayFotos);
                    totalBytes += uploadResponse.contentLength;
                } catch(Exception ex) {
                    Log.e(LOGTAG, ex.getMessage(), ex);

                    String mensaje = "Error enviado las Fotos de la Ubicación ["+ex.getMessage()+"]";
                    publishProgress(mensaje);
                }
            }*/
        }

        jsonObject.put("detalles", jsonArray);

        try {
            postResponse = sincronizacion.postService(url, jsonObject.toString());
            totalBytes += postResponse.contentLength;
            publishProgress("Respuesta "+postResponse.response);

            if (sincronizacion.okRespuesta(postResponse.response)) {
                for(Object object : array) {
                    UbicacionDAO ubicacionDAO = (UbicacionDAO) object;

                    ubicacionDAO.respuesta = postResponse.response;
                    ubicacionDAO.status = Constantes.ESTADO_ENVIADO;
                }

            } else {
                publishProgress("Respuesta invalida -"+postResponse.response+"-");
                errores = true;
            }

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al enviar las ubicaciones "+e.getMessage());
        }

        // Fin de Transmision
        publishProgress("Fin de Transmisión de Ubicaciones ...");

        jsonObject = new JSONObject();
        jsonObject.put("filial", configuracionTO.filial);
        jsonObject.put("intermediario", configuracionTO.intermediario);
        jsonObject.put("transmision", transmisionubicaciones);
        jsonObject.put("estado", "fin");

        postResponse = sincronizacion.postService(url, jsonObject.toString());
        totalBytes += postResponse.contentLength;
        publishProgress("Respuesta "+postResponse.response);

        if (!sincronizacion.okRespuesta(postResponse.response))
            throw new Exception("Fin de Transmisión de Ubicaciones incorrecto ["+postResponse.response+"]");

        // Escribe la Confirmacion de Ubicacion Enviada
        dsOp.beginTransaction();
        try {
            for(Object object : array) {
                UbicacionDAO ubicacionDAO = (UbicacionDAO)object;

                dsOp.update(ubicacionDAO);
            }

            dsOp.commit();
        } catch(Exception e) {
            dsOp.rollback();

            Log.e(LOGTAG, e.getMessage(), e);

            String mensaje = "Error escribiendo la confirmación de ubicación enviada ["+e.getMessage()+"]";
            publishProgress(mensaje);
            Message.alert(activity, mensaje);
        }

        if (errores)
            throw new Exception("Error al transmitir las ubicaciones");

        return totalBytes;
    }

    private void log(String message) {
        Log.i(LOGTAG, message);

        StringBuffer sb = new StringBuffer();
        sb.append(Fecha.getFechaHora())
                .append(" ").append(message).append("\n");

        edtLog.append(sb.toString());
        edtLog.setSelection(edtLog.getText().toString().length());
        edtLog.scrollTo(0, edtLog.getBottom());
    }

    private boolean verificaToken(File informacion) {
        boolean pasa = false;
        try {
            BufferedReader in = new BufferedReader(new FileReader(informacion));
            String line = in.readLine();
            String[] tokens = line.split("\\|");
            in.close();

            if (tokens.length<4)
                return false;

            String token = tokens[2];
            pasa = configuracionTO.token.compareTo(token)==0;

            String fechaUltimaSincronizacion = tokens[3];
            publishProgress("La fecha y hora de última sincronización es [" + fechaUltimaSincronizacion + "].");

            String fechaHoraCentral = tokens[4];
            SystemClock.setCurrentTimeMillis(Fecha.getDate(fechaHoraCentral).getTime());
            publishProgress("La fecha y hora del dispositivo es [" + Fecha.getFechaHora() + "] la hora de la central es [" + fechaHoraCentral + "].");

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress(e.getMessage());
        }

        return pasa;
    }

    @Override
    public void onMensaje(String message) {
        publishProgress(message);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        log(progress[0]);
    }
}