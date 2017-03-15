package mx.com.sagaji.android.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.atcloud.android.util.Message;

import mx.com.sagaji.android.AndroidApplication;
import mx.com.sagaji.android.listener.OnMensajeListener;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.BasicNameValue;
import mx.com.sagaji.android.util.Constantes;
import mx.com.sagaji.android.util.PostResponse;
import mx.com.sagaji.android.util.UploadResponse;

/**
 * Created by jbecerra.
 */
public class Sincronizacion {
    public static String LOGTAG = Sincronizacion.class.getCanonicalName();

    private OnMensajeListener onMensajeListener;

    public boolean checaURL(ConfiguracionTO configuracionTO) {
        onMensajeListener.onMensaje("Verificando disponibilidad de la URL [" + configuracionTO.url + "] ...");
        if (!checaDisponibilidad(configuracionTO.url)) {
            onMensajeListener.onMensaje("No se alcanza la URL, cambio a la URL alterna ...");
            configuracionTO.url = configuracionTO.parametros.get("URLAlterna");
            if (configuracionTO.url == null) {
                onMensajeListener.onMensaje("No esta definida la URL alterna.");
                return false;
            }

            onMensajeListener.onMensaje("Verificando disponibilidad de la URL alterna [" + configuracionTO.url + "] ...");
            if (!checaDisponibilidad(configuracionTO.url)) {
                onMensajeListener.onMensaje("No se alcanza la URL alterna.");
                return false;
            }
        }

        onMensajeListener.onMensaje("OK con la URL [" + configuracionTO.url + "] continuo con la sincronizacion.");
        return true;
    }

    public String verificaConfiguracion(ConfiguracionTO configuracionTO) {
        if (configuracionTO.filial.length() == 0) {
            return "Debe de especificar el número de Filial.";
        }
        if (configuracionTO.intermediario.length() == 0) {
            return "Debe de especificar el número de Intermediario.";
        }
        if (configuracionTO.url.length() == 0) {
            return "Debe de especificar la URL de Sincronización.";
        }
        if (!configuracionTO.url.startsWith("http")) {
            return "La URL de Sincronización esta mal definida.";
        }
        return null;
    }

