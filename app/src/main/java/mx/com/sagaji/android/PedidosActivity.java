package mx.com.sagaji.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Message;
import com.atcloud.android.util.Numero;
import com.atcloud.android.util.Reflector;
import com.atcloud.android.view.InputManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mx.com.sagaji.android.adapter.BusquedaAdapter;
import mx.com.sagaji.android.adapter.EquivalentesAdapter;
import mx.com.sagaji.android.adapter.PedidosDetalleAdapter;
import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.dao.GenericDAO;
import mx.com.sagaji.android.dao.entity.ClienteDAO;
import mx.com.sagaji.android.dao.entity.DetPedidoDAO;
import mx.com.sagaji.android.dao.entity.ExistenciaDAO;
import mx.com.sagaji.android.dao.entity.PedidoDAO;
import mx.com.sagaji.android.dao.entity.ProductoDAO;
import mx.com.sagaji.android.dao.entity.PromocionDAO;
import mx.com.sagaji.android.to.BusquedaPasarTO;
import mx.com.sagaji.android.to.BusquedaTO;
import mx.com.sagaji.android.to.CategoriaDetalleTO;
import mx.com.sagaji.android.to.ClienteTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.to.EquivalenteTO;
import mx.com.sagaji.android.to.ExistenciaTO;
import mx.com.sagaji.android.to.PedidoDetalleTO;
import mx.com.sagaji.android.to.PedidoGuardarTO;
import mx.com.sagaji.android.util.ActivityUtil;
import mx.com.sagaji.android.util.Constantes;
import mx.com.sagaji.android.ws.ExistenciasTask;
import mx.com.sagaji.android.ws.SagajiWebService;

public class PedidosActivity extends AppCompatActivity {
    public static String LOGTAG = PedidosActivity.class.getCanonicalName();

