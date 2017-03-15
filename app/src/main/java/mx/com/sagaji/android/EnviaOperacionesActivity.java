package mx.com.sagaji.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Message;

import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.listener.OnEnviaOperacionesListener;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Constantes;

public class EnviaOperacionesActivity extends AppCompatActivity implements OnEnviaOperacionesListener {
    public static String LOGTAG = EnviaOperacionesActivity.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;
    private boolean enviando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviaoperaciones);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Envia Operaciones");

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            configuracionTO = (ConfiguracionTO)extras.getSerializable(Constantes.EXTRA_CONFIGURACION);
        }

        Fragment fragment = new EnviaOperacionesFragment();
        ((EnviaOperacionesFragment)fragment).setParametros(configuracionTO);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (enviando) {
                Message.alert(this, "No puede salir del Envío de Operaciones en este momento.");
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_enviaoperaciones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            if (enviando) {
                Message.alert(this, "No puede salir del Envío de Operaciones en este momento.");
                return true;
            }

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public long onCuentaRegistros(DatabaseRecord dao) {
        long operaciones = 0;

        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        try {
            operaciones = ds.count(dao, "status = '"+Constantes.ESTADO_TERMINADO+"'");
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        ds.close();

        return operaciones;
    }

    @Override
    public void onIniciaEnvioOperaciones() {
        enviando = true;
    }

    @Override
    public void onTerminaEnvioOperaciones() {
        enviando = false;
    }
}