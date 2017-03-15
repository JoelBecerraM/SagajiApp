package mx.com.sagaji.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.atcloud.android.util.Message;

import mx.com.sagaji.android.to.CobranzaGuardarTO;

public class CobranzaGuardarActivity extends AppCompatActivity {
    public static String LOGTAG = CobranzaGuardarActivity.class.getCanonicalName();

    private boolean salir = false;
    private EditText edtObservaciones = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranzaguardar);

        setTitle("Cobranza Guardar");

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
        getMenuInflater().inflate(R.menu.menu_pedidos_guardar, menu);
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
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pasarReferencia();
            }
        });

        Button btnCancelar = (Button)findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
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
        CobranzaGuardarTO cobranzaGuardarTO = new CobranzaGuardarTO();
        cobranzaGuardarTO.salir = salir;
        cobranzaGuardarTO.observaciones = edtObservaciones.getText().toString();

        cobranzaGuardarTO.observaciones = cobranzaGuardarTO.observaciones.toUpperCase();
        cobranzaGuardarTO.observaciones = cobranzaGuardarTO.observaciones.replaceAll("\n","");
        edtObservaciones.setText(cobranzaGuardarTO.observaciones);

        Intent intent = new Intent();
        intent.putExtra("cobranza-guardar", cobranzaGuardarTO);

        setResult(RESULT_OK, intent);

        finish();
    }
}