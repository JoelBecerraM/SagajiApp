package mx.com.sagaji.android;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Message;
import com.atcloud.android.view.DatePickerFragment;
import java.util.Calendar;

import mx.com.sagaji.android.to.CobranzaReferenciaTO;

public class CobranzaReferenciaActivity extends AppCompatActivity {
    public static String LOGTAG = CobranzaReferenciaActivity.class.getCanonicalName();

    private Calendar calendarFecha;
    private TextView edtReferencia;
    private TextView txtFechaCobro;
    private Spinner spinnerBanco;
    private String referencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranzareferencia);

        setTitle("Cobranza Referencia");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            referencia = extras.getString("referencia");
        }

        setUpView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cobranza_referencia, menu);
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
        spinnerBanco = (Spinner)findViewById(R.id.spinnerBanco);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.bancos_cobranza, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanco.setAdapter(adapter);

        calendarFecha = Calendar.getInstance();
        calendarFecha.add(Calendar.DAY_OF_MONTH, 1);
        calendarFecha.set(Calendar.HOUR, 0);
        calendarFecha.set(Calendar.MINUTE, 0);
        calendarFecha.set(Calendar.SECOND, 0);
        calendarFecha.set(Calendar.MILLISECOND, 0);

        Button btnPickDate = (Button)findViewById(R.id.btnPickDate);
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                elijeFecha();
            }
        });

        edtReferencia = (EditText)findViewById(R.id.edtReferencia);
        edtReferencia.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_NONE
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    //enterOnReferencia(edtReferencia.getText().toString());
                    return true;
                }
                return false;
            }
        });
        edtReferencia.setText(referencia);

        txtFechaCobro = (TextView)findViewById(R.id.txtFechaCobro);

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

    private void elijeFecha() {
        @SuppressLint("ValidFragment")
        DialogFragment datePickerFragment = new DatePickerFragment() {
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendarFecha.set(Calendar.YEAR, year);
                calendarFecha.set(Calendar.MONTH, month);
                calendarFecha.set(Calendar.DAY_OF_MONTH, day);

                txtFechaCobro.setText(Fecha.getFecha(calendarFecha.getTime()));
            }
        };

        Bundle bundle = new Bundle();
        bundle.putSerializable("date", calendarFecha);

        datePickerFragment.setArguments(bundle);
        datePickerFragment.show(this.getSupportFragmentManager(), "datePicker");
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
        String banco = spinnerBanco.getSelectedItem().toString();

        CobranzaReferenciaTO cobranzaReferenciaTO = new CobranzaReferenciaTO();
        cobranzaReferenciaTO.banco = banco;
        cobranzaReferenciaTO.referencia = edtReferencia.getText().toString();
        cobranzaReferenciaTO.fechacobro = txtFechaCobro.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("cobranza-referencia", cobranzaReferenciaTO);

        setResult(RESULT_OK, intent);

        finish();
    }
}