    private SagajiWebService sagajiWebService;
    private ConfiguracionTO configuracionTO;
    private ClienteTO clienteTO;
    private Date feiniciovisita;
    private DatabaseServices ds = null;
    private EditText edtCodigo = null;
    private EditText edtCantidad = null;
    private EditText edtCantidadModificar = null;
    private AlertDialog modificarDialog = null;
    private CheckBox chkEquivalentes = null;
    private EditText edtDetalles = null;
    private boolean equivalentes = false;
    private ListView lstBusqueda = null;
    private ListView lstDetalles = null;
    private EquivalentesAdapter equivalentesAdapter = null;
    private BusquedaAdapter busquedaAdapter = null;
    private PedidosDetalleAdapter pedidosDetalleAdapter = null;
    private PedidoDetalleTO detPedidoTO = null;
    private ProductoDAO productoDAO = null;
    private HashMap<String,String> almacenesFiliales = null;
    private HashMap<String,String[]> existenciasAlmacenes = null;
    private boolean promocion = false;
    private String[] filialesExistencias = null;
    private String[] almacenes = null;
    private int cantidad = 0;
    private int totTotalCantidadSurtir = 0;
    private double totTotalSurtir;
    private int totTotalCantidad = 0;
    private double totTotal;
    private TextView txtTotalSurtir = null;
    private TextView txtTotalCantidadSurtir = null;
    private TextView txtTotal = null;
    private TextView txtTotalCantidad = null;
    private ClienteDAO clienteDAO = null;
    private int folioPedido = 0;
    private boolean requiereAutorizacion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        setTitle("Pedidos");

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
        getMenuInflater().inflate(R.menu.menu_pedidos, menu);
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
            case R.id.opcModificar:
                modificarDetalle();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pedidos_detalles, menu);
    }

    private void setUpView() {
        txtTotalSurtir = (TextView)findViewById(R.id.txtTotalSurtir);
        txtTotalCantidadSurtir = (TextView)findViewById(R.id.txtTotalCantidadSurtir);
        txtTotal = (TextView)findViewById(R.id.txtTotal);
        txtTotalCantidad = (TextView)findViewById(R.id.txtTotalCantidad);

        edtCodigo = (EditText)findViewById(R.id.edtCodigo);
        edtCodigo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_NONE
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = edtCodigo.getText().toString();
                    enterOnCodigo(text);
                    return true;
                }
                return false;
            }
        });
        edtCodigo.setOnKeyListener(new TextView.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN) {
                    if (keyCode==KeyEvent.KEYCODE_ENTER) {
                        String text = edtCodigo.getText().toString();
                        enterOnCodigo(text);
                        return true;
                    }
                }
                return false;
            }
        });

        edtCantidad = (EditText)findViewById(R.id.edtCantidad);
        edtCantidad.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_NONE
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    enterOnCantidad(edtCantidad.getText().toString());
                    return true;
                }
                return false;
            }
        });
        edtCantidad.setOnKeyListener(new TextView.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        enterOnCantidad(edtCantidad.getText().toString());
                        return true;
                    }
                }
                return false;
            }
        });
        edtCantidad.setEnabled(false);

        edtCantidadModificar = new EditText(this);
        edtCantidadModificar.setInputType(InputType.TYPE_CLASS_NUMBER);

        chkEquivalentes = (CheckBox)findViewById(R.id.chkEquivalentes);
        chkEquivalentes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                if(checked) {
                    equivalentes = true;
                    lstBusqueda.setAdapter(equivalentesAdapter);
                } else {
                    equivalentes = false;
                    lstBusqueda.setAdapter(busquedaAdapter);
                }
            }
        });
        chkEquivalentes.setChecked(false);
        equivalentes = false;

        edtDetalles = (EditText)findViewById(R.id.edtDetalles);

        lstBusqueda = (ListView)findViewById(R.id.lstBusqueda);

        equivalentesAdapter = new EquivalentesAdapter(this, R.layout.layout_pedidos_equivalentes_detalles);
        busquedaAdapter = new BusquedaAdapter(this, R.layout.layout_pedidos_busqueda_detalles);

        lstBusqueda.setAdapter(busquedaAdapter);
        lstBusqueda.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (equivalentes) {
                    EquivalenteTO equivalenteTO = (EquivalenteTO)parent.getAdapter().getItem(position);

                    capturaCodigo();

                    edtCodigo.setText(equivalenteTO.codigo);
                    enterOnCodigo(equivalenteTO.codigo);

                } else {
                    BusquedaTO busquedaTO = (BusquedaTO)parent.getAdapter().getItem(position);

                    edtCodigo.setText(busquedaTO.codigo);
                    enterOnCodigo(busquedaTO.codigo);
                }
            }
        });

        lstDetalles = (ListView)findViewById(R.id.lstDetalles);
        registerForContextMenu(lstDetalles);

        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.layout_pedidos_detalles_header, lstDetalles, false);
        lstDetalles.addHeaderView(header, null, false);

        pedidosDetalleAdapter = new PedidosDetalleAdapter(this, R.layout.layout_pedidos_detalles);
        lstDetalles.setAdapter(pedidosDetalleAdapter);
        lstDetalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                detPedidoTO = (PedidoDetalleTO)parent.getAdapter().getItem(position);

                try {
                    productoDAO = new ProductoDAO(detPedidoTO.codigo);
                    ds.exists(productoDAO);

                    verDetalle();
                } catch(Exception e) {
                    Log.e(LOGTAG, e.getMessage(), e);
                    Message.alert(PedidosActivity.this, "Exception: "+e.getMessage());
                }
            }
        });
        lstDetalles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                detPedidoTO = (PedidoDetalleTO)parent.getAdapter().getItem(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button btnBorrar = (Button)findViewById(R.id.btnBorrar);
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                borrarDetalle();
            }
        });
        Button btnSalir = (Button)findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //salir();
                previoGuardarPedido(false);
            }
        });
        Button btnAnteriores = (Button)findViewById(R.id.btnAnteriores);
        btnAnteriores.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                anteriores();
            }
        });
        Button btnCategorias = (Button)findViewById(R.id.btnCategorias);
        btnCategorias.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                categorias();
            }
        });
        Button btnBuscar = (Button)findViewById(R.id.btnBuscar);
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                buscar();
            }
        });
        Button btnNuevos = (Button)findViewById(R.id.btnNuevos);
        btnNuevos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                nuevos();
            }
        });

        despliegaTotales();

        almacenesFiliales = new HashMap<String,String>();
        almacenesFiliales.put("Mexico", "01");
        almacenesFiliales.put("Leon"  , "02");
        almacenesFiliales.put("Oaxaca", "04");
        almacenesFiliales.put("Puebla", "05");
        almacenesFiliales.put("Tuxtla", "08");

        existenciasAlmacenes = new HashMap<String,String[]>();
        existenciasAlmacenes.put("01", new String[]{"Mexico","Leon","Oaxaca","Puebla","Tuxtla"});
        existenciasAlmacenes.put("02", new String[]{"Mexico","Leon","Oaxaca","Puebla","Tuxtla"});
        existenciasAlmacenes.put("03", new String[]{"Mexico","Leon","Oaxaca","Puebla","Tuxtla"});
        existenciasAlmacenes.put("04", new String[]{"Mexico","Leon","Oaxaca","Puebla","Tuxtla"});
        existenciasAlmacenes.put("05", new String[]{"Mexico","Leon","Oaxaca","Puebla","Tuxtla"});
        existenciasAlmacenes.put("08", new String[]{"Mexico","Leon","Oaxaca","Puebla","Tuxtla"});

        almacenes = existenciasAlmacenes.get(configuracionTO.filial);

        TextView lblAlmacenA = (TextView)findViewById(R.id.lblAlmacenA);
        lblAlmacenA.setText(almacenes[0]);
        TextView lblAlmacenB = (TextView)findViewById(R.id.lblAlmacenB);
        lblAlmacenB.setText(almacenes[1]);
        TextView lblAlmacenC = (TextView)findViewById(R.id.lblAlmacenC);
        lblAlmacenC.setText(almacenes[2]);
        TextView lblAlmacenD = (TextView)findViewById(R.id.lblAlmacenD);
        lblAlmacenD.setText(almacenes[3]);
        TextView lblAlmacenE = (TextView)findViewById(R.id.lblAlmacenE);
        lblAlmacenE.setText(almacenes[4]);

        filialesExistencias = new String[almacenes.length];
        for(int i=0; i<filialesExistencias.length; i++)
            filialesExistencias[i] = almacenesFiliales.get(almacenes[i]);

        ds = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();

        clienteDAO = new ClienteDAO(clienteTO.cliente);
        try {
            ds.exists(clienteDAO);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constantes.ACTIVITY_BUSQUEDA) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if(extras !=null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<BusquedaPasarTO> registros = (ArrayList<BusquedaPasarTO>)extras.getSerializable("registros");

                    for(BusquedaPasarTO registro : registros) {
                        edtCodigo.setText(registro.codigo);
                        enterOnCodigo(registro.codigo);

                        edtCantidad.setText(String.valueOf(registro.cantidad));
                        enterOnCantidad(String.valueOf(registro.cantidad));
                    }

                    capturaCodigo();
                }
            }
        }
        else if (requestCode == Constantes.ACTIVITY_CATEGORIAS) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if(extras !=null) {
                    CategoriaDetalleTO categoriaDetalleTO = (CategoriaDetalleTO)extras.getSerializable("registro");

                    edtCodigo.setText(categoriaDetalleTO.codigo);
                    enterOnCodigo(categoriaDetalleTO.codigo);

                    edtCantidad.setText("1");
                    enterOnCantidad("1");

                    capturaCodigo();
                }
            }
        }
        else if (requestCode == Constantes.ACTIVITY_ANTERIORES) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if(extras !=null) {

                    String accion = extras.getString("accion");

                    if (accion.equals(Constantes.ACCION_ABRIR)) {
                        if (pedidosDetalleAdapter.getCount()>0) {
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
        else if (requestCode == Constantes.ACTIVITY_PEDIDOS_GUARDAR) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if (extras!=null) {

                    PedidoGuardarTO pedidoGuardarTO = (PedidoGuardarTO)extras.getSerializable("pedido-guardar");

                    guardarPedido(pedidoGuardarTO);

                    Intent intent = new Intent();
                    intent.putExtra("folioPedido", folioPedido);

                    setResult(RESULT_OK, intent);

                    if (pedidoGuardarTO.salir)
                        finish();
                }
            }
        }
    }

    private void salir() {
        if (pedidosDetalleAdapter.getCount() > 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            previoGuardarPedido(true);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                        default:
                            finish();
                            break;
                    }
                    dialog.dismiss();
                }
            };

            Message.question(this, dialogClickListener, "¿Desea guardar el pedido?");

        } else {
            finish();
        }
    }

    private void previoGuardarPedido(final boolean salir) {
        requiereAutorizacion = totTotal > clienteDAO.mnsaldo;

        if (requiereAutorizacion) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    guardarPedido(salir);
                }
            };

            Message.alert(this, dialogClickListener, "El valor del pedido "+Numero.getMoneda(totTotal)+
                    " sobrepasa el valor del saldo del Cliente "+Numero.getMoneda(clienteDAO.mnsaldo)+
                    ", el pedido sera transmitido como 'Pendiente por Autorizacion'.");
        }
        else {
            guardarPedido(salir);
        }
    }

    private void guardarPedido(boolean salir) {
        InputManager.hide(this, edtCodigo);

        Bundle bundle = new Bundle();
        bundle.putString("formaenvio", clienteDAO.formaenvio);
        bundle.putBoolean("salir", salir);

        Intent intent = new Intent(this, PedidosGuardarActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_PEDIDOS_GUARDAR);
    }

    private void guardarPedido(PedidoGuardarTO pedidoGuardarTO) {
        boolean nuevo = folioPedido == 0;

        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

        if (folioPedido == 0) {
            ds.beginTransaction();
            try {
                folioPedido = GenericDAO.obtenerSiguienteFolio(ds, Constantes.FOLIO_PEDIDO);

                ds.commit();
            } catch (Exception e) {
                ds.rollback();

                Log.e(LOGTAG, e.getMessage(), e);
                Message.alert(this, "Error al obtener el folio del pedido: " + e.getMessage());
            }
        }

        ds.beginTransaction();

        try {
            PedidoDAO pedidoDAO = new PedidoDAO();
            pedidoDAO.folio = folioPedido;
            pedidoDAO.status = Constantes.ESTADO_TERMINADO;
            pedidoDAO.fechainicio = feiniciovisita;
            pedidoDAO.fechacreacion = new Date();
            pedidoDAO.fechamodificacion = new Date();
            pedidoDAO.filial = configuracionTO.filial; //filialOperaciones;
            pedidoDAO.intermediario = configuracionTO.intermediario;
            pedidoDAO.cliente = clienteTO.cliente;
            pedidoDAO.nombre = clienteDAO.razonsocial;
            pedidoDAO.partidas = pedidosDetalleAdapter.getCount();
            pedidoDAO.cantidad = totTotalCantidad;
            pedidoDAO.importe = 0;
            pedidoDAO.importeiva = 0;
            pedidoDAO.iva = 0;
            pedidoDAO.total = totTotal;
            pedidoDAO.respuesta = "";
            pedidoDAO.impresiones = 0;
            pedidoDAO.fechaentrega = new Date();
            pedidoDAO.tipo = pedidoGuardarTO.tipo;
            pedidoDAO.claveenvio = pedidoGuardarTO.claveenvio;
            pedidoDAO.observaciones = pedidoGuardarTO.observaciones;
            pedidoDAO.autorizacion = requiereAutorizacion ? 1 : 0;

            if (nuevo) {
                ds.insert(pedidoDAO);
            } else {
                PedidoDAO pedidoAnteriorDAO = new PedidoDAO(folioPedido);
                ds.exists(pedidoAnteriorDAO);

                pedidoDAO.fechacreacion = pedidoAnteriorDAO.fechacreacion;
                pedidoDAO.impresiones = pedidoAnteriorDAO.impresiones;
                pedidoDAO.respuesta = pedidoAnteriorDAO.respuesta;

                ds.update(pedidoDAO);
                ds.execute("DELETE FROM DetPedido WHERE folio = " + folioPedido);
            }

            int index = 0;
            for(index = 0; index < pedidoDAO.partidas; index++) {
                PedidoDetalleTO detPedidoTO = pedidosDetalleAdapter.getItem(index);

                DetPedidoDAO detPedidoDAO = new DetPedidoDAO(folioPedido, detPedidoTO.codigo);
                detPedidoDAO.linea = detPedidoTO.linea;
                detPedidoDAO.orden = index;
                detPedidoDAO.cantidad = detPedidoTO.cantidad;
                detPedidoDAO.precio = detPedidoTO.precio;
                detPedidoDAO.precioiva = detPedidoTO.precioiva;
                detPedidoDAO.importe = detPedidoTO.importe;
                detPedidoDAO.importeiva = detPedidoTO.importeiva;
                detPedidoDAO.priva = detPedidoTO.priva;
                detPedidoDAO.iva = detPedidoTO.iva;
                detPedidoDAO.total = detPedidoTO.total;

                if (ds.exists(detPedidoDAO)) {
                    detPedidoDAO.cantidad += detPedidoTO.cantidad;
                    detPedidoDAO.importe += detPedidoTO.importe;
                    detPedidoDAO.importeiva += detPedidoTO.importeiva;
                    detPedidoDAO.iva += detPedidoTO.iva;
                    detPedidoDAO.total += detPedidoTO.total;

                    ds.update(detPedidoDAO);
                } else {
                    ds.insert(detPedidoDAO);
                }
            }

            ds.commit();
        } catch(Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al guardar el pedido: "+e.getMessage());
        }

        ds.close();

        Toast.makeText(PedidosActivity.this, "Pedido ["+folioPedido+"] guardado correctamente.", Toast.LENGTH_SHORT).show();
    }

    private void enterOnCodigo(String codigo) {
        try {
            if (codigo.length()==0)
                return;

            codigo = codigo.toUpperCase();
            edtCodigo.setText(codigo);

            productoDAO = new ProductoDAO(codigo);

            boolean existe = ds.exists(productoDAO);

            if (!existe && !equivalentes) {
                busquedaProducto(codigo);
                return;
            }

            if (existe) {
                if (equivalentes)
                    equivalentesProducto(codigo);
                //else
                //      busquedaAdapter.clear();

                String cantidad = "1";
                int index = 0;
                for(index = 0; index < pedidosDetalleAdapter.getCount(); index++) {
                    PedidoDetalleTO detPedidoTO = pedidosDetalleAdapter.getItem(index);
                    if (detPedidoTO.codigo.compareTo(productoDAO.codigo)==0) {
                        cantidad = String.valueOf(detPedidoTO.cantidad);
                        break;
                    }
                }

                edtCantidad.setEnabled(true);
                edtCantidad.setText(cantidad);
                edtCantidad.selectAll();
                edtCantidad.requestFocus();

                verDetalle();

                muestraExistencias();

            } else {
                edtCodigo.selectAll();

                Toast.makeText(PedidosActivity.this, "Codigo "+codigo+" inexistente.", Toast.LENGTH_SHORT).show();
            }

        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void equivalentesProducto(String codigo) {
        equivalentesAdapter.clear();

        try {
            List<?> array = ds.collection(new EquivalenteTO(),
                    "SELECT e.equivalente AS codigo, e.descripcion, e.precio, e.cantidad, e.lineaproveedor, e.linea "
                            +"FROM Equivalente e "
                            +"WHERE e.codigo = '"+codigo+"' ORDER BY e.descripcion;");

            if (array.size()!=0) {
                for (Object equivalenteTO : array)
                    equivalentesAdapter.add((EquivalenteTO)equivalenteTO);

                lstBusqueda.setSelection(0);

            } else {
                edtCodigo.selectAll();

                //Toast.makeText(PedidosActivity.this, "No se encontraron productos equivalentes con el codigo '"+codigo+"'.", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void busquedaProducto(String buscar) {
        busquedaAdapter.clear();

        try {

            List<?> array = ds.collection(new BusquedaTO(),
                    "SELECT p.codigo, p.descripcion, p.unidadmedida, p.precio, p.linea "
                            +"FROM Producto p "
                            +"WHERE p.descripcion LIKE '"+buscar+"%' ORDER BY p.descripcion;");

            if (array.size()==0) {
                array = ds.collection(new BusquedaTO(),
                        "SELECT p.codigo, p.descripcion, p.unidadmedida, p.precio, p.linea "
                                +"FROM Producto p "
                                +"WHERE p.codigo LIKE '"+buscar+"%' ORDER BY p.codigo;");
            }

            if (array.size()!=0) {
                for (Object busquedaTO : array)
                    busquedaAdapter.add((BusquedaTO)busquedaTO);

                lstBusqueda.setSelection(0);

            } else {
                edtCodigo.selectAll();

                Toast.makeText(PedidosActivity.this, "No se encontraron productos con la descripcion o codigo '"+buscar+"'.", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void muestraExistencias() {
        TextView edtAlmacenA = (TextView)findViewById(R.id.edtAlmacenA);
        edtAlmacenA.setText("");
        TextView edtAlmacenB = (TextView)findViewById(R.id.edtAlmacenB);
        edtAlmacenB.setText("");
        TextView edtAlmacenC = (TextView)findViewById(R.id.edtAlmacenC);
        edtAlmacenC.setText("");
        TextView edtAlmacenD = (TextView)findViewById(R.id.edtAlmacenD);
        edtAlmacenD.setText("");
        TextView edtAlmacenE = (TextView)findViewById(R.id.edtAlmacenE);
        edtAlmacenE.setText("");

        try {
            List<DatabaseRecord> array = ds.select(new ExistenciaDAO(), "codigo = '"+productoDAO.codigo+"'");

            for (DatabaseRecord record : array) {
                ExistenciaDAO existenciaDAO = (ExistenciaDAO)record;

                if (existenciaDAO.filial.compareTo(filialesExistencias[0])==0)
                    edtAlmacenA.setText(Numero.getIntNumero(existenciaDAO.existencia));
                else if (existenciaDAO.filial.compareTo(filialesExistencias[1])==0)
                    edtAlmacenB.setText(Numero.getIntNumero(existenciaDAO.existencia));
                else if (existenciaDAO.filial.compareTo(filialesExistencias[2])==0)
                    edtAlmacenC.setText(Numero.getIntNumero(existenciaDAO.existencia));
                else if (existenciaDAO.filial.compareTo(filialesExistencias[3])==0)
                    edtAlmacenD.setText(Numero.getIntNumero(existenciaDAO.existencia));
                else if (existenciaDAO.filial.compareTo(filialesExistencias[4])==0)
                    edtAlmacenE.setText(Numero.getIntNumero(existenciaDAO.existencia));
            }

            muestraExistenciasWS(productoDAO.codigo);

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
    }

    private void muestraExistenciasWS(String codigo) {
        //sagajiWebService.waitMessage(this, "Obtengo los Existencias del Producto ...");

        ExistenciasTask existenciasTask = new ExistenciasTask(sagajiWebService, this,
                new String[] {sagajiWebService.getAccess(), codigo});
        existenciasTask.execute();
    }

    public void tengoExistencias(ArrayList<ExistenciaTO> existencias) {
        if (existencias!=null&&!existencias.isEmpty()) {
            ExistenciaTO existenciaTO = existencias.get(0);
            Log.d(LOGTAG, Reflector.toStringAllFields(existenciaTO));

            TextView text;
            for(int index=0; index<filialesExistencias.length; index++) {
                String filial = filialesExistencias[index];
                switch (index) {
                    default:
                    case 0:
                        text = (TextView)findViewById(R.id.edtAlmacenA);
                        break;
                    case 1:
                        text = (TextView)findViewById(R.id.edtAlmacenB);
                        break;
                    case 2:
                        text = (TextView)findViewById(R.id.edtAlmacenC);
                        break;
                    case 3:
                        text = (TextView)findViewById(R.id.edtAlmacenD);
                        break;
                    case 4:
                        text = (TextView)findViewById(R.id.edtAlmacenE);
                        break;
                }
                text.setText("");

                if (filial.compareTo("01")==0) {
                    text.setText(Numero.getIntNumero((int)existenciaTO.MEXICO));
                }
                else if (filial.compareTo("02")==0) {
                    text.setText(Numero.getIntNumero((int)existenciaTO.LEON));
                }
                else if (filial.compareTo("04")==0) {
                    text.setText(Numero.getIntNumero((int)existenciaTO.OAXACA));
                }
                else if (filial.compareTo("05")==0) {
                    text.setText(Numero.getIntNumero((int)existenciaTO.PUEBLA));
                }
                else if (filial.compareTo("08")==0) {
                    text.setText(Numero.getIntNumero((int)existenciaTO.TUXTLA));
                }
            }

            try {
                verDetalle(existenciaTO);
            } catch (Exception e) {
                Log.e(LOGTAG, e.getMessage(), e);
            }
        }
    }

    private void verDetalle() throws Exception {
        ExistenciaDAO existenciaDAO = new ExistenciaDAO(configuracionTO.filial, productoDAO.codigo);
        ds.exists(existenciaDAO);

        StringBuilder detalle = new StringBuilder();
        detalle
                .append(productoDAO.codigo).append(" ")
                .append(productoDAO.descripcion)
                .append(" ").append(Numero.getMoneda(productoDAO.precio))
                .append(" U.Med.:").append(productoDAO.unidadmedida)
                .append(" Exist:").append(Numero.getIntNumero(existenciaDAO.existencia))
        ;

        PromocionDAO promocionDAO = (PromocionDAO)ds.first(new PromocionDAO(), "codigo = '"+productoDAO.codigo+"'");
        promocion = promocionDAO!=null;
        if (promocion) {
            detalle.append(" Oferta:");
            if (promocionDAO.tipo.compareTo("D")==0) {
                detalle
                        .append(Numero.getPorcentaje(promocionDAO.descuento / 100.0))
                        .append(" desde ").append(promocionDAO.escala)
                        .append(" ").append(Numero.getMoneda(promocionDAO.preciooferta))
                ;
            } else {
                detalle
                        .append(" ").append(promocionDAO.escala)
                        .append(" ").append(Numero.getMoneda(promocionDAO.preciooferta))
                ;
            }
        }

        edtDetalles.setText(detalle.toString());
    }

    private void verDetalle(ExistenciaTO existenciaTO) throws Exception {
        int existencia = 0;
        if (configuracionTO.filial.compareTo("01")==0) {
            existencia = (int)existenciaTO.MEXICO;
        }
        else if (configuracionTO.filial.compareTo("02")==0) {
            existencia = (int)existenciaTO.LEON;
        }
        else if (configuracionTO.filial.compareTo("03")==0) {
            //existencia = (int)existenciaTO.MONTERREY;
        }
        else if (configuracionTO.filial.compareTo("04")==0) {
            existencia = (int)existenciaTO.OAXACA;
        }
        else if (configuracionTO.filial.compareTo("05")==0) {
            existencia = (int)existenciaTO.PUEBLA;
        }
        else if (configuracionTO.filial.compareTo("08")==0) {
            existencia = (int)existenciaTO.TUXTLA;
        }

        StringBuilder detalle = new StringBuilder();
        detalle
                .append(existenciaTO.CODIGO).append(" ")
                .append(existenciaTO.DESCRIPCION)
                .append(" ").append(Numero.getMoneda(existenciaTO.PRECIO))
                .append(" U.Med.:").append(existenciaTO.UNIDAD)
                .append(" Exist:").append(Numero.getIntNumero(existencia))
        ;

        PromocionDAO promocionDAO = (PromocionDAO)ds.first(new PromocionDAO(), "codigo = '"+existenciaTO.CODIGO+"'");
        promocion = promocionDAO!=null;
        if (promocion) {
            detalle.append(" Oferta:");
            if (promocionDAO.tipo.compareTo("D")==0) {
                detalle
                        .append(Numero.getPorcentaje(promocionDAO.descuento / 100.0))
                        .append(" desde ").append(promocionDAO.escala)
                        .append(" ").append(Numero.getMoneda(promocionDAO.preciooferta))
                ;
            } else {
                detalle
                        .append(" ").append(promocionDAO.escala)
                        .append(" ").append(Numero.getMoneda(promocionDAO.preciooferta))
                ;
            }
        }

        edtDetalles.setText(detalle.toString());
    }

    private void capturaCodigo() {
        edtCantidad.setText("");
        edtCantidad.setEnabled(false);

        productoDAO = null;

        edtCodigo.setText("");
        edtCodigo.requestFocus();

        edtDetalles.setText("");

        InputManager.show(this);
    }

    private void enterOnCantidad(String scantidad) {
        if(productoDAO==null) {
            edtCodigo.selectAll();
            edtCodigo.requestFocus();
            return;
        }

        cantidad = Numero.getIntFromString(edtCantidad.getText().toString());
        if(cantidad==0) {
            edtCantidad.selectAll();
            edtCantidad.requestFocus();
            return;
        }

        addProductoToGrid();
    }

    private void addProductoToGrid() {
        if (cantidad > 20) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            addProductoToGrid(true);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                        default:
                            edtCantidad.selectAll();
                            edtCantidad.requestFocus();
                            break;
                    }
                    dialog.dismiss();
                }
            };

            Message.question(this, dialogClickListener, "¿Realmente desea capturar "+Numero.getIntNumero(cantidad)+" piezas del producto?");
        }
        else {
            addProductoToGrid(true);
        }
    }

    private void addProductoToGrid(boolean agregar) {
        int index = 0;
        for(index = 0; index < pedidosDetalleAdapter.getCount(); index++) {
            PedidoDetalleTO detPedidoTO = pedidosDetalleAdapter.getItem(index);
            if (detPedidoTO.codigo.compareTo(productoDAO.codigo)==0) {
                Toast.makeText(PedidosActivity.this, "Ya tiene el producto ["+detPedidoTO.codigo+"] agregado en el pedido.", Toast.LENGTH_LONG).show();

                capturaCodigo();

                lstDetalles.smoothScrollToPosition(index);

                this.detPedidoTO = pedidosDetalleAdapter.getItem(index);
                modificarDetalle();

                return;
            }
        }

        PedidoDetalleTO detPedidoTO = new PedidoDetalleTO();
        detPedidoTO.codigo = productoDAO.codigo;
        detPedidoTO.descripcion = productoDAO.descripcion;
        detPedidoTO.unidadmedida = productoDAO.unidadmedida;
        detPedidoTO.linea = productoDAO.linea;
        detPedidoTO.lentomovimiento = ""; //productoDAO.lentomovimiento;
        detPedidoTO.status = ""; //productoDAO.status;
        detPedidoTO.promocion = promocion;
        detPedidoTO.cantidadsurtir = productoDAO.existencia > cantidad ? cantidad : productoDAO.existencia;
        detPedidoTO.cantidad = cantidad;
        detPedidoTO.priva = 0.16;
        detPedidoTO.precio = productoDAO.precio;
        detPedidoTO.calcula();

        pedidosDetalleAdapter.insert(detPedidoTO, 0);

        totTotalCantidadSurtir += detPedidoTO.cantidadsurtir;
        totTotalSurtir += detPedidoTO.totalsurtir;

        totTotalCantidad += detPedidoTO.cantidad;
        totTotal += detPedidoTO.total;

        despliegaTotales();

        capturaCodigo();
    }

    private void limpiarDetalles(boolean confirma) {
        pedidosDetalleAdapter.clear();

        detPedidoTO = null;

        totTotalCantidadSurtir = 0;
        totTotalSurtir = 0.0;

        totTotalCantidad = 0;
        totTotal = 0.0;

        despliegaTotales();
    }

    private void limpiarDetalles() {
        if (pedidosDetalleAdapter.getCount() > 0) {
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

            Message.question(this, dialogClickListener, "¿Realmente desea limpiar TODOS los detalles del pedido?");
        }
    }

    private void modificarDetalle() {
        if (detPedidoTO==null) {
            Toast.makeText(PedidosActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        edtCantidadModificar.setText(String.valueOf(detPedidoTO.cantidad));
        edtCantidadModificar.selectAll();

        if (modificarDialog==null) {
            modificarDialog = new AlertDialog.Builder(this)
                    //.setTitle("SagajiAndroid")
                    .setMessage("Cantidad:")
                    .setView(edtCantidadModificar)
                    .setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            cantidad = Numero.getIntFromString(edtCantidadModificar.getText().toString());

                            modificarDetalle(cantidad);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            capturaCodigo();
                        }
                    })
                    .show();
        }

        modificarDialog.show();
    }

    private void modificarDetalle(int cantidad) {
        if (detPedidoTO==null) {
            Toast.makeText(PedidosActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        if (cantidad <= 0) {
            Toast.makeText(PedidosActivity.this, "Debe de escribir una cantidad mayor a cero.", Toast.LENGTH_LONG).show();
            return;
        }

        totTotalCantidadSurtir -= detPedidoTO.cantidadsurtir;
        totTotalSurtir -= detPedidoTO.totalsurtir;

        totTotalCantidad -= detPedidoTO.cantidad;
        totTotal -= detPedidoTO.total;

        pedidosDetalleAdapter.remove(detPedidoTO);

        despliegaTotales();

        try {
            productoDAO = new ProductoDAO(detPedidoTO.codigo);
            ds.exists(productoDAO);
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }

        detPedidoTO.cantidadsurtir = productoDAO.existencia > cantidad ? cantidad : productoDAO.existencia;
        detPedidoTO.cantidad = cantidad;
        detPedidoTO.calcula();

        pedidosDetalleAdapter.insert(detPedidoTO, 0);
        lstDetalles.smoothScrollToPosition(0);

        totTotalCantidadSurtir += detPedidoTO.cantidadsurtir;
        totTotalSurtir += detPedidoTO.totalsurtir;

        totTotalCantidad += detPedidoTO.cantidad;
        totTotal += detPedidoTO.total;

        despliegaTotales();

        capturaCodigo();

        detPedidoTO = null;
    }

    private void borrarDetalle() {
        if (detPedidoTO==null) {
            Toast.makeText(PedidosActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        totTotalCantidadSurtir -= detPedidoTO.cantidadsurtir;
        totTotalSurtir -= detPedidoTO.totalsurtir;

        totTotalCantidad -= detPedidoTO.cantidad;
        totTotal -= detPedidoTO.total;

        despliegaTotales();

        pedidosDetalleAdapter.remove(detPedidoTO);

        detPedidoTO = null;
    }

    private void despliegaTotales() {
        txtTotalCantidadSurtir.setText(Numero.getIntNumero(totTotalCantidadSurtir));
        txtTotalSurtir.setText(Numero.getMoneda(totTotalSurtir));
        txtTotalCantidad.setText(Numero.getIntNumero(totTotalCantidad));
        txtTotal.setText(Numero.getMoneda(totTotal));
    }

    private void categorias() {
        Bundle bundle = new Bundle();

        Intent intent = new Intent(this, CategoriasActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_CATEGORIAS);
    }

    private void buscar() {
        Bundle bundle = new Bundle();

        Message.alert(this, "ToDo: Implementar");
        //Intent intent = new Intent(this, BusquedaActivity.class);
        //intent.putExtras(bundle);

        //startActivityForResult(intent, Constantes.ACTIVITY_BUSQUEDA);
    }

    private void nuevos() {
        Bundle bundle = new Bundle();

        Message.alert(this, "ToDo: Implementar");
        //Intent intent = new Intent(this, ProductosNuevosActivity.class);
        //intent.putExtras(bundle);

        //startActivityForResult(intent, Constantes.ACTIVITY_PRODUCTOS_NUEVOS);
    }

    private void anteriores() {
        Bundle bundle = new Bundle();
        bundle.putString("daoList", PedidoDAO.class.getCanonicalName());

        Intent intent = new Intent(this, AnterioresActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_ANTERIORES);
    }

    private void reenviarAnterior(int folio) {
        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        ds.beginTransaction();
        try {
            ds.execute("UPDATE Pedido SET status = '" + Constantes.ESTADO_TERMINADO + "' WHERE folio = " + folio);

            ds.commit();

            Toast.makeText(PedidosActivity.this, "Pedido [" + folio + "] marcado para reenvio.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al borrar el pedido anterior: "+e.getMessage());
        }
        ds.close();
    }

    private void borrarAnterior(int folio) {
        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        ds.beginTransaction();
        try {
            ds.execute("UPDATE Pedido SET status = '" + Constantes.ESTADO_BORRADO + "' WHERE folio = " + folio);

            ds.commit();

            Toast.makeText(PedidosActivity.this, "Pedido [" + folio + "] borrado correctamente.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al borrar el pedido anterior: "+e.getMessage());
        }
        ds.close();
    }

    private void imprimirAnterior(int folio) {
        try {
            DatabaseServices dsOperaciones = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
            dsOperaciones.beginTransaction();

            PedidoDAO pedidoDAO = new PedidoDAO(folio);
            dsOperaciones.exists(pedidoDAO);

            pedidoDAO.impresiones ++;
            try {
                dsOperaciones.execute("UPDATE Pedido SET impresiones = " + pedidoDAO.impresiones + " WHERE folio = " + folio);

                dsOperaciones.commit();
            } catch (Exception e) {
                dsOperaciones.rollback();

                Log.e(LOGTAG, e.getMessage(), e);
            }

            List<DatabaseRecord> detalles = dsOperaciones.select(new DetPedidoDAO(), "folio = " + pedidoDAO.folio);

            dsOperaciones.close();

            Message.alert(this, "ToDo: Implementar");
            /*PrioridadEnvioDAO prioridadEnvioDAO = new PrioridadEnvioDAO(pedidoDAO.claveenvio);
            ds.exists(prioridadEnvioDAO);

            pedidoDAO.claveenvio = prioridadEnvioDAO.descripcion;

            PrintingImp printing = new PrintingImp();
            printing.setDriver(Configuracion.getInstance().impresoraDriver);
            printing.setDatabaseServices(ds);
            printing.setRegistros(pedidoDAO, detalles);

            if (Configuracion.getInstance().imprimir=="")
                Configuracion.getInstance().imprimir = "com.atcloud.android.bluetooth.BluetoothPrint";
            PrintInterface printer = (PrintInterface)Class.forName(Configuracion.getInstance().imprimir).newInstance();

            printer.doPrint(this, Configuracion.getInstance().impresora, printing.print());*/

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al imprimir el pedido: "+e.toString());
        }
    }

    private void abrirAnterior(int folio) {
        folioPedido = folio;

        try {
            DatabaseServices dsOperaciones = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

            PedidoDAO pedidoDAO = new PedidoDAO(folio);
            dsOperaciones.exists(pedidoDAO);

            List<DatabaseRecord> detalles = dsOperaciones.select(new DetPedidoDAO(), "folio = " + folioPedido);

            dsOperaciones.close();

            clienteDAO = new ClienteDAO(pedidoDAO.cliente);
            ds.exists(clienteDAO);

            for (DatabaseRecord record : detalles) {
                DetPedidoDAO detPedidoDAO = (DetPedidoDAO)record;

                productoDAO = new ProductoDAO(detPedidoDAO.codigo);
                ds.exists(productoDAO);

                PromocionDAO promocionDAO = (PromocionDAO)ds.first(new PromocionDAO(), "codigo = '"+productoDAO.codigo+"'");
                promocion = promocionDAO!=null;

                cantidad = detPedidoDAO.cantidad;
                addProductoToGrid(true);
            }

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al abrir el pedido anterior: "+e.getMessage());
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
