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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Message;
import com.atcloud.android.util.Numero;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mx.com.sagaji.android.adapter.DevolucionesDetalleAdapter;
import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.dao.GenericDAO;
import mx.com.sagaji.android.dao.entity.ClienteDAO;
import mx.com.sagaji.android.dao.entity.DetDevolucionDAO;
import mx.com.sagaji.android.dao.entity.DevolucionDAO;
import mx.com.sagaji.android.dao.entity.ProductoDAO;
import mx.com.sagaji.android.to.CategoriaTO;
import mx.com.sagaji.android.to.ClienteTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.to.DevolucionDetalleTO;
import mx.com.sagaji.android.to.DevolucionGuardarTO;
import mx.com.sagaji.android.to.FacturaDevolucionDetalleTO;
import mx.com.sagaji.android.to.FacturaDevolucionTO;
import mx.com.sagaji.android.util.ActivityUtil;
import mx.com.sagaji.android.util.Constantes;
import mx.com.sagaji.android.ws.FacturasDevolucionDetalleTask;
import mx.com.sagaji.android.ws.FacturasDevolucionTask;
import mx.com.sagaji.android.ws.SagajiWebService;

public class DevolucionesActivity extends AppCompatActivity {
    public static String LOGTAG = DevolucionesActivity.class.getCanonicalName();

