package mx.com.sagaji.android;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Message;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import java.util.Date;

import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.dao.GenericDAO;
import mx.com.sagaji.android.dao.entity.VisitaDAO;
import mx.com.sagaji.android.listener.OnClienteAceptarCancelarListener;
import mx.com.sagaji.android.to.CausaNoVentaTO;
import mx.com.sagaji.android.to.ClienteTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.to.UbicacionTO;
import mx.com.sagaji.android.util.Constantes;

public class ClienteActivity extends AppCompatActivity implements OnClienteAceptarCancelarListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static String LOGTAG = ClienteActivity.class.getCanonicalName();

    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private ClienteFragment clienteFragment;
    private int id = 0;
    private ConfiguracionTO configuracionTO;
    private ClienteTO clienteTO;
    private UbicacionTO ubicacionTO;
    private Date feiniciovisita;
    private Date fechaentrega;
    private String direccion;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setTitle("Cliente");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            id = (int)extras.getSerializable(Constantes.EXTRA_ID);
            configuracionTO = (ConfiguracionTO)extras.getSerializable(Constantes.EXTRA_CONFIGURACION);
            clienteTO = (ClienteTO)extras.getSerializable(Constantes.EXTRA_CLIENTE);
        }

        ubicacionTO = new UbicacionTO();
        feiniciovisita = new Date();

        clienteFragment = new ClienteFragment();
        clienteFragment.setParametros(configuracionTO, clienteTO);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, clienteFragment);
        fragmentTransaction.commit();

        handler.post(new Runnable() {
            public void run() {
                checaCliente();
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        ubicacionTO.updateLocation(location);
        clienteFragment.updateLocation(location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cliente, menu);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constantes.ACTIVITY_PEDIDOS) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public void onAceptar() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);
        bundle.putSerializable(Constantes.EXTRA_CLIENTE, clienteTO);
        bundle.putSerializable(Constantes.EXTRA_UBICACION, ubicacionTO);
        bundle.putSerializable(Constantes.EXTRA_INICIOVISTA, feiniciovisita);
        bundle.putSerializable(Constantes.EXTRA_FECHAENTREGA, fechaentrega);
        bundle.putSerializable(Constantes.EXTRA_DIRECCION, direccion);

        if (id == R.id.nav_pedidos) {
            Intent intent = new Intent(this, PedidosActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, Constantes.ACTIVITY_PEDIDOS);
        }
        else if (id == R.id.nav_devoluciones) {
            Intent intent = new Intent(this, DevolucionesActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, Constantes.ACTIVITY_DEVOLUCIONES);
        }
        else if (id == R.id.nav_cobranza) {
            Intent intent = new Intent(this, CobranzaActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, Constantes.ACTIVITY_COBRANZA);
        }
    }

    @Override
    public void onCancelar() {
        setResult(RESULT_CANCELED);

        finish();
    }

    @Override
    public void onFechaEntrega(Date fechaentrega) {
        this.fechaentrega = fechaentrega;
    }

    @Override
    public void onDireccionEntrega(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public void onGuardarCasaNoVenta(CausaNoVentaTO causaNoVentaTO) {
        int folioVisita = 0;

        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

        ds.beginTransaction();
        try {
            folioVisita = GenericDAO.obtenerSiguienteFolio(ds, Constantes.FOLIO_VISITA);

            ds.commit();
        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al obtener el folio de la visita: " + e.getMessage());
        }

        ds.beginTransaction();

        VisitaDAO visitaDAO = new VisitaDAO();
        try {
            visitaDAO.folio = folioVisita;
            visitaDAO.status = Constantes.ESTADO_TERMINADO;
            visitaDAO.fechainicio = feiniciovisita;
            visitaDAO.fechacreacion = new Date();
            visitaDAO.fechamodificacion = new Date();
            visitaDAO.filial = configuracionTO.filial;
            visitaDAO.intermediario = configuracionTO.intermediario;
            visitaDAO.cliente = clienteTO.cliente;
            visitaDAO.razonsocial = clienteTO.razonsocial;
            visitaDAO.causanopedido = causaNoVentaTO.causa;
            visitaDAO.foliopedido = 0;
            visitaDAO.totalpedido = 0.0;
            visitaDAO.latitud = location==null ? 0.0 : location.getLatitude();
            visitaDAO.longitud = location==null ? 0.0 : location.getLongitude();
            visitaDAO.respuesta = "";

            ds.insert(visitaDAO);

            ds.commit();
        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al guardar la visita: " + e.getMessage());
        }

        ds.close();

        Toast.makeText(this, "Visita [" + folioVisita + "] guardada correctamente.", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void checaCliente() {
        /*if (clienteTO.status.compareTo("C")==0) {
            Message.alert(this, "El Cliente "+clienteTO.cliente+"-"+clienteTO.nombre
                    +" se encuentra congelado, por lo tanto este pedido quedará sujeto a revisión.");
        }
        if (clienteTO.ubicado==0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    ubicacionCliente();
                }
            };

            Message.alert(this, dialogClickListener, "El Cliente "+clienteTO.cliente+"-"+clienteTO.nombre
                    +" no se ha ubicado, por favor proceda a ubicar al cliente.");
        }*/
    }

    private void ubicacionCliente() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);
        bundle.putSerializable(Constantes.EXTRA_CLIENTE, clienteTO);

        //Intent intent = new Intent(this, UbicacionClienteActivity.class);
        //intent.putExtras(bundle);

        //startActivityForResult(intent, 0);
    }
}