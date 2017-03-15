package mx.com.sagaji.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Message;
import com.atcloud.android.util.Numero;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mx.com.sagaji.android.adapter.CobranzaDescuentosAdapter;
import mx.com.sagaji.android.adapter.CobranzaDetalleAdapter;
import mx.com.sagaji.android.adapter.CobranzaDocumentosAdapter;
import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.dao.GenericDAO;
import mx.com.sagaji.android.dao.entity.ClienteDAO;
import mx.com.sagaji.android.dao.entity.CobranzaDAO;
import mx.com.sagaji.android.dao.entity.CobranzaDepositoDAO;
import mx.com.sagaji.android.dao.entity.DetCobranzaDAO;
import mx.com.sagaji.android.to.ClienteTO;
import mx.com.sagaji.android.to.CobranzaDetalleTO;
import mx.com.sagaji.android.to.CobranzaGuardarTO;
import mx.com.sagaji.android.to.CobranzaReferenciaTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.to.DescuentoCobranzaTO;
import mx.com.sagaji.android.to.FacturaCobranzaTO;
import mx.com.sagaji.android.util.ActivityUtil;
import mx.com.sagaji.android.util.Constantes;
import mx.com.sagaji.android.ws.FacturasCobranzaTask;
import mx.com.sagaji.android.ws.SagajiWebService;

public class CobranzaActivity extends AppCompatActivity {
    public static String LOGTAG = CobranzaActivity.class.getCanonicalName();

