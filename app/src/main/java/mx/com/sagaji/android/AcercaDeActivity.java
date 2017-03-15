package mx.com.sagaji.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Constantes;

public class AcercaDeActivity extends AppCompatActivity {
    public static String LOGTAG = AcercaDeActivity.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acercade);

        setTitle("Acerca De");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            configuracionTO = (ConfiguracionTO) extras.getSerializable(Constantes.EXTRA_CONFIGURACION);
        }

        Fragment fragment = new AcercaDeFragment();
        ((AcercaDeFragment) fragment).setParametros(configuracionTO);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_acercade, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}