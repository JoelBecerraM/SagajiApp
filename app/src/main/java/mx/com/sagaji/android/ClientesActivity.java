package mx.com.sagaji.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.atcloud.android.util.Message;

import mx.com.sagaji.android.listener.OnClienteSelectedListener;
import mx.com.sagaji.android.to.ClienteTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Constantes;

public class ClientesActivity extends AppCompatActivity implements OnClienteSelectedListener {
    public static String LOGTAG = ClientesActivity.class.getCanonicalName();

    private int id = 0;
    private ConfiguracionTO configuracionTO;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Clientes");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = (int) extras.getSerializable(Constantes.EXTRA_ID);
            configuracionTO = (ConfiguracionTO) extras.getSerializable(Constantes.EXTRA_CONFIGURACION);
        }

        Fragment fragment = new ClientesFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        handler.post(new Runnable() {
            public void run() {
                checaServiciosUbicacion();
                if (id == R.id.nav_pedidos) {
                    checaPedidoPendiente();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
        if (fragment instanceof ClientesFragment) {
            ((ClientesFragment) fragment).clientesDelDia();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_clientes, menu);
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
        if (requestCode == Constantes.ACTIVITY_CLIENTE) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    @Override
    public void onSelected(ClienteTO clienteTO) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constantes.EXTRA_ID, id);
        bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);
        bundle.putSerializable(Constantes.EXTRA_CLIENTE, clienteTO);

        Intent intent = new Intent(this, ClienteActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, Constantes.ACTIVITY_CLIENTE);

        if (id == R.id.nav_pedidos) {
            /*if (configuracionTO.haypublicidad) {
                Intent intentp = new Intent(this, PublicidadActivity.class);
                intentp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentp.putExtras(bundle);
                startActivity(intentp);
            }*/
        }
    }

    private void checaServiciosUbicacion() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);

                    finish();
                }
            };

            Message.alert(this, dialogClickListener,
                    "Los servicios de ubicación no estan activos, debe de activarlos para poder continuar.");
        }
    }

    private void checaPedidoPendiente() {
        boolean existePedidoPendiente = false;
        /*DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getReadableDatabaseServices();
        final PedidoDAO pedidoDAO = new PedidoDAO(0);
        try {
            existePedidoPendiente = ds.exists(pedidoDAO);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        ds.close();

        if (existePedidoPendiente) {
            ClienteUtil clienteUtil = new ClienteUtil();
            final ClienteTO clienteTO = clienteUtil.getCliente(pedidoDAO.cliente);

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            abrePedidoPendiente(clienteTO, pedidoDAO);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                        default:
                            break;
                    }
                    dialog.dismiss();
                }
            };

            Message.question(this, dialogClickListener, "Hay un pedido pendiente del Cliente [" + clienteTO.toString() + "] "
                    + " con fecha de [" + Fecha.getFechaHora(pedidoDAO.fechacreacion) + "] y total de [" + Numero.getMoneda(pedidoDAO.total)
                    + "], ¿desea abrir este Pedido Pendiente?");
        }*/
    }

    /*private void abrePedidoPendiente(ClienteTO clienteTO, PedidoDAO pedidoDAO) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);
        bundle.putSerializable(Constantes.EXTRA_CLIENTE, clienteTO);
        bundle.putSerializable(Constantes.EXTRA_INICIOVISTA, pedidoDAO.fechainicio);
        bundle.putSerializable(Constantes.EXTRA_FECHAENTREGA, pedidoDAO.fechaentrega);
        bundle.putSerializable(Constantes.EXTRA_DIRECCION, pedidoDAO.direccion);
        bundle.putString(Constantes.EXTRA_FOLIO, "0");

        Intent intent = new Intent(this, PedidosActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, Constantes.ACTIVITY_PEDIDOS);
    }*/
}