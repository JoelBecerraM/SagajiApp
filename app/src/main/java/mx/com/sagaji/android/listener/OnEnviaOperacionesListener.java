package mx.com.sagaji.android.listener;

import com.atcloud.android.dao.engine.DatabaseRecord;

public interface OnEnviaOperacionesListener {
    public long onCuentaRegistros(DatabaseRecord dao);
    public void onIniciaEnvioOperaciones();
    public void onTerminaEnvioOperaciones();
}
