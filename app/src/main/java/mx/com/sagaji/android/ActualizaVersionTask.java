package mx.com.sagaji.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.FilenameFilter;
import com.atcloud.android.util.Message;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.delegate.TaskDelegate;
import mx.com.sagaji.android.listener.OnMensajeListener;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Sincronizacion;

/**
 * Created by jbecerra.
 */
public class ActualizaVersionTask extends AsyncTask<String, String, String> implements OnMensajeListener {
    public static String LOGTAG = ActualizaVersionTask.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;
    private Sincronizacion sincronizacion;
    private Activity activity;
    private EditText edtLog;
    private String versionCentral;
    private String fechaVersionCentral;
    private boolean instalar;
    private File apkFile;
    private TaskDelegate taskDelegate;

    public ActualizaVersionTask(Activity activity, ConfiguracionTO configuracionTO, EditText edtLog) {
        this.activity = activity;
        this.configuracionTO = configuracionTO;
        this.edtLog = edtLog;

        sincronizacion = new Sincronizacion();
        sincronizacion.setParametros(this);
    }

    @Override
    public String doInBackground(String... urls) {
        instalar = obtenUltimaVersion();
        if (instalar)
            actualizaVersion();
        return "OK";
    }

    @Override
    protected void onPostExecute(String result) {
        if (instalar)
            instalaApk();

        sincronizacion.guardaLog(edtLog.getText().toString().getBytes());

        if (taskDelegate != null)
            taskDelegate.taskCompletionResult(activity.RESULT_OK);
    }

    public void setTaskDelegate(TaskDelegate taskDelegate) {
        this.taskDelegate = taskDelegate;
    }

    private boolean obtenUltimaVersion() {
        if (!sincronizacion.checaURL(configuracionTO)) {
            publishProgress("Fallo al checar la URL, salgo del envio de información.");
            return false;
        }
        ;

        publishProgress("Obteniendo la última version ...");

        File externalStorage = AndroidApplication.getStorage();

        String url = configuracionTO.url+"/sagaji/servlets/ResponseController";
        String id = "&filial="+configuracionTO.filial+"&intermediario="+configuracionTO.intermediario;

        File setencias = new File(externalStorage, "sentencias.sql");
        try {
            sincronizacion.downloadFromUrl(url+"?response=sentencias"+id, setencias.getAbsolutePath());
            aplicaCambiosSQL(setencias);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
        }

        File version = new File(externalStorage, "version.txt");
        try {
            sincronizacion.downloadFromUrl(url+"?response=version"+id, version.getAbsolutePath());
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
        }

        return checaUltimaVersion(version);
    }

    private boolean checaUltimaVersion(File version) {
        boolean pasa = false;
        try {
            BufferedReader in = new BufferedReader(new FileReader(version));
            String line = in.readLine();
            String[] tokens = line.split("\\|");
            in.close();

            version.delete();

            versionCentral = tokens[0];
            fechaVersionCentral = tokens[1];

            int compara = configuracionTO.version.compareTo(versionCentral);
            if (compara < 0) {
                pasa = true;
            } else if (compara > 0) {
                publishProgress("Usted cuenta con una versión ["+configuracionTO.version
                       +"] mayor a la última versión publicada ["+versionCentral+"].");
            } else if (compara == 0) {
                if (configuracionTO.versionFecha.compareTo(fechaVersionCentral) < 0) {
                    pasa = true;
                } else {
                    publishProgress("La versión actual del sistema corresponde con la ultima versión publicada.");
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress(e.getMessage());
        }
        return pasa;
    }

    private void actualizaVersion() {
        publishProgress("Actualizando la Versión ["+versionCentral+"] ...");

        File externalStorage = AndroidApplication.getStorage();

        String urlVersion = configuracionTO.url+"/sagaji-static/versiones/";

        String url = configuracionTO.url+"/sagaji/ResponseController";
        String id = "&filial="+configuracionTO.filial+"&intermediario="+configuracionTO.intermediario
               +"&version="+configuracionTO.version;

        File version = new File(externalStorage, "version.zip");
        try {
            sincronizacion.downloadFromUrl(urlVersion+"version-"+versionCentral+".zip", version.getAbsolutePath());
            sincronizacion.unZip(externalStorage, version);
            version.delete();
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
        }

        File sqlFile = new File(externalStorage, versionCentral+".sql");
        try {
            sincronizacion.downloadFromUrl(url+"?response=versionCambios"+id, sqlFile.getAbsolutePath());
            aplicaCambiosSQL(sqlFile);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress("Error al obtener la url "+e.getMessage());
        }
    }

    private void aplicaCambiosSQL(File sqlFile) {
        if (!sqlFile.exists())
            return;

        publishProgress("Ejecutando las sentencias SQL del archivo ["+sqlFile.getName()+"].");

        try {
            String lineaSQL = null;
            BufferedReader in = new BufferedReader(new FileReader(sqlFile));
            while ((lineaSQL = in.readLine()) != null) {
                if (lineaSQL.isEmpty())
                    continue;

                publishProgress("Ejecutando ["+lineaSQL+"] ...");

                try {
                    if (lineaSQL.startsWith("DROP DATABASE OPERACIONES;")) {
                        DatabaseOperacionesOpenHelper.getInstance().dropDatabase();
                        publishProgress("SQL: OK DDO");

                    } else if (lineaSQL.startsWith("DROP DATABASE CATALOGOS;")) {
                        DatabaseOpenHelper.getInstance().dropDatabase();
                        publishProgress("SQL: OK DDC");

                    } else {
                        String[] tokens = lineaSQL.split("\\|");
                        if (tokens.length > 1) {
                            DatabaseServices ds;
                            if (tokens[0].startsWith("OPERACIONES")) {
                                ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
                            } else {
                                ds = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();
                            }
                            ds.beginTransaction();
                            try {
                                ds.execute(tokens[1]);
                                ds.commit();
                                publishProgress("SQL: OK");
                            } catch (Exception eex) {
                                ds.rollback();
                                publishProgress("Exception: "+eex.getMessage());
                            }
                            ds.close();
                        }
                    }
                } catch (Exception ex) {
                    Log.e(LOGTAG, ex.getMessage(), ex);
                    publishProgress(ex.getMessage());
                }
            }
            in.close();
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            publishProgress(e.getMessage());
        }
        sqlFile.delete();
    }

    public void instalaApk() {
        File[] files = getApkFiles();
        if (files.length == 0) {
            publishProgress("No hay archivos .apk para instalar.");
            return;
        }

        apkFile = files[0];
        apkFile.setReadable(true, false);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                publishProgress("Instalando el apk ["+apkFile.getName()+"] ...");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
        };

        String mensaje = "Una nueva versión del sistema sera instalada ["+versionCentral+"].";
        publishProgress(mensaje);

        Message.mensaje(activity, dialogClickListener, mensaje);
    }

    private File[] getApkFiles() {
        String mensaje = "Obtengo los archivos .apk ...";
        publishProgress(mensaje);

        FilenameFilter filter = new FilenameFilter("*.apk");
        File externalStorage = AndroidApplication.getStorage();

        return externalStorage.listFiles(filter);
    }

    @Override
    public void onMensaje(String message) {
        publishProgress(message);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        log(progress[0]);
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

}