package mx.com.sagaji.android;

import com.atcloud.android.util.Message;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import mx.com.sagaji.android.to.DevolucionGuardarTO;

public class DevolucionesGuardarActivity extends AppCompatActivity {
    public static String LOGTAG = DevolucionesGuardarActivity.class.getCanonicalName();

    private boolean salir = false;
    private EditText edtObservaciones = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devolucionesguardar);

        setTitle("Devoluciones Guardar");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            salir = extras.getBoolean("salir");
        }

        setUpView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_devoluciones_guardar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            salir();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            salir();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setUpView() {
        edtObservaciones = (EditText)findViewById(R.id.edtObservaciones);

        Button btnAceptar = (Button)findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                pasarReferencia();
            }
        });
        Button btnCancelar = (Button)findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                cancelaReferencia();
            }
        });
    }

    private void salir() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        pasarReferencia();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        cancelaReferencia();
                        break;
                }
                dialog.dismiss();
            }
        };

        Message.question(this, dialogClickListener, "¿Desea pasar los datos?");
    }

    private void cancelaReferencia() {
        setResult(RESULT_CANCELED);

        finish();
    }

    private void pasarReferencia() {
        DevolucionGuardarTO devolucionGuardarTO = new DevolucionGuardarTO();
        devolucionGuardarTO.salir = salir;
        devolucionGuardarTO.observaciones = edtObservaciones.getText().toString();

        devolucionGuardarTO.observaciones = devolucionGuardarTO.observaciones.toUpperCase();
        edtObservaciones.setText(devolucionGuardarTO.observaciones);

        Intent intent = new Intent();
        intent.putExtra("devolucion-guardar", devolucionGuardarTO);

        setResult(RESULT_OK, intent);

        finish();
    }
}