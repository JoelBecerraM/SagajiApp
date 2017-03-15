package mx.com.sagaji.android;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseRecordLoad;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Numero;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.entity.ClienteDAO;
import mx.com.sagaji.android.dao.entity.DireccionEntregaDAO;
import mx.com.sagaji.android.dao.entity.EquivalenteDAO;
import mx.com.sagaji.android.dao.entity.ExistenciaDAO;
import mx.com.sagaji.android.dao.entity.FilialDAO;
import mx.com.sagaji.android.dao.entity.InformacionDAO;
import mx.com.sagaji.android.dao.entity.IntermediarioDAO;
import mx.com.sagaji.android.dao.entity.MotivoDevolucionDAO;
import mx.com.sagaji.android.dao.entity.ParametroDAO;
import mx.com.sagaji.android.dao.entity.PrioridadEnvioDAO;
import mx.com.sagaji.android.dao.entity.ProductoDAO;
import mx.com.sagaji.android.dao.entity.PromocionDAO;
import mx.com.sagaji.android.dao.entity.TipoPedidoDAO;
import mx.com.sagaji.android.delegate.TaskDelegate;
import mx.com.sagaji.android.listener.OnMensajeListener;
import mx.com.sagaji.android.to.CatalogoTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Sincronizacion;

public class SincronizacionTask extends AsyncTask<String, String, String> implements OnMensajeListener {
    public static String LOGTAG = SincronizacionTask.class.getCanonicalName();

    private SQLiteDatabase db;
    private ConfiguracionTO configuracionTO;
    private Sincronizacion sincronizacion;
    private Activity activity;
    private EditText edtLog;
    private File externalStorage;
    private boolean sincronizacionOK;
    private int erroresSincronizacion;
    private TaskDelegate taskDelegate;
    private HashMap<String,String> almacenesFiliales = null;
    private HashMap<String,String[]> existenciasAlmacenes = null;

    public SincronizacionTask(Activity activity, ConfiguracionTO configuracionTO, EditText edtLog) {
        this.activity = activity;
        this.configuracionTO = configuracionTO;
        this.edtLog = edtLog;

        sincronizacion = new Sincronizacion();
        sincronizacion.setParametros(this);
    }

    @Override
    public String doInBackground(String... urls) {
        actualizaCatalogos();
        return "OK";
    }

    @Override
    protected void onPostExecute(String result) {
        if (taskDelegate != null) {
            taskDelegate.taskCompletionResult(sincronizacionOK
                    ? activity.RESULT_OK : activity.RESULT_CANCELED);
        }
    }

    public void setTaskDelegate(TaskDelegate taskDelegate) {
        this.taskDelegate = taskDelegate;
    }

    private void actualizaCatalogos() {
        sincronizacionOK = false;
        erroresSincronizacion = 0;

        publishProgress("Inicio la carga de los datos.");
        publishProgress("Directorio de carga ["+AndroidApplication.getStorage().getAbsolutePath()+"] ...");

        almacenesFiliales = new HashMap<String,String>();
        almacenesFiliales.put("Mex", "01");
        almacenesFiliales.put("Leo", "02");
        almacenesFiliales.put("Mty", "03");
        almacenesFiliales.put("Oax", "04");
        almacenesFiliales.put("Pue", "05");
        almacenesFiliales.put("Tux", "08");

        existenciasAlmacenes = new HashMap<String,String[]>();
        existenciasAlmacenes.put("01", new String[]{"Mex","Leo","Mty","Oax","Pue","Tux"});
        existenciasAlmacenes.put("02", new String[]{"Mex","Leo","Mty","Oax","Pue","Tux"});
        existenciasAlmacenes.put("03", new String[]{"Mex","Leo","Mty","Oax","Pue","Tux"});
        existenciasAlmacenes.put("04", new String[]{"Mex","Leo","Mty","Oax","Pue","Tux"});
        existenciasAlmacenes.put("05", new String[]{"Mex","Leo","Mty","Oax","Pue","Tux"});
        existenciasAlmacenes.put("08", new String[]{"Mex","Leo","Mty","Oax","Pue","Tux"});

        externalStorage = AndroidApplication.getStorage();

        String verifica = sincronizacion.verificaConfiguracion(configuracionTO);
        if (verifica == null) {
            ArrayList<CatalogoTO> catalogos = obtenCatalogos();
            if (!catalogos.isEmpty()) {
                cargaCatalogos(catalogos);
                creaRelaciones();
                vacuum();
                sincronizacionOK = erroresSincronizacion == 0;
            }
        } else {
            publishProgress(verifica);
        }

        publishProgress("Fin");
    }

