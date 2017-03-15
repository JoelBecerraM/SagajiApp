package mx.com.sagaji.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Message;
import com.atcloud.android.util.Numero;
import java.util.List;

import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.entity.InformacionDAO;
import mx.com.sagaji.android.dao.entity.ParametroDAO;
import mx.com.sagaji.android.dao.entity.ProductoDAO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Constantes;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static String LOGTAG = MainActivity.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;

    private Handler handler = new Handler();
    private AlertDialog passwordDialog = null;
    private EditText edtPassword = null;
    private int intentos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getConfiguracion();

        configuracionTO.checkpassword = false;
        try {
            if (configuracionTO.checkpassword) {
                edtPassword = new EditText(this);
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                passwordDialog = new AlertDialog.Builder(this)
                        .setTitle("SagajiApp")
                        .setMessage("Contraseña:")
                        .setView(edtPassword)
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                validaPassword();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                salir();
                            }
                        })
                        .create();

                showPasswordDialog();
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setTextView(R.id.txtFilial, String.valueOf(configuracionTO.filial));
        setTextView(R.id.txtIntermediario, configuracionTO.intermediario);
        setTextView(R.id.txtFechaSincronizacion, Fecha.getFechaHora(configuracionTO.fechaultimasincronizacion));
        setTextView(R.id.txtArticulos, Numero.getIntNumero(configuracionTO.productos));
        setTextView(R.id.txtArticulosExistencia, Numero.getIntNumero(configuracionTO.productosexistencia));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        displayNavigationItem(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constantes.ACTIVITY_SINCRONIZACION_CATALOGOS) {
            //if (resultCode == RESULT_OK) {
            getConfiguracion();
            //}
        }
        else if (requestCode == Constantes.ACTIVITY_CONFIGURACION) {
            //if (resultCode == RESULT_OK) {
            getConfiguracion();
            //}
        }
    }

    private void displayNavigationItem(int id) {
        if (id == R.id.nav_pedidos || id == R.id.nav_devoluciones
                || id == R.id.nav_cobranza) {
            if (!validaFechaSincronizacion())
                return;

            Bundle bundle = new Bundle();
            bundle.putInt(Constantes.EXTRA_ID, id);
            bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);

            Intent intent = new Intent(this, ClientesActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);

        } else if (id == R.id.nav_sincroniza_catalogos) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);

            Intent intent = new Intent(this, SincronizacionActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constantes.ACTIVITY_SINCRONIZACION_CATALOGOS);

        } else if (id == R.id.nav_envia_operaciones) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);

            Intent intent = new Intent(this, EnviaOperacionesActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);

        } else if (id == R.id.nav_configuracion) {
            if (!validaFechaSincronizacion())
                return;

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);

            Intent intent = new Intent(this, ConfiguracionActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constantes.ACTIVITY_CONFIGURACION);

        } else if (id == R.id.nav_acercade) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);

            Intent intent = new Intent(this, AcercaDeActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);

        }
    }

    private boolean validaFechaSincronizacion() {
        long elapsed = System.currentTimeMillis() - configuracionTO.fechaultimasincronizacion.getTime();
        long espera_max_sincronizacion =
                Numero.getLongFromString(configuracionTO.parametros.get(Constantes.PARAMETRO_ESPERAMAXSINCRONIZACION),
                        Constantes.ESPERA_MAX_SINCRONIZACION);

        if (elapsed > espera_max_sincronizacion) {
            int minutos = (int)(elapsed / (1000 * 60 * 60));
            Message.alert(this, "Han pasado mas de ("+Numero.getIntNumero(minutos)+") horas desde su última sincronizacion, "
                    +"debe de actualizar los Catálogos en su dispositivo.");

            return false;
        }
        return true;
    }

    private void getConfiguracion() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        configuracionTO = new ConfiguracionTO();
        configuracionTO.intermediario = sharedPref.getString("intermediario", "");
        configuracionTO.filial = sharedPref.getString("filial", "");
        configuracionTO.url = sharedPref.getString("url", "");
        configuracionTO.token = sharedPref.getString("token", "");
        configuracionTO.version = getResources().getString(R.string.version_number);
        configuracionTO.versionFecha = getResources().getString(R.string.version_date);

        DatabaseServices ds = DatabaseOpenHelper.getInstance().getReadableDatabaseServices();
        try {
            InformacionDAO informacionDAO = (InformacionDAO)ds.first(new InformacionDAO());
            if (informacionDAO!=null) {
                configuracionTO.fechaultimasincronizacion = informacionDAO.fechacentral;
                configuracionTO.token = informacionDAO.token;
            }

            configuracionTO.productos = (int)ds.count(new ProductoDAO());
            configuracionTO.productosexistencia = (int)ds.count(new ProductoDAO(), "existencia > 0");

            List array = ds.select(new ParametroDAO(), "activo = '1'");
            for(Object object : array) {
                ParametroDAO parametroDAO = (ParametroDAO)object;
                configuracionTO.parametros.put(parametroDAO.parametro, parametroDAO.valor);
            }

            AndroidApplication.setConfiguracion(configuracionTO);
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        } finally {
            ds.close();
        }
    }

    private void setTextView(int resource, String value) {
        TextView textView = (TextView)findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }

    private void showPasswordDialog() {
        intentos ++;
        if (intentos > 3) {
            salir();
            return;
        }

        handler.post(new Runnable() {
            public void run() {
                showPasswordDialog(true);
            }
        });
    }

    private void showPasswordDialog(boolean dialog) {
        edtPassword.selectAll();

        passwordDialog.show();
    }

    private void salir() {
        finish();

        System.exit(0);
    }

    private void validaPassword() {
        String password = edtPassword.getText().toString();
        if (configuracionTO.token==null||password.compareTo(configuracionTO.token)!=0) {
            showPasswordDialog();
        }
    }
}