    public boolean checaDisponibilidad(String surl) {
        try {
            URL url = new URL(surl);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("User-Agent", "Android Application");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(10 * 1000);
            urlc.connect();
            boolean isReachable = (urlc.getResponseCode() == 200);
            return isReachable;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        return false;
    }

    public boolean hayConexionAInternet(Activity activity) {
        try {
            boolean haveConnectedWifi = false;
            boolean haveConnectedMobile = false;

            ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
            return haveConnectedWifi || haveConnectedMobile;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(activity, e.getMessage());
        }
        return false;
    }

    public void setParametros(OnMensajeListener onMensajeListener) {
        this.onMensajeListener = onMensajeListener;
    }

    public String getRespuesta(String respuesta) {
        int begin = respuesta.indexOf("[", 0);
        if (begin == -1)
            return null;
        int end = respuesta.indexOf("]", 0);
        if (end == -1)
            return null;
        return respuesta.substring(begin, end + 1);
    }

    public boolean okRespuesta(String respuesta) {
        String mensaje;
        int index = 0;

        index = respuesta.indexOf("*", 0);
        if (index != -1) {
            index = respuesta.indexOf("|", 0);
            if (index != -1) {
                mensaje = respuesta.substring(index + 1);
                index = mensaje.indexOf("|", 0);
                if (index != -1)
                    mensaje = mensaje.substring(0, index);

                //Mensaje.exclamation(mensaje);
                return false;
            }
        } else {
            index = respuesta.indexOf("[", 0);
            mensaje = respuesta.substring(index + 1);
            index = mensaje.indexOf("]", 0);
            if (index == -1) {
                mensaje = "Respuesta invalida -" + respuesta + "-";

                //Mensaje.exclamation(mensaje);
                return false;
            } else {
                mensaje = mensaje.substring(0, index);
                return true;
            }
        }

        return false;
    }

    public PostResponse getResponse(String URL, String data) throws Exception {
        try {
            PostResponse postResponse = new PostResponse();

            long startTime = System.currentTimeMillis();

            onMensajeListener.onMensaje("Enviando "+URL+data+" ...");
            URL url = new URL(URL+data);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            // JBM: Works whit http://23.250.116.6/Service.asmx, don't use setDoOutput(true)
            //urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("GET");
            //

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            br.close();
            in.close();

            postResponse.responseCode = urlConnection.getResponseCode();
            postResponse.responseMessage = urlConnection.getResponseMessage();
            postResponse.contentLength = urlConnection.getContentLength();
            postResponse.response = sb.toString();

            urlConnection.disconnect();

            long endTime = System.currentTimeMillis();
            double elapsed = (double)(endTime - startTime) / 1000.0;

            onMensajeListener.onMensaje("Envio listo en " + elapsed + " segundos.");

            return postResponse;

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            onMensajeListener.onMensaje("Posting error: " + e);

            throw e;
        }
    }

    public PostResponse postService(String postURL, ArrayList<BasicNameValue> values) throws Exception {
        try {
            PostResponse postResponse = new PostResponse();

            long startTime = System.currentTimeMillis();

            onMensajeListener.onMensaje("Enviando " + postURL + " ...");
            URL url = new URL(postURL);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");

            try {
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (BasicNameValue entry : values) {
                    if (first)
                        first = false;
                    else
                        result.append("&");

                    result.append(URLEncoder.encode(entry.name, "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.value.toString(), "UTF-8"));
                }

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.write(result.toString().getBytes());

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = br.readLine();
                StringBuilder sb = new StringBuilder();
                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                br.close();
                in.close();

                postResponse.response = sb.toString();
            } catch(Exception e) {
            }

            postResponse.responseCode = urlConnection.getResponseCode();
            postResponse.responseMessage = urlConnection.getResponseMessage();
            postResponse.contentLength = urlConnection.getContentLength();

            urlConnection.disconnect();

            long endTime = System.currentTimeMillis();
            double elapsed = (double) (endTime - startTime) / 1000.0;

            onMensajeListener.onMensaje("Envio listo en " + elapsed + " segundos.");

            return postResponse;

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            onMensajeListener.onMensaje("Posting error: " + e);

            throw e;
        }
    }

    public PostResponse postService(String postURL, String data) throws Exception {
        try {
            PostResponse postResponse = new PostResponse();

            long startTime = System.currentTimeMillis();

            onMensajeListener.onMensaje("Enviando " + postURL + " ...");
            URL url = new URL(postURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(data.getBytes());

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            br.close();
            in.close();

            postResponse.responseCode = urlConnection.getResponseCode();
            postResponse.responseMessage = urlConnection.getResponseMessage();
            postResponse.contentLength = urlConnection.getContentLength();
            postResponse.response = sb.toString();

            urlConnection.disconnect();

            long endTime = System.currentTimeMillis();
            double elapsed = (double) (endTime - startTime) / 1000.0;

            onMensajeListener.onMensaje("Envio listo en " + elapsed + " segundos.");

            return postResponse;

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            onMensajeListener.onMensaje("Posting error: " + e);

            throw e;
        }
    }

    public int downloadFromUrl(String fileURL, String fileName) throws Exception {
        try {
            long startTime = System.currentTimeMillis();

            onMensajeListener.onMensaje("Descargando " + fileURL + " ...");
            URL url = new URL(fileURL);
            File file = new File(fileName);

            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();

            BufferedInputStream bis = new BufferedInputStream(is, 1024 * 8);
            FileOutputStream fos = new FileOutputStream(file);

            int read = 0;
            int totalBytes = 0;
            byte[] buffer = new byte[1024 * 10];
            while ((read = bis.read(buffer)) != -1) {
                totalBytes += read;
                fos.write(buffer, 0, read);
            }

            fos.close();

            bis.close();
            is.close();

            long endTime = System.currentTimeMillis();
            double elapsed = (double) (endTime - startTime) / 1000.0;

            onMensajeListener.onMensaje("Descarga lista en " + elapsed + " segundos.");
            return totalBytes;

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            onMensajeListener.onMensaje("Download error:" + e);

            throw e;
        }
    }

    public UploadResponse uploadFile(String uploadURL, File file, String fileName, List<BasicNameValue> values) throws Exception {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        URL url = new URL(uploadURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

        // Send values ...
        for (BasicNameValue value : values) {
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + value.name + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(value.value.toString());
            dos.writeBytes(lineEnd);
        }

        // Send multipart file ...
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + lineEnd);
        dos.writeBytes(lineEnd);

        // Create a buffer of maximum size
        FileInputStream fileInputStream = new FileInputStream(file);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1 * 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // Read file and write it into form ...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            dos.write(buffer, 0, bufferSize);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        fileInputStream.close();

        // Send multipart form data necesssary after file data ...
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        dos.flush();
        dos.close();

        // Responses from the server (code and message)
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.responseCode = conn.getResponseCode();
        uploadResponse.responseMessage = conn.getResponseMessage();
        uploadResponse.contentLength = conn.getContentLength();

        if (uploadResponse.responseCode == 200) {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            br.close();
            in.close();

            uploadResponse.response = sb.toString();
        }

        onMensajeListener.onMensaje("HTTP response is: " + uploadResponse.responseMessage + ":" + uploadResponse.responseCode);

        return uploadResponse;
    }

    public void unZip(File externalStorage, File file) throws Exception {
        onMensajeListener.onMensaje("UnZip " + file.getAbsolutePath() + " ...");

        byte[] buffer = new byte[1024];
        FileInputStream fin = new FileInputStream(file);
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry ze = null;
        try {
            while ((ze = zin.getNextEntry()) != null) {
                onMensajeListener.onMensaje("Descomprimo " + ze.getName() + " ...");

                if (ze.isDirectory()) {
                    File dir = new File(externalStorage, ze.getName());
                    dir.mkdirs();
                } else {

                    try {
                        FileOutputStream fout = new FileOutputStream(new File(externalStorage, ze.getName()));
                        int read = 0;
                        while ((read = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, read);
                        }
                        fout.close();
                    } catch (Exception e) {
                        zin.close();

                        Log.e(LOGTAG, e.getMessage(), e);
                        onMensajeListener.onMensaje("Error al descomprimir " + e.getMessage());
                        throw e;
                    }

                    zin.closeEntry();
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            onMensajeListener.onMensaje("Error " + e.getMessage());

            throw e;
        } finally {
            zin.close();
            fin.close();

            file.delete();
        }
    }

    public void guardaLog(byte[] bytes) {
        try {
            File externalStorage = AndroidApplication.getStorage();
            File log = new File(externalStorage, Constantes.SINCRONIZACION_LOG);

            FileOutputStream out = new FileOutputStream(log, true);
            out.write("\n\n".getBytes());
            out.write(bytes);
            out.close();
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
    }
}