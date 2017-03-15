package mx.com.sagaji.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Message;
import java.util.List;

import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.to.CategoriaTO;
import mx.com.sagaji.android.to.PedidoGuardarTO;

public class PedidosGuardarActivity extends AppCompatActivity {
    public static String LOGTAG = PedidosGuardarActivity.class.getCanonicalName();

    private String formaenvio = null;
    private boolean salir = false;
    private Spinner spinnerTipo = null;
    private Spinner spinnerClaveEnvio = null;
    private EditText edtObservaciones = null;
    private ArrayAdapter<CategoriaTO> adapterClaveEnvio = null;
    private ArrayAdapter<CategoriaTO> adapterTipoPedido = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidosguardar);

        setTitle("Pedidos Guardar");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            formaenvio = extras.getString("formaenvio");
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
        adapterTipoPedido = new ArrayAdapter<CategoriaTO>(this, android.R.layout.simple_spinner_dropdown_item);

        spinnerTipo = (Spinner)findViewById(R.id.spinnerTipo);
        adapterTipoPedido.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipoPedido);

        adapterClaveEnvio = new ArrayAdapter<CategoriaTO>(this, android.R.layout.simple_spinner_dropdown_item);

        spinnerClaveEnvio = (Spinner)findViewById(R.id.spinnerClaveEnvio);
        adapterClaveEnvio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClaveEnvio.setAdapter(adapterClaveEnvio);

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

        cargaTiposPedido();
        cargaClavesEnvio();
        colocaFormaEnvio(formaenvio);
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
        PedidoGuardarTO pedidoGuardarTO = new PedidoGuardarTO();
        pedidoGuardarTO.salir = salir;
        pedidoGuardarTO.tipo = ((CategoriaTO)spinnerTipo.getSelectedItem()).clave;
        pedidoGuardarTO.claveenvio = ((CategoriaTO)spinnerClaveEnvio.getSelectedItem()).clave;
        pedidoGuardarTO.observaciones = edtObservaciones.getText().toString();

        pedidoGuardarTO.observaciones = pedidoGuardarTO.observaciones.toUpperCase();
        edtObservaciones.setText(pedidoGuardarTO.observaciones);

        Intent intent = new Intent();
        intent.putExtra("pedido-guardar", pedidoGuardarTO);

        setResult(RESULT_OK, intent);

        finish();
    }

    private void colocaFormaEnvio(String formaenvio) {
        int index = 0;
        for (int i=0; i<spinnerClaveEnvio.getCount(); i++) {
            CategoriaTO categoriaTO = (CategoriaTO)spinnerClaveEnvio.getItemAtPosition(i);
            if (categoriaTO.clave.compareTo(formaenvio)==0) {
                index = i;
                break;
            }
        }
        spinnerClaveEnvio.setSelection(index);
    }

    private void cargaTiposPedido() {
        adapterTipoPedido.clear();

        try {
            DatabaseServices ds = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();

            List<?> array = ds.collection(new CategoriaTO(),
                    "SELECT clave, descripcion FROM TipoPedido ORDER BY clave;");

            ds.close();

            if (array.size()!=0) {
                for (Object categoriaTO : array) {
                    adapterTipoPedido.add((CategoriaTO)categoriaTO);
                }
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void cargaClavesEnvio() {
        adapterClaveEnvio.clear();

        try {
            DatabaseServices ds = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();

            List<?> array = ds.collection(new CategoriaTO(),
                    "SELECT clave, descripcion FROM PrioridadEnvio ORDER BY clave;");

            ds.close();

            if (array.size()!=0) {
                for (Object categoriaTO : array) {
                    adapterClaveEnvio.add((CategoriaTO)categoriaTO);
                }
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }
}