    private SagajiWebService sagajiWebService;
    private ConfiguracionTO configuracionTO;
    private ClienteTO clienteTO;
    private Date feiniciovisita;
    private DatabaseServices ds = null;
    private Spinner spinnerCausa = null;
    private Spinner spinnerTipo = null;
    private Spinner spinnerCodigos = null;
    private EditText edtDocumento = null;
    private EditText edtDescuento = null;
    private EditText edtCantidad = null;
    private EditText edtCantidadModificar = null;
    private AlertDialog modificarDialog = null;
    private ListView lstDetalles = null;
    private ArrayAdapter<CategoriaTO> adapterCausa = null;
    private ArrayAdapter<FacturaDevolucionDetalleTO> adapterCodigos = null;
    private DevolucionesDetalleAdapter devolucionesDetalleAdapter = null;
    private DevolucionDetalleTO detDevolucionTO = null;
    private FacturaDevolucionDetalleTO facturaDevolucionDetalleTO = null;
    private int cantidad = 0;
    private int totTotalCantidad = 0;
    private double totTotal;
    private ClienteDAO clienteDAO = null;
    private int folioDevolucion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devoluciones);

        setTitle("Devoluciones");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            configuracionTO = (ConfiguracionTO) extras.getSerializable(Constantes.EXTRA_CONFIGURACION);
            clienteTO = (ClienteTO) extras.getSerializable(Constantes.EXTRA_CLIENTE);
            feiniciovisita = (Date) extras.getSerializable(Constantes.EXTRA_INICIOVISTA);
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
        getMenuInflater().inflate(R.menu.menu_devoluciones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_rotate) {
            ActivityUtil.rotar(this);
            return true;
        } else if (id == R.id.action_calculadora) {
            calculadora();
            return true;
        } else if (id == R.id.action_back) {
            salir();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
        inflater.inflate(R.menu.menu_devoluciones_detalles, menu);
    }

    private void setUpView() {
        adapterCausa = new ArrayAdapter<CategoriaTO>(this, android.R.layout.simple_spinner_dropdown_item);
        spinnerCausa = (Spinner)findViewById(R.id.spinnerCausa);
        adapterCausa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCausa.setAdapter(adapterCausa);

        ArrayAdapter<CategoriaTO> adapterTipo = new ArrayAdapter<CategoriaTO>(this, android.R.layout.simple_spinner_dropdown_item);
        adapterTipo.add(new CategoriaTO("0", "Mercancia"));
        adapterTipo.add(new CategoriaTO("1", "Garantia"));
        adapterTipo.add(new CategoriaTO("2", "Decomiso"));

        spinnerTipo = (Spinner)findViewById(R.id.spinnerTipo);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        adapterCodigos = new ArrayAdapter<FacturaDevolucionDetalleTO>(this, android.R.layout.simple_spinner_dropdown_item);
        spinnerCodigos = (Spinner)findViewById(R.id.spinnerCodigos);
        adapterCodigos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCodigos.setAdapter(adapterCodigos);

        edtDocumento = (EditText)findViewById(R.id.edtDocumento);
        edtDocumento.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_NONE
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = edtDocumento.getText().toString();
                    enterOnDocumento(text);
                    return true;
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
                    String text = edtDescuento.getText().toString();
                    enterOnDescuento(text);
                    return true;
                }
                return false;
            }
        });
        edtDescuento.setEnabled(false);

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
        edtCantidad.setEnabled(false);

        edtCantidadModificar = new EditText(this);
        edtCantidadModificar.setInputType(InputType.TYPE_CLASS_NUMBER);

        lstDetalles = (ListView)findViewById(R.id.lstDetalles);
        registerForContextMenu(lstDetalles);

        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.layout_devoluciones_detalles_header, lstDetalles, false);
        lstDetalles.addHeaderView(header, null, false);

        devolucionesDetalleAdapter = new DevolucionesDetalleAdapter(this, R.layout.layout_devoluciones_detalles);
        lstDetalles.setAdapter(devolucionesDetalleAdapter);
        lstDetalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                detDevolucionTO = (DevolucionDetalleTO) parent.getAdapter().getItem(position);
            }
        });
        lstDetalles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                detDevolucionTO = (DevolucionDetalleTO) parent.getAdapter().getItem(position);
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
                salir();
            }
        });
        Button btnAnteriores = (Button)findViewById(R.id.btnAnteriores);
        btnAnteriores.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                anteriores();
            }
        });

        despliegaTotales();

        ds = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();

        clienteDAO = new ClienteDAO(clienteTO.cliente);
        try {
            ds.exists(clienteDAO);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }

        cargaCausas();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constantes.ACTIVITY_ANTERIORES) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if(extras !=null) {

                    String accion = extras.getString("accion");

                    if (accion.equals(Constantes.ACCION_ABRIR)) {
                        if (devolucionesDetalleAdapter.getCount()>0) {
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
        else if (requestCode == Constantes.ACTIVITY_DEVOLUCIONES_GUARDAR) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if (extras!=null) {

                    DevolucionGuardarTO devolucionGuardarTO = (DevolucionGuardarTO)extras.getSerializable("devolucion-guardar");

                    guardarDevolucion(devolucionGuardarTO);

                    if (devolucionGuardarTO.salir)
                        finish();
                }
            }
        }
    }

    private void salir() {
        if (devolucionesDetalleAdapter.getCount() > 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            guardarDevolucion(true);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                        default:
                            finish();
                            break;
                    }
                    dialog.dismiss();
                }
            };

            Message.question(this, dialogClickListener, "¿Desea guardar la Devolucion?");

        } else {
            finish();
        }
    }

    private void guardarDevolucion(boolean salir) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("salir", salir);

        Intent intent = new Intent(this, DevolucionesGuardarActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_DEVOLUCIONES_GUARDAR);
    }

    private void guardarDevolucion(DevolucionGuardarTO devolucionGuardarTO) {
        boolean nuevo = folioDevolucion == 0;

        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

        if (folioDevolucion == 0) {
            ds.beginTransaction();
            try {
                folioDevolucion = GenericDAO.obtenerSiguienteFolio(ds, Constantes.FOLIO_DEVOLUCION);

                ds.commit();
            } catch (Exception e) {
                ds.rollback();

                Log.e(LOGTAG, e.getMessage(), e);
                Message.alert(this, "Error al obtener el folio del pedido: " + e.getMessage());
            }
        }

        ds.beginTransaction();

        try {
            DevolucionDAO devolucionDAO = new DevolucionDAO();
            devolucionDAO.folio = folioDevolucion;
            devolucionDAO.status = Constantes.ESTADO_TERMINADO;
            devolucionDAO.fechacreacion = new Date();
            devolucionDAO.fechamodificacion = new Date();
            devolucionDAO.filial = configuracionTO.filial; //Configuracion.getInstance().filialOperaciones;
            devolucionDAO.intermediario = configuracionTO.intermediario;
            devolucionDAO.cliente = clienteDAO.cliente;
            devolucionDAO.nombre = clienteDAO.razonsocial;
            devolucionDAO.partidas = devolucionesDetalleAdapter.getCount();
            devolucionDAO.cantidad = totTotalCantidad;
            devolucionDAO.total = totTotal;
            devolucionDAO.respuesta = "";
            devolucionDAO.impresiones = 0;
            devolucionDAO.tipo = "0";
            devolucionDAO.observaciones = devolucionGuardarTO.observaciones;

            if (nuevo) {
                ds.insert(devolucionDAO);
            } else {
                DevolucionDAO devolucionAnteriorDAO = new DevolucionDAO(folioDevolucion);
                ds.exists(devolucionAnteriorDAO);

                devolucionDAO.fechacreacion = devolucionAnteriorDAO.fechacreacion;
                devolucionDAO.impresiones = devolucionAnteriorDAO.impresiones;
                devolucionDAO.respuesta = devolucionAnteriorDAO.respuesta;

                ds.update(devolucionDAO);
                ds.execute("DELETE FROM DetDevolucion WHERE folio = " + folioDevolucion);
            }

            int index = 0;
            for(index = 0; index < devolucionDAO.partidas; index++) {
                DevolucionDetalleTO detDevolucionTO = devolucionesDetalleAdapter.getItem(index);

                DetDevolucionDAO detDevolucionDAO = new DetDevolucionDAO(folioDevolucion, detDevolucionTO.documento, detDevolucionTO.codigo);
                detDevolucionDAO.linea = detDevolucionTO.linea;
                detDevolucionDAO.cantidad = detDevolucionTO.cantidad;
                detDevolucionDAO.precio = detDevolucionTO.precio;
                detDevolucionDAO.total = detDevolucionTO.total;
                detDevolucionDAO.causa = detDevolucionTO.causa;
                detDevolucionDAO.descuento = detDevolucionTO.descuento;
                detDevolucionDAO.tipo = detDevolucionTO.tipo;

                if (ds.exists(detDevolucionDAO)) {
                    detDevolucionDAO.cantidad += detDevolucionTO.cantidad;
                    detDevolucionDAO.total += detDevolucionTO.total;

                    ds.update(detDevolucionDAO);
                } else {
                    ds.insert(detDevolucionDAO);
                }
            }

            ds.commit();
        } catch(Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al guardar la devolucion: "+e.getMessage());
        }

        ds.close();

        Toast.makeText(DevolucionesActivity.this, "Devolucion ["+folioDevolucion+"] guardada correctamente.", Toast.LENGTH_SHORT).show();
    }

    private void enterOnDocumento(String documento) {
        if (documento.length()==0)
            return;

        if (documento.compareTo("0")==0) {
            sagajiWebService.waitMessage(this, "Obtengo los Documentos de Devolucion ...");

            FacturasDevolucionTask facturasDevolucionTask = new FacturasDevolucionTask(sagajiWebService, this,
                    new String[] {sagajiWebService.getAccess(), clienteDAO.cliente});
            facturasDevolucionTask.execute();
        } else {
            sagajiWebService.waitMessage(this, "Obtengo los Detalles del Documento de Devolucion ...");

            FacturasDevolucionDetalleTask facturasDevolucionDetalleTask = new FacturasDevolucionDetalleTask(sagajiWebService, this,
                    new String[] {sagajiWebService.getAccess(), clienteDAO.cliente, configuracionTO.intermediario, documento});
            facturasDevolucionDetalleTask.execute();
        }
    }

    public void tengoFacturasDevolucion(ArrayList<FacturaDevolucionTO> facturas) {
        StringBuilder mensaje = new StringBuilder();
        for(FacturaDevolucionTO facturaDevolucionTO : facturas) {
            mensaje.append(facturaDevolucionTO.DOCUMENTO).append(" ")
                    .append(facturaDevolucionTO.ESTATUS).append("\n");
        }
        Message.mensaje(this, mensaje.toString());

        edtDocumento.setText("");
        edtDocumento.requestFocus();
    }

    public void tengoFacturaDevolucionDetalles(ArrayList<FacturaDevolucionDetalleTO> detalles) {
        adapterCodigos.clear();
        for(FacturaDevolucionDetalleTO facturaDevolucionDetalleTO : detalles) {
            adapterCodigos.add(facturaDevolucionDetalleTO);
        }

        edtDocumento.setEnabled(false);
        edtDescuento.setEnabled(true);
        edtCantidad.setEnabled(true);

        edtDescuento.requestFocus();
    }

    private void enterOnDescuento(String descuento) {
        if (descuento.length()==0)
            return;

        facturaDevolucionDetalleTO = (FacturaDevolucionDetalleTO)spinnerCodigos.getSelectedItem();
        if (facturaDevolucionDetalleTO==null) {
            edtDescuento.selectAll();

            Toast.makeText(DevolucionesActivity.this, "No hay codigos en el documento.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((Numero.redondea(Numero.getDoubleFromString(descuento) / 100.0)) > 0.99) {
            edtDescuento.selectAll();

            Toast.makeText(DevolucionesActivity.this, "No hay descuentos mayores al 99.99%.", Toast.LENGTH_SHORT).show();
            return;
        }

        edtCantidad.setText(String.valueOf(facturaDevolucionDetalleTO.CANTIDAD));
        edtCantidad.selectAll();
        edtCantidad.requestFocus();
    }

    private void enterOnCantidad(String scantidad) {
        facturaDevolucionDetalleTO = (FacturaDevolucionDetalleTO)spinnerCodigos.getSelectedItem();
        if (facturaDevolucionDetalleTO==null) {
            edtDescuento.selectAll();

            Toast.makeText(DevolucionesActivity.this, "No hay codigos en el documento.", Toast.LENGTH_SHORT).show();
            return;
        }

        cantidad = Numero.getIntFromString(edtCantidad.getText().toString());

        addProductoToGrid();
    }

    private void capturaCodigo() {
        edtCantidad.setText("");
        edtDescuento.setText("0.0");

        facturaDevolucionDetalleTO = null;

        edtDescuento.requestFocus();
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
        facturaDevolucionDetalleTO = (FacturaDevolucionDetalleTO)spinnerCodigos.getSelectedItem();
        if (facturaDevolucionDetalleTO==null) {
            edtDescuento.selectAll();

            Toast.makeText(DevolucionesActivity.this, "No hay codigos en el documento.", Toast.LENGTH_SHORT).show();
            return;
        }

        String documento = edtDocumento.getText().toString();
        String causa = ((CategoriaTO)spinnerCausa.getSelectedItem()).clave;
        String tipo = ((CategoriaTO)spinnerTipo.getSelectedItem()).clave;
        double descuento = Numero.getDoubleFromString(edtDescuento.getText().toString()) / 100.0;

        addProductoToGrid(true, documento, causa, tipo, descuento);
    }

    private void addProductoToGrid(boolean agregar, String documento, String causa, String tipo, double descuento) {
        DevolucionDetalleTO detDevolucionTO = new DevolucionDetalleTO();
        detDevolucionTO.documento = documento;
        detDevolucionTO.codigo = facturaDevolucionDetalleTO.CODPROD;
        detDevolucionTO.descripcion = facturaDevolucionDetalleTO.DESCRIP;
        detDevolucionTO.unidadmedida = facturaDevolucionDetalleTO.UM;
        detDevolucionTO.linea = "";
        detDevolucionTO.cantidad = cantidad;
        detDevolucionTO.precio = facturaDevolucionDetalleTO.PU;
        detDevolucionTO.total = Numero.redondea(detDevolucionTO.precio * detDevolucionTO.cantidad);
        detDevolucionTO.causa = causa;
        detDevolucionTO.descuento = Numero.redondea(descuento);
        detDevolucionTO.tipo = tipo;

        devolucionesDetalleAdapter.insert(detDevolucionTO, 0);

        totTotalCantidad += detDevolucionTO.cantidad;
        totTotal += detDevolucionTO.total;

        despliegaTotales();

        capturaCodigo();
    }

    private void limpiarDetalles(boolean confirma) {
        devolucionesDetalleAdapter.clear();

        detDevolucionTO = null;

        totTotalCantidad = 0;
        totTotal = 0.0;

        despliegaTotales();

        capturaCodigo();
    }

    private void limpiarDetalles() {
        if (devolucionesDetalleAdapter.getCount() > 0) {
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

            Message.question(this, dialogClickListener, "¿Realmente desea limpiar TODOS los detalles dla devolucion?");
        }
    }

    private void modificarDetalle() {
        if (detDevolucionTO==null) {
            Toast.makeText(DevolucionesActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        edtCantidadModificar.setText(String.valueOf(detDevolucionTO.cantidad));
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
        if (detDevolucionTO==null) {
            Toast.makeText(DevolucionesActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        if (cantidad <= 0) {
            Toast.makeText(DevolucionesActivity.this, "Debe de escribir una cantidad mayor a cero.", Toast.LENGTH_LONG).show();
            return;
        }

        totTotalCantidad -= detDevolucionTO.cantidad;
        totTotal -= detDevolucionTO.total;

        devolucionesDetalleAdapter.remove(detDevolucionTO);

        despliegaTotales();

        detDevolucionTO.cantidad = cantidad;
        detDevolucionTO.calcula();

        devolucionesDetalleAdapter.insert(detDevolucionTO, 0);

        totTotalCantidad += detDevolucionTO.cantidad;
        totTotal += detDevolucionTO.total;

        despliegaTotales();

        capturaCodigo();

        detDevolucionTO = null;
    }

    private void borrarDetalle() {
        if (detDevolucionTO==null) {
            Toast.makeText(DevolucionesActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        totTotalCantidad -= detDevolucionTO.cantidad;
        totTotal -= detDevolucionTO.total;

        despliegaTotales();

        devolucionesDetalleAdapter.remove(detDevolucionTO);

        detDevolucionTO = null;
    }

    private void despliegaTotales() {
        //txtTotalCantidad.setText(Numero.getIntNumero(totTotalCantidad));
        //txtTotal.setText(Numero.getMoneda(totTotal));
    }

    private void anteriores() {
        Bundle bundle = new Bundle();
        bundle.putString("daoList", DevolucionDAO.class.getCanonicalName());

        Intent intent = new Intent(this, AnterioresActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_ANTERIORES);
    }

    private void reenviarAnterior(int folio) {
        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        ds.beginTransaction();
        try {
            ds.execute("UPDATE Devolucion SET status = '" + Constantes.ESTADO_TERMINADO + "' WHERE folio = " + folio);

            ds.commit();

            Toast.makeText(DevolucionesActivity.this, "Devolucion [" + folio + "] marcado para reenvio.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al borrar la devolucion anterior: "+e.getMessage());
        }
        ds.close();
    }

    private void borrarAnterior(int folio) {
        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        ds.beginTransaction();
        try {
            ds.execute("UPDATE Devolucion SET status = '" + Constantes.ESTADO_BORRADO + "' WHERE folio = " + folio);

            ds.commit();

            Toast.makeText(DevolucionesActivity.this, "Devolucion [" + folio + "] borrada correctamente.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al borrar la devolucion anterior: "+e.getMessage());
        }
        ds.close();
    }

    private void imprimirAnterior(int folio) {
        try {
            DatabaseServices dsOperaciones = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
            dsOperaciones.beginTransaction();

            DevolucionDAO devolucionDAO = new DevolucionDAO(folio);
            dsOperaciones.exists(devolucionDAO);

            devolucionDAO.impresiones ++;
            try {
                dsOperaciones.execute("UPDATE Devolucion SET impresiones = " + devolucionDAO.impresiones + " WHERE folio = " + folio);

                dsOperaciones.commit();
            } catch (Exception e) {
                dsOperaciones.rollback();

                Log.e(LOGTAG, e.getMessage(), e);
            }

            List<DatabaseRecord> detalles = dsOperaciones.select(new DetDevolucionDAO(), "folio = " + devolucionDAO.folio);

            dsOperaciones.close();

            /*PrintingImp printing = new PrintingImp();
            printing.setDriver(Configuracion.getInstance().impresoraDriver);
            printing.setDatabaseServices(ds);
            printing.setRegistros(devolucionDAO, detalles);

            if (Configuracion.getInstance().imprimir=="")
                Configuracion.getInstance().imprimir = "com.atcloud.android.bluetooth.BluetoothPrint";
            PrintInterface printer = (PrintInterface)Class.forName(Configuracion.getInstance().imprimir).newInstance();

            printing.setLeyenda("*** Original Cliente ***");
            printer.doPrint(this, Configuracion.getInstance().impresora, printing.print());

            printing.setLeyenda("*** Copia Intermediario ***");
            printer.doPrint(this, Configuracion.getInstance().impresora, printing.print());*/

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al imprimir la devolucion: "+e.toString());
        }
    }

    private void abrirAnterior(int folio) {
        folioDevolucion = folio;

        try {
            DatabaseServices dsOperaciones = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

            DevolucionDAO devolucionDAO = new DevolucionDAO(folio);
            dsOperaciones.exists(devolucionDAO);

            List<DatabaseRecord> detalles = dsOperaciones.select(new DetDevolucionDAO(), "folio = " + folioDevolucion);

            dsOperaciones.close();

            clienteDAO = new ClienteDAO(devolucionDAO.cliente);
            ds.exists(clienteDAO);

            for (DatabaseRecord record : detalles) {
                DetDevolucionDAO detDevolucionDAO = (DetDevolucionDAO)record;

                ProductoDAO productoDAO = new ProductoDAO(detDevolucionDAO.codigo);
                ds.exists(productoDAO);

                facturaDevolucionDetalleTO = new FacturaDevolucionDetalleTO();
                facturaDevolucionDetalleTO.CODPROD = productoDAO.codigo;
                facturaDevolucionDetalleTO.DESCRIP = productoDAO.descripcion;
                facturaDevolucionDetalleTO.UM = productoDAO.unidadmedida;
                facturaDevolucionDetalleTO.PU = detDevolucionDAO.precio;

                cantidad = detDevolucionDAO.cantidad;

                String documento = detDevolucionDAO.documento;
                String causa = detDevolucionDAO.causa;
                String tipo = detDevolucionDAO.tipo;
                double descuento = detDevolucionDAO.descuento;

                addProductoToGrid(true, documento, causa, tipo, descuento);
            }

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al abrir la devolucion anterior: "+e.getMessage());
        }
    }

    private void cargaCausas() {
        adapterCausa.clear();

        try {
            List<?> array = ds.collection(new CategoriaTO(),
                    "SELECT clave, descripcion FROM MotivoDevolucion ORDER BY clave;");

            if (array.size()!=0) {
                for (Object categoriaTO : array) {
                    adapterCausa.add((CategoriaTO)categoriaTO);
                }
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
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
