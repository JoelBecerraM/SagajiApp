package mx.com.sagaji.android.to;

import java.io.Serializable;

/**
 * Created by jbecerra.
 */
public class ClienteTO implements Serializable {
    public String cliente = "";
    public String razonsocial = "";
    public String propietario = "";
    public String rfc = "";
    public double mnsaldo = 0.0;
    public int ubicado = 0;

    @Override
    public String toString() {
        return cliente+";"+razonsocial+";"+propietario+";";
    }
}