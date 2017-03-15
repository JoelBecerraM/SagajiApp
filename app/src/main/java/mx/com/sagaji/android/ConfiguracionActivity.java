package mx.com.sagaji.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Constantes;

public class ConfiguracionActivity extends AppCompatActivity {
    public static String LOGTAG = ConfiguracionActivity.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;
    private Handler handler = new Handler();
    private AlertDialog passwordDialog = null;
    private EditText edtPassword = null;
    private int intentos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        setTitle("Configuración");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            configuracionTO = (ConfiguracionTO) extras.getSerializable(Constantes.EXTRA_CONFIGURACION);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.frame, new ConfiguracionFragment())
                .commit();

        try {
            if (configuracionTO.checkpassword) {
                edtPassword = new EditText(this);
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                passwordDialog = new AlertDialog.Builder(this)
                        .setTitle("SagajiApp - Configuración")
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
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_configuracion, menu);
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

    private void showPasswordDialog() {
        intentos++;
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
    }

    private void validaPassword() {
        String passwordconfiguracion = configuracionTO.parametros.get(Constantes.PARAMETRO_CONTRASENACONFIGURACION);
        if (passwordconfiguracion == null)
            passwordconfiguracion = "onlysistemas";

        String password = edtPassword.getText().toString();
        if (passwordconfiguracion == null || password.compareTo(passwordconfiguracion) != 0) {
            showPasswordDialog();
        }
    }
}