    private void vacuum() {
        publishProgress("Optimizando la base de datos ...");

        db = DatabaseOpenHelper.getInstance().getWritableDatabase();

        try {
            db.execSQL("VACUUM;");

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Exception: "+e.getMessage());
        }

        db.close();
    }

    private ArrayList<CatalogoTO> obtenCatalogos() {
        ArrayList<CatalogoTO> catalogos = new ArrayList<>();
        int totalBytes = 0;

        if (!sincronizacion.checaURL(configuracionTO)) {
            publishProgress("Fallo al checar la URL, salgo de la sincronización.");
            return catalogos;
        }

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
            erroresSincronizacion++;

            //
            // Salgo si falla este catalogo
            //
            return catalogos;
        }

        boolean pasa = verificaToken(informacion);
        if (!pasa) {
            //rutinaDePanico(externalStorage);
            publishProgress("El password central ha cambiado, no se puede continuar con la Sincronización.");
            return catalogos;
        }

        TelephonyManager mTelephonyMgr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        String valoresdispositivo = "&serie="+getSerialNumber()+"&numero="+mTelephonyMgr.getLine1Number()
                +"&sim="+mTelephonyMgr.getSimSerialNumber()+"&imei="+mTelephonyMgr.getDeviceId()
                +"&version="+configuracionTO.version;

        publishProgress("Dispositivo ...");
        File dispositivo = new File(externalStorage, "dispositivo.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=dispositivo"+id+valoresdispositivo, dispositivo.getAbsolutePath());
            //catalogos.add(new CatalogoTO(new DispositivoDAO(), dispositivo));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Parametros ...");
        File parametros = new File(externalStorage, "parametros.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=parametros"+id, parametros.getAbsolutePath());
            catalogos.add(new CatalogoTO(new ParametroDAO(), parametros));

