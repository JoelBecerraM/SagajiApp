package mx.com.sagaji.android.to;


import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by jbecerra.
 */
public class ConfiguracionTO implements Serializable {
    public String filial = "";
    public String intermediario = "";
    public String url = "";
    public String token = "";
    public String version = "";
    public String versionFecha = "";
    public int productos = 0;
    public int productosexistencia = 0;
    public boolean checkpassword = true;
    public Date fechaultimasincronizacion = new Date();
    public HashMap<String, String> parametros;

    public ConfiguracionTO() {
        parametros = new HashMap<>();
    }

    @Override
    public String toString() {
        return filial+";"+intermediario+";";
    }
}