    private SagajiWebService sagajiWebService;
    private ConfiguracionTO configuracionTO;
    private ClienteTO clienteTO;
    private Date feiniciovisita;
    private DatabaseServices ds = null;
    private ListView lstDocumentos = null;
    private ListView lstDescuentos = null;
    private ListView lstDetalles = null;
    private CobranzaDocumentosAdapter documentosAdapter = null;
    private CobranzaDescuentosAdapter descuentosAdapter = null;
    private CobranzaDetalleAdapter detCobranzaAdapter = null;
    private CobranzaDetalleTO detCobranzaTO = null;
    private ClienteDAO clienteDAO = null;
    private FacturaCobranzaTO facturaCobranzaTO = null;
    private DescuentoCobranzaTO descuentoCobranzaTO = null;
    private EditText edtPago = null;
    private EditText edtImporte = null;
    private EditText edtDescuento = null;
    private Spinner spinnerPago = null;
    private CobranzaReferenciaTO cobranzaReferenciaTO = null;
    private int captura = 0;
    private double totTotal;
    private TextView txtTotal = null;
    private int folioCobranza = 0;
    private String filial = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranza);

        setTitle("Cobranza");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            configuracionTO = (ConfiguracionTO)extras.getSerializable(Constantes.EXTRA_CONFIGURACION);
            clienteTO = (ClienteTO)extras.getSerializable(Constantes.EXTRA_CLIENTE);
            feiniciovisita = (Date)extras.getSerializable(Constantes.EXTRA_INICIOVISTA);
        }

        sagajiWebService = new SagajiWebService();
        sagajiWebService.setParametros(configuracionTO);

        setUpView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ds.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cobranza, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_rotate) {
            ActivityUtil.rotar(this);
            return true;
        }
        else if (id == R.id.action_calculadora) {
            calculadora();
            return true;
        }
        else if (id == R.id.action_back) {
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opcLimpiar:
                limpiarDetalles();
                return true;
            case R.id.opcBorrar:
                borrarDetalle();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cobranza_detalles, menu);
    }

    private void setUpView() {
        lstDocumentos = (ListView)findViewById(R.id.lstDocumentos);

        documentosAdapter = new CobranzaDocumentosAdapter(this, R.layout.layout_cobranza_documentos_detalles);
        lstDocumentos.setAdapter(documentosAdapter);
        lstDocumentos.setTextFilterEnabled(true);
        lstDocumentos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                facturaCobranzaTO = (FacturaCobranzaTO)parent.getAdapter().getItem(position);

                verDescuentos();
            }
        });
        lstDocumentos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                facturaCobranzaTO = (FacturaCobranzaTO)parent.getAdapter().getItem(position);

                verDescuentos();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        lstDescuentos = (ListView)findViewById(R.id.lstDescuentos);

        descuentosAdapter = new CobranzaDescuentosAdapter(this, R.layout.layout_cobranza_descuentos_detalles);
        lstDescuentos.setAdapter(descuentosAdapter);
        lstDescuentos.setTextFilterEnabled(true);
        lstDescuentos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                descuentoCobranzaTO = (DescuentoCobranzaTO)parent.getAdapter().getItem(position);

                verDetalleDescuento();
            }
        });
        lstDescuentos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                descuentoCobranzaTO = (DescuentoCobranzaTO)parent.getAdapter().getItem(position);

                verDetalleDescuento();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerPago = (Spinner)findViewById(R.id.spinnerPago);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.pagos_cobranza, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPago.setAdapter(adapter);
        spinnerPago.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                capturaReferencia();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtPago = (EditText)findViewById(R.id.edtPago);
        edtPago.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_NONE
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    enterOnPago(edtPago.getText().toString());
                    return true;
                }
                return false;
            }
        });
        edtPago.setOnKeyListener(new TextView.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        enterOnPago(edtPago.getText().toString());
                        return true;
                    }
                }
                return false;
            }
        });

        edtImporte = (EditText)findViewById(R.id.edtImporte);
        edtImporte.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_NONE
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    enterOnImporte(edtImporte.getText().toString());
                    return true;
                }
                return false;
            }
        });
        edtImporte.setOnKeyListener(new TextView.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        enterOnImporte(edtImporte.getText().toString());
                        return true;
                    }
                }
                return false;
            }
        });

        edtDescuento = (EditText)findViewById(R.id.edtDescuento);
        edtDescuento.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_NONE
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    enterOnDescuento(edtDescuento.getText().toString());
                    return true;
                }
                return false;
            }
        });
        edtDescuento.setOnKeyListener(new TextView.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        enterOnDescuento(edtDescuento.getText().toString());
                        return true;
                    }
                }
                return false;
            }
        });

        lstDetalles = (ListView)findViewById(R.id.lstDetalles);
        registerForContextMenu(lstDetalles);

        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.layout_cobranza_detalles_header, lstDetalles, false);
        lstDetalles.addHeaderView(header, null, false);

        detCobranzaAdapter = new CobranzaDetalleAdapter(this, R.layout.layout_cobranza_detalles);
        lstDetalles.setAdapter(detCobranzaAdapter);
        lstDetalles.setTextFilterEnabled(true);
        lstDetalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                detCobranzaTO = (CobranzaDetalleTO)parent.getAdapter().getItem(position);

                //verDetalle(detCobranzaTO);
            }
        });
        lstDetalles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                detCobranzaTO = (CobranzaDetalleTO)parent.getAdapter().getItem(position);

                //verDetalle(detCobranzaTO);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        txtTotal = (TextView)findViewById(R.id.txtTotal);

        Button btnBorrar = (Button)findViewById(R.id.btnBorrar);
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                borrarDetalle();
            }
        });
        Button btnSalir = (Button)findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                salir();
            }
        });
        Button btnAnteriores = (Button)findViewById(R.id.btnAnteriores);
        btnAnteriores.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                anteriores();
            }
        });

        ds = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();

        clienteDAO = new ClienteDAO(clienteTO.cliente);
        try {
            ds.exists(clienteDAO);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }

        cargaDocumentos();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constantes.ACTIVITY_REFERENCIA) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if (extras!=null)
                    cobranzaReferenciaTO = (CobranzaReferenciaTO)extras.getSerializable("cobranza-referencia");
            } else {
                cobranzaReferenciaTO = null;
            }
        }
        else if (requestCode == Constantes.ACTIVITY_ANTERIORES) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if(extras !=null) {

                    String accion = extras.getString("accion");

                    if (accion.equals(Constantes.ACCION_ABRIR)) {
                        if (detCobranzaAdapter.getCount()>0) {
                            Message.alert(this, "No se puede abrir un registro anterior cuando ya se ha iniciado la captura de detalles.");
                            return;
                        }

                        int folio = extras.getInt("folio");
                        abrirAnterior(folio);
                    }
                    else if (accion.equals(Constantes.ACCION_IMPRIMIR)) {
                        int folio = extras.getInt("folio");
                        imprimirAnterior(folio);
                    }
                    else if (accion.equals(Constantes.ACCION_BORRAR)) {
                        int folio = extras.getInt("folio");
                        borrarAnterior(folio);
                    }
                    else if (accion.equals(Constantes.ACCION_REENVIAR)) {
                        int folio = extras.getInt("folio");
                        reenviarAnterior(folio);
                    }
                }
            }
        }
        else if (requestCode == Constantes.ACTIVITY_COBRANZA_GUARDAR) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if (extras!=null) {

                    CobranzaGuardarTO cobranzaGuardarTO = (CobranzaGuardarTO)extras.getSerializable("cobranza-guardar");

                    guardarCobranza(cobranzaGuardarTO);

                    if (cobranzaGuardarTO.salir)
                        finish();
                }
            }
        }
    }

    private void salir() {
        if (detCobranzaAdapter.getCount() > 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            guardarCobranza(true);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                        default:
                            finish();
                            break;
                    }
                    dialog.dismiss();
                }
            };

            Message.question(this, dialogClickListener, "¿Desea guardar la cobranza?");

        } else {
            finish();
        }
    }

    private void guardarCobranza(boolean salir) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("salir", salir);

        Intent intent = new Intent(this, CobranzaGuardarActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_COBRANZA_GUARDAR);
    }

    private void guardarCobranza(CobranzaGuardarTO cobranzaGuardarTO) {
        boolean nuevo = folioCobranza == 0;

        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

        if (folioCobranza == 0) {
            ds.beginTransaction();
            try {
                folioCobranza = GenericDAO.obtenerSiguienteFolio(ds, Constantes.FOLIO_COBRANZA);

                ds.commit();
            } catch (Exception e) {
                ds.rollback();

                Log.e(LOGTAG, e.getMessage(), e);
                Message.alert(this, "Error al obtener el folio de cobranza: " + e.getMessage());
            }
        }

        ds.beginTransaction();

        try {
            CobranzaDAO cobranzaDAO = new CobranzaDAO();
            cobranzaDAO.folio = folioCobranza;
            cobranzaDAO.status = Constantes.ESTADO_TERMINADO;
            cobranzaDAO.fechacreacion = new Date();
            cobranzaDAO.fechamodificacion = new Date();
            cobranzaDAO.filial = filial;
            cobranzaDAO.intermediario = configuracionTO.intermediario;
            cobranzaDAO.cliente = clienteDAO.cliente;
            cobranzaDAO.nombre = clienteDAO.razonsocial;
            cobranzaDAO.tipo = "0";
            cobranzaDAO.lineas = detCobranzaAdapter.getCount();
            cobranzaDAO.piezas = 0;
            cobranzaDAO.total = totTotal;
            cobranzaDAO.respuesta = "";
            cobranzaDAO.impresiones = 0;
            cobranzaDAO.observaciones = cobranzaGuardarTO.observaciones;

            if (nuevo) {
                ds.insert(cobranzaDAO);
            } else {
                CobranzaDAO cobranzaAnteriorDAO = new CobranzaDAO(folioCobranza);
                ds.exists(cobranzaAnteriorDAO);

                cobranzaDAO.fechacreacion = cobranzaAnteriorDAO.fechacreacion;
                cobranzaDAO.impresiones = cobranzaAnteriorDAO.impresiones;
                cobranzaDAO.respuesta = cobranzaAnteriorDAO.respuesta;

                ds.update(cobranzaDAO);
                ds.execute("DELETE FROM DetCobranza WHERE folio = " + folioCobranza);
            }

            List<DatabaseRecord> detalles = new ArrayList<DatabaseRecord>();

            int index = 0;
            for(index = 0; index < cobranzaDAO.lineas; index++) {
                CobranzaDetalleTO detCobranzaTO = detCobranzaAdapter.getItem(index);

                DetCobranzaDAO detCobranzaDAO = new DetCobranzaDAO(folioCobranza, index);
                detCobranzaDAO.documento = detCobranzaTO.documento;
                detCobranzaDAO.tipodocumento = "1";
                detCobranzaDAO.tipopago = detCobranzaTO.tipopago;
                detCobranzaDAO.referencia = detCobranzaTO.referencia;
                detCobranzaDAO.banco = detCobranzaTO.banco;
                detCobranzaDAO.fechacobro = detCobranzaTO.fechacobro;
                detCobranzaDAO.pago = detCobranzaTO.pago;
                detCobranzaDAO.importe = detCobranzaTO.importe;
                detCobranzaDAO.prdescuento = detCobranzaTO.prdescuento;
                detCobranzaDAO.descuento = detCobranzaTO.descuento;

                ds.insert(detCobranzaDAO);

                detalles.add(detCobranzaDAO);
            }

            /*
            HtlDAO htlDAO = new HtlDAO();
            htlDAO.folio = null;
            htlDAO.foliocobranza = folioCobranza;
            htlDAO.status = Constantes.ESTADO_TERMINADO;
            htlDAO.fechacreacion = new Date();
            htlDAO.fechamodificacion = new Date();
            htlDAO.filial = cobranzaDAO.filial;
            htlDAO.agente = cobranzaDAO.agente;
            htlDAO.cliente = cobranzaDAO.cliente;
            htlDAO.monto = cobranzaDAO.total;
            htlDAO.accion = "Registro";

            ds.insert(htlDAO);

            for (DatabaseRecord record : detalles) {
                DetCobranzaDAO detCobranzaDAO = (DetCobranzaDAO)record;

                HtldDAO htldDAO = new HtldDAO();
                htldDAO.folio = null;
                htldDAO.foliocobranza = cobranzaDAO.folio;
                htldDAO.status = Constantes.ESTADO_TERMINADO;
                htldDAO.fechacreacion = new Date();
                htldDAO.fechamodificacion = new Date();
                htldDAO.filial = cobranzaDAO.filial;
                htldDAO.agente = cobranzaDAO.agente;
                htldDAO.cliente = cobranzaDAO.cliente;
                htldDAO.documento = detCobranzaDAO.documento;
                htldDAO.tipodocumento = detCobranzaDAO.tipodocumento;
                htldDAO.tipopago = detCobranzaDAO.tipopago;
                htldDAO.pago = detCobranzaDAO.pago;
                htldDAO.importe = detCobranzaDAO.importe;
                htldDAO.prdescuento = detCobranzaDAO.prdescuento;
                htldDAO.descuento = detCobranzaDAO.descuento;

                ds.insert(htldDAO);
            }
            */

            ds.commit();
        } catch(Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al guardar la cobranza: "+e.getMessage());
        }

        ds.close();

        Toast.makeText(CobranzaActivity.this, "Cobranza ["+folioCobranza+"] guardada correctamente.", Toast.LENGTH_SHORT).show();
    }

    private List<DescuentoCobranzaTO> cargaDescuentos() {
        List<DescuentoCobranzaTO> ret = new ArrayList<DescuentoCobranzaTO>();
        try {
            int diasTranscurridos = facturaCobranzaTO.DiasTrascurridos;

            int tipopago = spinnerPago.getSelectedItemPosition() + 1;
            if (tipopago==2||tipopago==3||tipopago==4) {
                if (cobranzaReferenciaTO!=null) {
                    diasTranscurridos = (int)Fecha.elapsedFrom(facturaCobranzaTO.Fecha_Factura,
                            Fecha.getFecha(cobranzaReferenciaTO.fechacobro), Calendar.DATE);
                }
            }

            List<?> array = ds.collection(new DescuentoCobranzaTO(),
                    "SELECT linea, prdescuento, prdescuentomax, plazo, plazomax, "
                            +"monto, montomax, identificador, opcion "
                            +"FROM DescuentoCobranza "
                            +"ORDER BY linea, prdescuento, prdescuentomax;");

            if (array.size()!=0) {
                for (Object descuento : array) {
                    DescuentoCobranzaTO descuentoCobranzaTO = (DescuentoCobranzaTO)descuento;

                    if (facturaCobranzaTO.Documento.length()==0)
                        continue;

                    double monto =
                            descuentoCobranzaTO.identificador.compareTo("0")==0 && facturaCobranzaTO.PLAZO < 6 && /*clienteDAO.mncod*/ 0 != 0 ? 0 /*clienteDAO.mncod*/:
                                    descuentoCobranzaTO.identificador.compareTo("0")==0 ? facturaCobranzaTO.MontoOrginal :
                                            descuentoCobranzaTO.identificador.compareTo("1")==0 ? /*clienteDAO.mnventamesant*/ 0 :
                                                    descuentoCobranzaTO.identificador.compareTo("2")==0 && clienteDAO.tipo.compareTo("09")==0 ? clienteDAO.mnventa : 0.0;

                    int plazo = descuentoCobranzaTO.plazo;
                    int plazomax = descuentoCobranzaTO.plazomax;

                    if (descuentoCobranzaTO.linea.compareTo("0")!=0)
                        plazo = plazomax = 0;

                    if (Numero.between(diasTranscurridos, plazo, plazomax)
                            && Numero.between(monto, descuentoCobranzaTO.monto, descuentoCobranzaTO.montomax))      {

                        if (descuentoCobranzaTO.opcion.compareTo("2")==0) {
                            //Message.alert(this, "'"+descuentoCobranzaTO.identificador+"' '"+descuentoCobranzaTO.linea+"'\n "
                            //      +facturaCobranzaTO.diastranscurridos+" "+plazo+" "+plazomax+"\n "
                            //      +monto+" "+descuentoCobranzaTO.monto+" "+descuentoCobranzaTO.montomax
                            //      );

                            ret.add(descuentoCobranzaTO);
                        }
                    }
                }
            }

            if (ret.size()==0) {
                DescuentoCobranzaTO descuentoCobranzaTO = new DescuentoCobranzaTO();
                descuentoCobranzaTO.identificador = "X";
                ret.add(descuentoCobranzaTO);
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
        return ret;
    }

    private void cargaDocumentos() {
        sagajiWebService.waitMessage(this, "Obtengo los Documentos del Cliente ...");

        FacturasCobranzaTask facturasCobranzaTask = new FacturasCobranzaTask(sagajiWebService, this,
                new String[] {sagajiWebService.getAccess(), clienteDAO.cliente});
        facturasCobranzaTask.execute();
    }

    public void tengoFacturasCobranza(ArrayList<FacturaCobranzaTO> facturas) {
        if (facturas.size()!=0) {
            for (FacturaCobranzaTO facturaCobranzaTO : facturas) {
                documentosAdapter.add(facturaCobranzaTO);
            }
        }
    }

    private void capturaReferencia() {
        int tipopago = spinnerPago.getSelectedItemPosition() + 1;
        if (tipopago==2||tipopago==3||tipopago==4||tipopago==5) {

            Bundle bundle = new Bundle();
            bundle.putString("referencia", clienteDAO.referencia);

            Intent intent = new Intent(this, CobranzaReferenciaActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, Constantes.ACTIVITY_REFERENCIA);
        } else {
            cobranzaReferenciaTO = null;
        }
    }

    private void verDescuentos() {
        descuentosAdapter.clear();

        int index = 0;
        for(index = 0; index < detCobranzaAdapter.getCount(); index++) {
            CobranzaDetalleTO detCobranzaTO = detCobranzaAdapter.getItem(index);

            if (detCobranzaTO.documento.compareTo(facturaCobranzaTO.Documento)==0) {
                lstDetalles.setSelection(index);
                lstDetalles.requestFocus();

                Message.alert(this, "Ya tiene capturado el documento ["+facturaCobranzaTO.Documento+"].");

                return;
            }
        }

        List<DescuentoCobranzaTO> descuentos = cargaDescuentos();
        for (DescuentoCobranzaTO descuentoCobranzaTO : descuentos)
            descuentosAdapter.add(descuentoCobranzaTO);

        edtImporte.setText("0.00");
        edtDescuento.setText("0.00");
        edtPago.setText("0.00");
    }

    private void verDetalleDescuento() {
        if (facturaCobranzaTO==null)
            return;
        if (descuentoCobranzaTO==null)
            return;

        double pago = Numero.redondea(facturaCobranzaTO.Saldo * (1.0 - descuentoCobranzaTO.prdescuentomax));
        double importe = facturaCobranzaTO.Saldo;

        edtImporte.setText(String.valueOf(Numero.redondea(importe)));
        edtDescuento.setText(String.valueOf(Numero.redondea(descuentoCobranzaTO.prdescuentomax * 100.0)));
        edtPago.setText(String.valueOf(Numero.redondea(pago)));

        captura = R.id.edtImporte;

        if (captura==R.id.edtImporte) {
            edtImporte.selectAll();
            edtImporte.requestFocus();
        }

        if (captura==R.id.edtPago) {
            edtPago.selectAll();
            edtPago.requestFocus();
        }

        captura = 0;
    }

    private void enterOnImporte(String s) {
        if (facturaCobranzaTO == null)
            return;

        if (captura==0)
            captura = R.id.edtImporte;

        String tipopago = String.valueOf(spinnerPago.getSelectedItemPosition() + 1);
        double pago = Numero.getDoubleFromString(edtPago.getText().toString());
        double importe = Numero.getDoubleFromString(s);
        double prdescuento = Numero.redondea(Numero.getDoubleFromString(edtDescuento.getText().toString()) / 100.0, 4);

        if (importe > facturaCobranzaTO.Saldo) {
            edtImporte.setText(String.valueOf(Numero.redondea(facturaCobranzaTO.Saldo)));

            Message.alert(this, "No se permite capturar un Importe Mayor al saldo ["+Numero.getMoneda(facturaCobranzaTO.Saldo)+"] de la factura.");

            if (captura==R.id.edtImporte) {
                edtImporte.selectAll();
                edtImporte.requestFocus();
            }

            if (captura==R.id.edtPago) {
                edtPago.selectAll();
                edtPago.requestFocus();
            }

            return;
        }

        if (captura==R.id.edtImporte) {
            pago = Numero.redondea(importe * (1.0 - prdescuento));

            edtPago.setText(String.valueOf(Numero.redondea(pago)));

            edtDescuento.selectAll();
            edtDescuento.requestFocus();
        }

        if (captura==R.id.edtPago) {
            agregaDocumento(tipopago, pago, importe, prdescuento);
        }
    }

    private void enterOnDescuento(String s) {
        if (facturaCobranzaTO == null)
            return;
        if (descuentoCobranzaTO==null)
            return;

        double descuento = Numero.redondea(Numero.getDoubleFromString(s) / 100.0, 4);

        if (descuento > Numero.redondea(descuentoCobranzaTO.prdescuentomax, 4)) {
            edtDescuento.setText(String.valueOf(Numero.redondea(descuentoCobranzaTO.prdescuentomax * 100.0)));

            Message.alert(this, "No se permite capturar un Descuento Mayor al "+Numero.getPorcentaje(descuentoCobranzaTO.prdescuentomax));

            edtDescuento.selectAll();
            edtDescuento.requestFocus();

            return;
        }

        double pago = Numero.getDoubleFromString(edtPago.getText().toString());
        double importe = Numero.getDoubleFromString(edtImporte.getText().toString());

        if (captura==R.id.edtImporte) {
            pago = Numero.redondea(importe * Numero.redondea(1.0 - descuento, 4));

            edtPago.setText(String.valueOf(Numero.redondea(pago)));

            edtPago.selectAll();
            edtPago.requestFocus();
        }

        if (captura==R.id.edtPago) {
            importe = Numero.redondea(pago / Numero.redondea(1.0 - descuento, 4));

            edtImporte.setText(String.valueOf(Numero.redondea(importe)));

            edtImporte.selectAll();
            edtImporte.requestFocus();
        }
    }

    private void enterOnPago(String s) {
        if (facturaCobranzaTO == null)
            return;

        if (captura==0)
            captura = R.id.edtPago;

        String tipopago = String.valueOf(spinnerPago.getSelectedItemPosition() + 1);
        double pago = Numero.getDoubleFromString(s);
        double importe = Numero.getDoubleFromString(edtImporte.getText().toString());
        double prdescuento = Numero.redondea(Numero.getDoubleFromString(edtDescuento.getText().toString()) / 100.0, 4);

        if (pago > importe)     {
            Message.alert(this, "El pago no puede ser mayor al monto del importe "+Numero.getMoneda(importe));

            edtPago.selectAll();
            edtPago.requestFocus();
            return;
        }

        if (captura==R.id.edtImporte) {
            agregaDocumento(tipopago, pago, importe, prdescuento);
        }

        if (captura==R.id.edtPago) {
            importe = Numero.redondea(pago / Numero.redondea(1.0 - prdescuento, 4));

            edtImporte.setText(String.valueOf(Numero.redondea(importe)));

            edtDescuento.selectAll();
            edtDescuento.requestFocus();
        }
    }

    private void agregaDocumento(String tipopago, double pago, double importe, double prdescuento) {
        if (descuentoCobranzaTO==null)
            return;
        if (facturaCobranzaTO==null)
            return;

        if (filial==null) {
            filial = configuracionTO.filial; //facturaCobranzaTO.filial;
        }
        if (filial.compareTo(configuracionTO.filial/*facturaCobranzaTO.filial*/)!=0) {
            Message.alert(this, "No se permite capturar Documentos de diferentes filiales, la filial capturada es ["+filial
                    +"] y el documento ["+facturaCobranzaTO.Documento+"] tiene la filial ["+configuracionTO.filial+"].");

            if (captura==R.id.edtImporte) {
                edtImporte.selectAll();
                edtImporte.requestFocus();
            }

            if (captura==R.id.edtPago) {
                edtPago.selectAll();
                edtPago.requestFocus();
            }

            return;
        }

        if (importe > facturaCobranzaTO.Saldo) {
            edtImporte.setText(String.valueOf(Numero.redondea(facturaCobranzaTO.Saldo)));

            Message.alert(this, "No se permite capturar un Importe Mayor al saldo ["+Numero.getMoneda(facturaCobranzaTO.Saldo)+"] de la factura.");

            if (captura==R.id.edtImporte) {
                edtImporte.selectAll();
                edtImporte.requestFocus();
            }

            if (captura==R.id.edtPago) {
                edtPago.selectAll();
                edtPago.requestFocus();
            }

            return;
        }

        if (prdescuento > Numero.redondea(descuentoCobranzaTO.prdescuentomax, 4)) {
            edtDescuento.setText(String.valueOf(Numero.redondea(descuentoCobranzaTO.prdescuentomax * 100.0)));

            Message.alert(this, "No se permite capturar un Descuento Mayor al "+Numero.getPorcentaje(descuentoCobranzaTO.prdescuentomax));

            edtDescuento.selectAll();
            edtDescuento.requestFocus();

            return;
        }

        //
        // Se recalcula el Importe de la Cobranza
        //
        pago = Numero.getDoubleFromString(edtPago.getText().toString());

        importe = Numero.redondea(pago / Numero.redondea(1.0 - prdescuento, 4));
        edtImporte.setText(String.valueOf(Numero.redondea(importe)));
        //
        //
        //

        CobranzaDetalleTO detCobranzaTO = new CobranzaDetalleTO();
        detCobranzaTO.documento = facturaCobranzaTO.Documento;
        detCobranzaTO.tipopago = tipopago;
        detCobranzaTO.pago = pago;
        detCobranzaTO.importe = importe;
        detCobranzaTO.prdescuento = prdescuento;
        detCobranzaTO.descuento = Numero.redondea(importe * detCobranzaTO.prdescuento);

        if (cobranzaReferenciaTO!=null) {
            detCobranzaTO.banco = cobranzaReferenciaTO.banco;
            detCobranzaTO.referencia = cobranzaReferenciaTO.referencia;
            detCobranzaTO.fechacobro = cobranzaReferenciaTO.fechacobro;
        }

        detCobranzaAdapter.insert(detCobranzaTO, 0);

        totTotal += detCobranzaTO.pago;

        despliegaTotales();

        limpiaCapturaDocumento();
    }

    private void limpiaCapturaDocumento() {
        edtImporte.setText("");
        edtDescuento.setText("");
        edtPago.setText("");

        facturaCobranzaTO = null;
        descuentoCobranzaTO = null;
        detCobranzaTO = null;
    }

    private void limpiarDetalles(boolean confirma) {
        detCobranzaAdapter.clear();

        facturaCobranzaTO = null;
        descuentoCobranzaTO = null;
        detCobranzaTO = null;

        totTotal = 0.0;

        despliegaTotales();
    }

    private void limpiarDetalles() {
        if (detCobranzaAdapter.getCount() > 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            limpiarDetalles(true);

                        case DialogInterface.BUTTON_NEGATIVE:
                        default:
                            break;
                    }
                    dialog.dismiss();
                }
            };

            Message.question(this, dialogClickListener, "¿Realmente desea limpiar TODOS los detalles de la cobranza?");
        }
    }

    private void borrarDetalle() {
        if (detCobranzaTO==null) {
            Toast.makeText(CobranzaActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        totTotal -= detCobranzaTO.pago;

        despliegaTotales();

        detCobranzaAdapter.remove(detCobranzaTO);

        detCobranzaTO = null;
    }

    private void despliegaTotales() {
        txtTotal.setText(Numero.getMoneda(totTotal));
    }

    private void anteriores() {
        Bundle bundle = new Bundle();
        bundle.putString("daoList", CobranzaDAO.class.getCanonicalName());

        Intent intent = new Intent(this, AnterioresActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_ANTERIORES);
    }

    private void reenviarAnterior(int folio) {
        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        ds.beginTransaction();
        try {
            ds.execute("UPDATE Cobranza SET status = '" + Constantes.ESTADO_TERMINADO + "' WHERE folio = " + folio);

            ds.commit();

            Toast.makeText(CobranzaActivity.this, "Cobranza [" + folio + "] marcada para reenvio.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al borrar la cobranza anterior: "+e.getMessage());
        }
        ds.close();
    }

    private void borrarAnterior(int folio) {
        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        ds.beginTransaction();
        try {
            CobranzaDAO cobranzaDAO = new CobranzaDAO(folio);
            ds.exists(cobranzaDAO);

            if (cobranzaDAO.impresiones > 0)
                throw new Exception("No se puede borrar porque ya se imprimio.");

            /*HtlDAO htlDAO = new HtlDAO();
            htlDAO.folio = null;
            htlDAO.foliocobranza = folio;
            htlDAO.status = Constantes.ESTADO_TERMINADO;
            htlDAO.fechacreacion = new Date();
            htlDAO.fechamodificacion = new Date();
            htlDAO.filial = cobranzaDAO.filial;
            htlDAO.agente = cobranzaDAO.agente;
            htlDAO.cliente = cobranzaDAO.cliente;
            htlDAO.monto = cobranzaDAO.total;
            htlDAO.accion = "Eliminado";

            ds.insert(htlDAO);*/

            ds.execute("UPDATE Cobranza SET status = '" + Constantes.ESTADO_BORRADO + "' WHERE folio = " + folio);

            List<DatabaseRecord> detalles = ds.select(new DetCobranzaDAO(), "folio = "+cobranzaDAO.getFolio());

            for (DatabaseRecord record : detalles) {
                DetCobranzaDAO detCobranzaDAO = (DetCobranzaDAO)record;

                /*HtldDAO htldDAO = new HtldDAO();
                htldDAO.folio = null;
                htldDAO.foliocobranza = cobranzaDAO.folio;
                htldDAO.status = Constantes.ESTADO_TERMINADO;
                htldDAO.fechacreacion = new Date();
                htldDAO.fechamodificacion = new Date();
                htldDAO.filial = cobranzaDAO.filial;
                htldDAO.agente = cobranzaDAO.agente;
                htldDAO.cliente = cobranzaDAO.cliente;
                htldDAO.documento = detCobranzaDAO.documento;
                htldDAO.tipodocumento = detCobranzaDAO.tipodocumento;
                htldDAO.tipopago = detCobranzaDAO.tipopago;
                htldDAO.pago = detCobranzaDAO.pago;
                htldDAO.importe = detCobranzaDAO.importe;
                htldDAO.prdescuento = detCobranzaDAO.prdescuento;
                htldDAO.descuento = detCobranzaDAO.descuento;

                ds.insert(htldDAO);*/
            }

            ds.commit();

            Toast.makeText(CobranzaActivity.this, "Cobranza [" + folio + "] borrada correctamente.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al borrar la cobranza anterior: "+e.getMessage());
        }
        ds.close();
    }

    private void imprimirAnterior(int folio) {
        try {
            DatabaseServices dsOperaciones = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
            dsOperaciones.beginTransaction();

            CobranzaDAO cobranzaDAO = new CobranzaDAO(folio);
            dsOperaciones.exists(cobranzaDAO);

            List<DatabaseRecord> detalles = dsOperaciones.select(new DetCobranzaDAO(), "folio = " + cobranzaDAO.folio);

            try {
                /*HtlDAO htlDAO = new HtlDAO();
                htlDAO.folio = null;
                htlDAO.foliocobranza = folio;
                htlDAO.status = Constantes.ESTADO_TERMINADO;
                htlDAO.fechacreacion = new Date();
                htlDAO.fechamodificacion = new Date();
                htlDAO.filial = cobranzaDAO.filial;
                htlDAO.agente = cobranzaDAO.agente;
                htlDAO.cliente = cobranzaDAO.cliente;
                htlDAO.monto = cobranzaDAO.total;
                htlDAO.accion = "Impreso";

                dsOperaciones.insert(htlDAO);*/

                cobranzaDAO.impresiones ++;

                dsOperaciones.execute("UPDATE Cobranza SET impresiones = " + cobranzaDAO.impresiones + " WHERE folio = " + folio);

                for (DatabaseRecord record : detalles) {
                    DetCobranzaDAO detCobranzaDAO = (DetCobranzaDAO)record;

                    /*HtldDAO htldDAO = new HtldDAO();
                    htldDAO.folio = null;
                    htldDAO.foliocobranza = cobranzaDAO.folio;
                    htldDAO.status = Constantes.ESTADO_TERMINADO;
                    htldDAO.fechacreacion = new Date();
                    htldDAO.fechamodificacion = new Date();
                    htldDAO.filial = cobranzaDAO.filial;
                    htldDAO.agente = cobranzaDAO.agente;
                    htldDAO.cliente = cobranzaDAO.cliente;
                    htldDAO.documento = detCobranzaDAO.documento;
                    htldDAO.tipodocumento = detCobranzaDAO.tipodocumento;
                    htldDAO.tipopago = detCobranzaDAO.tipopago;
                    htldDAO.pago = detCobranzaDAO.pago;
                    htldDAO.importe = detCobranzaDAO.importe;
                    htldDAO.prdescuento = detCobranzaDAO.prdescuento;
                    htldDAO.descuento = detCobranzaDAO.descuento;

                    dsOperaciones.insert(htldDAO);*/
                }

                dsOperaciones.commit();
            } catch (Exception e) {
                dsOperaciones.rollback();

                Log.e(LOGTAG, e.getMessage(), e);
            }

            CobranzaDepositoDAO cobranzaDepositoDAO = new CobranzaDepositoDAO(cobranzaDAO.folio);
            if (dsOperaciones.exists(cobranzaDepositoDAO)) {

                for(int i=0; i<detalles.size(); i++) {
                    DetCobranzaDAO detCobranzaDAO = (DetCobranzaDAO)detalles.get(i);
                    detCobranzaDAO.tipopago = "98";
                    detCobranzaDAO.banco = cobranzaDepositoDAO.banco;
                    detCobranzaDAO.referencia = cobranzaDepositoDAO.referencia;
                    detCobranzaDAO.fechacobro = cobranzaDepositoDAO.fechacobro;

                    detalles.set(i, detCobranzaDAO);
                }
            }

            dsOperaciones.close();

            /*
            PrintingImp printing = new PrintingImp();
            printing.setDriver(Configuracion.getInstance().impresoraDriver);
            printing.setDatabaseServices(ds);
            printing.setRegistros(cobranzaDAO, detalles);

            if (Configuracion.getInstance().imprimir=="")
                Configuracion.getInstance().imprimir = "com.atcloud.android.bluetooth.BluetoothPrint";
            PrintInterface printer = (PrintInterface)Class.forName(Configuracion.getInstance().imprimir).newInstance();

            if (cobranzaDAO.impresiones == 1) {
                printing.setLeyenda("*** Original Cliente ***");
                printer.doPrint(this, Configuracion.getInstance().impresora, printing.print());
            }

            printing.setLeyenda("*** Copia Intermediario ***");
            printer.doPrint(this, Configuracion.getInstance().impresora, printing.print());
            */

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al imprimir la cobranza: "+e.toString());
        }
    }

    private void abrirAnterior(int folio) {
        folioCobranza = folio;

        try {
            DatabaseServices dsOperaciones = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

            CobranzaDAO cobranzaDAO = new CobranzaDAO(folio);
            dsOperaciones.exists(cobranzaDAO);

            List<DatabaseRecord> detalles = dsOperaciones.select(new DetCobranzaDAO(), "folio = " + folioCobranza);

            dsOperaciones.close();

            if (cobranzaDAO.cliente.compareTo(clienteDAO.cliente)!=0) {
                folioCobranza = 0;

                throw new Exception("Solo se puede abrir una cobranza anterior cuando coincide el "
                        +"Cliente seleccionado ["+clienteDAO.cliente+"] con el Cliente de la Cobranza ["+cobranzaDAO.cliente+"] "
                        +"que desea abrir.");
            }

            filial = cobranzaDAO.filial;
            clienteDAO = new ClienteDAO(cobranzaDAO.cliente);
            ds.exists(clienteDAO);

            for (DatabaseRecord record : detalles) {
                DetCobranzaDAO detCobranzaDAO = (DetCobranzaDAO)record;

                facturaCobranzaTO = new FacturaCobranzaTO();
                facturaCobranzaTO.Documento = detCobranzaDAO.documento;
                facturaCobranzaTO.Saldo = detCobranzaDAO.importe;

                if (detCobranzaDAO.tipopago.compareTo("2")==0
                        ||detCobranzaDAO.tipopago.compareTo("3")==0
                        ||detCobranzaDAO.tipopago.compareTo("4")==0) {

                    cobranzaReferenciaTO = new CobranzaReferenciaTO();
                    cobranzaReferenciaTO.banco = detCobranzaDAO.banco;
                    cobranzaReferenciaTO.referencia = detCobranzaDAO.referencia;
                    cobranzaReferenciaTO.fechacobro = detCobranzaDAO.fechacobro;
                } else {
                    cobranzaReferenciaTO = null;
                }

                descuentoCobranzaTO = new DescuentoCobranzaTO();
                descuentoCobranzaTO.prdescuento = detCobranzaDAO.prdescuento;
                descuentoCobranzaTO.prdescuentomax = detCobranzaDAO.prdescuento;

                agregaDocumento(detCobranzaDAO.tipopago, detCobranzaDAO.pago, detCobranzaDAO.importe, detCobranzaDAO.prdescuento);
            }

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al abrir la cobranza anterior: "+e.getMessage());
        }
    }

    private void calculadora() {
        ArrayList<HashMap<String,Object>> items =new ArrayList<HashMap<String,Object>>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packs = pm.getInstalledPackages(0);
        for (PackageInfo pi : packs) {
            if( pi.packageName.toString().toLowerCase().contains("calcul")){
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("appName", pi.applicationInfo.loadLabel(pm));
                map.put("packageName", pi.packageName);
                items.add(map);
            }
        }
        if(items.size()>=1) {
            String packageName = (String)items.get(0).get("packageName");
            Intent i = pm.getLaunchIntentForPackage(packageName);
            if (i != null)
                startActivity(i);
        }
        else {
            // Application not found
            Message.alert(this, "Error al abrir la calculadora, no hay aplicaciones disponibles.");
        }
    }
}
