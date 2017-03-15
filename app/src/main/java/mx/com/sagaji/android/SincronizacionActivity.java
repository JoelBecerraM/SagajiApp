package mx.com.sagaji.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import com.atcloud.android.util.Message;

import mx.com.sagaji.android.listener.OnSincronizacionListener;
import mx.com.sagaji.android.to.ConfiguracionTO;

/**
 * Created by jbecerra.
 */
public class SincronizacionActivity extends AppCompatActivity implements OnSincronizacionListener {
    public static String LOGTAG = SincronizacionActivity.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;
    private boolean sincronizando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizacion);

        setTitle("Sincronización");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            configuracionTO = (ConfiguracionTO) extras.getSerializable("configuracion");
        }

        Fragment fragment = new SincronizacionFragment();
        ((SincronizacionFragment) fragment).setParametros(configuracionTO);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (sincronizando) {
                Message.alert(this, "No puede salir de la Sincronización en este momento.");
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sincronizacion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            if (sincronizando) {
                Message.alert(this, "No puede salir de la Sincronización en este momento.");
                return true;
            }

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onIniciaSincronizacion() {
        sincronizando = true;
    }

    @Override
    public void onTerminaSincronizacion() {
        sincronizando = false;
    }
}