            actualizaParametros(parametros);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("filial ...");
        File filial = new File(externalStorage, "filial.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=filial"+id, filial.getAbsolutePath());
            catalogos.add(new CatalogoTO(new FilialDAO(), filial));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Intermediario ...");
        File intermediario = new File(externalStorage, "intermediario.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=intermediario"+id, intermediario.getAbsolutePath());
            catalogos.add(new CatalogoTO(new IntermediarioDAO(), intermediario));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Clientes ...");
        File clientes = new File(externalStorage, "clientes.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=clientes"+id, clientes.getAbsolutePath());
            catalogos.add(new CatalogoTO(new ClienteDAO(), clientes));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Direcciones Entrega ...");
        File direccionesentrega = new File(externalStorage, "direccionesentrega.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=direccionesEntrega"+id, direccionesentrega.getAbsolutePath());
            catalogos.add(new CatalogoTO(new DireccionEntregaDAO(), direccionesentrega));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Prioridad Envio ...");
        File prioridadenvio = new File(externalStorage, "prioridadenvio.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=prioridadEnvio"+id, prioridadenvio.getAbsolutePath());
            catalogos.add(new CatalogoTO(new PrioridadEnvioDAO(), prioridadenvio));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Tipo de Pedido ...");
        File tipopedido = new File(externalStorage, "tipopedido.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=tipoPedido"+id, tipopedido.getAbsolutePath());
            catalogos.add(new CatalogoTO(new TipoPedidoDAO(), tipopedido));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Motivo de Devolucion ...");
        File motivodevolucion = new File(externalStorage, "motivodevolucion.txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(url+"?response=motivoDevolucion"+id, motivodevolucion.getAbsolutePath());
            catalogos.add(new CatalogoTO(new MotivoDevolucionDAO(), motivodevolucion));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        File zipFile = new File(externalStorage, "zipfile");

        publishProgress("Productos ...");
        File productos = new File(externalStorage, "art"+configuracionTO.filial+".txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(configuracionTO.url+"/sagaji-static/pda/art"+configuracionTO.filial+".zip",
                    zipFile.getAbsolutePath());
            sincronizacion.unZip(externalStorage, zipFile);
            catalogos.add(new CatalogoTO(new ProductoDAO(), productos));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Promociones ...");
        File promociones = new File(externalStorage, "pro"+configuracionTO.filial+".txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(configuracionTO.url+"/sagaji-static/pda/pro"+configuracionTO.filial+".zip",
                    zipFile.getAbsolutePath());
            sincronizacion.unZip(externalStorage, zipFile);
            catalogos.add(new CatalogoTO(new PromocionDAO(), promociones));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Equivalentes ...");
        File equivalentes = new File(externalStorage, "equivale"+configuracionTO.filial+".txt");
        try {
            totalBytes += sincronizacion.downloadFromUrl(configuracionTO.url+"/sagaji-static/pda/equivale"+configuracionTO.filial+".zip",
                    zipFile.getAbsolutePath());
            sincronizacion.unZip(externalStorage, zipFile);
            catalogos.add(new CatalogoTO(new EquivalenteDAO(), equivalentes));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
            erroresSincronizacion++;
        }

        publishProgress("Existencia ...");
        String[] almacenesExistencia = existenciasAlmacenes.get(configuracionTO.filial);
        if(almacenesExistencia!=null) {
            boolean delete = true;
            for(String almacen : almacenesExistencia) {
                File existenciaAlmacen = new File(externalStorage, "exst"+almacen+".zip");
                if (!existenciaAlmacen.exists()) {
                    try {
                        totalBytes += sincronizacion.downloadFromUrl(configuracionTO.url+"/sagaji-static/pda/exst"+almacen+".zip",
                                existenciaAlmacen.getAbsolutePath());
                        sincronizacion.unZip(externalStorage, existenciaAlmacen);
                        File existenciaAlmacenTxt = new File(externalStorage, "exst"+almacen+".txt");
                        if (existenciaAlmacenTxt.exists()) {
                            catalogos.add(new CatalogoTO(new ExistenciaDAO(), existenciaAlmacenTxt, delete));
                            delete = false;
                        }
                    } catch(Exception e) {
                        Log.e(LOGTAG, e.getMessage(), e);
                        publishProgress("Error al obtener la url "+e.getMessage());
                    }
                }
            }
        }

        if (actualizaCategorias()) {
            publishProgress("Categorias ...");
            try {
                totalBytes += sincronizacion.downloadFromUrl(configuracionTO.url+"/sagaji-static/pda/sqlitece.zip",
                        zipFile.getAbsolutePath());
                sincronizacion.unZip(externalStorage, zipFile);
            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);
                publishProgress("Error al obtener la url "+e.getMessage());
                erroresSincronizacion++;
            }
        }

        publishProgress("Total de bytes Recibidos: "+Numero.getIntNumero(totalBytes));
        return catalogos;
    }

    private boolean actualizaCategorias() {
        File database = new File(AndroidApplication.getStorage(), "SQLiteCE.db");
        if (!database.exists())
            return true;
        long diff = System.currentTimeMillis() - database.lastModified();
        long MAX_DIFF = 1000 * 60 * 60 * 24 * 15;
        if (diff > MAX_DIFF)
            return true;
        return false;
    }

    private void cargaCatalogos(ArrayList<CatalogoTO> catalogos) {
        db = DatabaseOpenHelper.getInstance().getWritableDatabase();
        try {
            for (CatalogoTO catalogoTO : catalogos) {
                if (catalogoTO.file.exists())
                    cargaTabla(catalogoTO.dao, catalogoTO.file, catalogoTO.delete);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Exception: "+e.getMessage());
        } finally {
            db.close();
        }
    }

    private void cargaTabla(DatabaseRecord dao, File file, boolean delete) {
        publishProgress("Cargo tabla ["+dao.getTable()+"] de archivo ["+file.getAbsolutePath()+"] ...");

        db.beginTransaction();
        try {
            String table = dao.getTable();
            if (delete) {
                publishProgress("DELETE FROM "+table);
                db.delete(table, null, null);
            }

            StringBuffer insert = new StringBuffer();
            insert.append("INSERT INTO ").append(table).append(" VALUES (");

            Field[] fields = dao.getClass().getFields();
            for (int i = 0; i < fields.length; i++) {
                insert.append("?");
                if ((i+1) < fields.length)
                    insert.append(",");
            }

            insert.append(");");
            publishProgress(insert.toString());

            SQLiteStatement insertStmt = db.compileStatement(insert.toString());
            int count = 0, errores = 0;

            String line = null;
            BufferedReader in = new BufferedReader(new FileReader(file), 1024 * 8);
            while ((line = in.readLine()) != null) {
                count++;
                if ((count % 10000) == 0)
                    publishProgress(String.valueOf(count));

                String[] tokens = line.split("\\|", -1);
                if (tokens.length < fields.length)
                    continue;

                ((DatabaseRecordLoad) dao).loadRecord(insertStmt, tokens);

                try {
                    insertStmt.execute();
                } catch (Exception e) {
                    Log.e(LOGTAG, "Error ["+e.getMessage()+"] en linea ("+count+") ["+line+"].");
                    errores++;
                }
            }
            in.close();

            publishProgress(Numero.getIntNumero(count)+" lineas procesadas.");

            if (errores > 0)
                publishProgress("Ocurrieron "+errores+" errores.");

            Cursor cursor = db.rawQuery("SELECT count(*) FROM "+table, null);
            if (cursor.moveToFirst())
                publishProgress("Hay ["+Numero.getIntNumero(cursor.getInt(0))+"] registros en "+table+".");
            if (cursor != null && !cursor.isClosed())
                cursor.close();

            db.setTransactionSuccessful();
            file.delete();

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress(e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private void creaRelaciones() {
        publishProgress("Creo las relaciones en la Base de Datos ...");

        db = DatabaseOpenHelper.getInstance().getWritableDatabase();
        db.beginTransaction();
        try {
            //

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Exception: "+e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
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

            if (tokens.length < 4)
                return false;

            String token = tokens[2];
            pasa = configuracionTO.token.compareTo(token) == 0;

            String fechaUltimaSincronizacion = tokens[3];
            publishProgress("La fecha y hora de última sincronización es ["+fechaUltimaSincronizacion+"].");

            String fechaHoraCentral = tokens[4];
            SystemClock.setCurrentTimeMillis(Fecha.getDate(fechaHoraCentral).getTime());
            publishProgress("La fecha y hora del dispositivo es ["+Fecha.getFechaHora()+"] la hora de la central es ["+fechaHoraCentral+"].");

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

    private void actualizaParametros(File file) {
        publishProgress("Actualizo parametros ["+file.getAbsolutePath()+"] ...");

        try {
            String line = null;
            BufferedReader in = new BufferedReader(new FileReader(file), 1024 * 8);
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\\|", -1);
                if (tokens.length < 2)
                    continue;
                configuracionTO.parametros.put(tokens[0], tokens[1]);
            }
            in.close();
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress(e.getMessage());
        }
    }

    private String getSerialNumber() {
        String deviceSN;
        try {
            Class<?> propClass = Class.forName("android.os.SystemProperties");
            Method getProp = propClass.getMethod("get", String.class, String.class);
            deviceSN = (String) getProp.invoke(propClass, "ro.lenovosn2", "");
            if (deviceSN == null || deviceSN.length() == 0)
                deviceSN = Build.SERIAL;
        } catch (Exception e) {
            deviceSN = Build.SERIAL;
        }
        return deviceSN;
    }
}