package mx.com.sagaji.android;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.atcloud.android.dao.engine.DatabaseList;
import com.atcloud.android.dao.engine.DatabaseRecord;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Message;

import java.util.ArrayList;
import java.util.List;

import mx.com.sagaji.android.adapter.AnterioresAdapter;
import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.util.ActivityUtil;
import mx.com.sagaji.android.util.Constantes;

public class AnterioresActivity extends AppCompatActivity {
    public static String LOGTAG = AnterioresActivity.class.getCanonicalName();
    private View viewDeposito = null;
    private DatabaseServices ds = null;
    private ListView lstDetalles = null;
    private AnterioresAdapter adapter = null;
    private RadioButton radPorEnviar = null;
    private RadioButton radEnviados = null;
    private Button btnReenviar = null;
    private Button btnAbrir = null;
    private String daoList = null;
    private DatabaseList registro = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anteriores);

        setTitle("Anteriores");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        }

        setUpView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ds.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_anteriores, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_rotate) {
            ActivityUtil.rotar(this);
            return true;
        }
        else if (id == R.id.action_back) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opcAbrir:
                abrirAnterior();
                return true;
            case R.id.opcImprimir:
                imprimirAnterior();
                return true;
            case R.id.opcBorrar:
                borrarAnterior();
                return true;
            case R.id.opcReenviar:
                reenviarAnterior();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_anteriores_detalles, menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setUpView() {
        viewDeposito = (View)findViewById(R.id.viewDeposito);

        lstDetalles = (ListView)findViewById(R.id.lstDetalles);
        registerForContextMenu(lstDetalles);

        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.layout_anteriores_detalles_header, lstDetalles, false);
        lstDetalles.addHeaderView(header, null, false);

        adapter = new AnterioresAdapter(this, R.layout.layout_anteriores_detalles, new ArrayList<DatabaseList>());
        lstDetalles.setAdapter(adapter);
        lstDetalles.setTextFilterEnabled(true);
        lstDetalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                registro = (DatabaseList)parent.getAdapter().getItem(position);
            }
        });
        lstDetalles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                registro = (DatabaseList)parent.getAdapter().getItem(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        radPorEnviar = (RadioButton)findViewById(R.id.radPorEnviar);
        radPorEnviar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cargaLista();
            }
        });

        radEnviados = (RadioButton)findViewById(R.id.radEnviados);
        radEnviados.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cargaLista();
            }
        });

        Button btnDeposito = (Button)findViewById(R.id.btnDeposito);
        btnDeposito.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                depositoCobranza();
            }
        });
        Button btnTicketDeposito = (Button)findViewById(R.id.btnTicketDeposito);
        btnTicketDeposito.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                imprimeDepositoCobranza();
            }
        });
        Button btnBorrar = (Button)findViewById(R.id.btnBorrar);
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                borrarAnterior();
            }
        });
        btnReenviar = (Button)findViewById(R.id.btnReenviar);
        btnReenviar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                reenviarAnterior();
            }
        });
        btnAbrir = (Button)findViewById(R.id.btnAbrir);
        btnAbrir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                abrirAnterior();
            }
        });

        Button btnImprimir = (Button)findViewById(R.id.btnImprimir);
        btnImprimir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                imprimirAnterior();
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            daoList = extras.getString("daoList");
        }

        if (daoList.endsWith("CobranzaDAO"))
            viewDeposito.setVisibility(View.VISIBLE);

        ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();

        radPorEnviar.setChecked(true);

        cargaLista();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constantes.ACTIVITY_REFERENCIA) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();

                if (extras!=null) {
                    //CobranzaReferenciaTO cobranzaReferenciaTO = (CobranzaReferenciaTO)extras.getSerializable("cobranza-referencia");

                    //aplicaDepositoCobranza(cobranzaReferenciaTO);
                }
            }
        }
    }

    private void cargaLista() {
        try {
            btnReenviar.setEnabled(radEnviados.isChecked());
            btnAbrir.setEnabled(radPorEnviar.isChecked());

            if(radPorEnviar.isChecked())
                cargaLista(Constantes.ESTADO_TERMINADO);
            if(radEnviados.isChecked())
                cargaLista(Constantes.ESTADO_ENVIADO);
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(AnterioresActivity.this, "Error al cargar la lista: "+e.getMessage());
        }
    }

    private void cargaLista(String estado) throws Exception {
        DatabaseList dao = (DatabaseList)Class.forName(daoList).newInstance();

        adapter.clear();

        List<DatabaseRecord> lista = ds.select((DatabaseRecord)dao, "status = '"+estado+"'", "folio DESC");
        for (DatabaseRecord record : lista) {
            adapter.add((DatabaseList)record);
        }

        registro = null;
    }

    private void reenviarAnterior(boolean confirmado) {
        Intent intent = new Intent();
        intent.putExtra("accion", Constantes.ACCION_REENVIAR);
        intent.putExtra("folio", registro.getFolio());

        setResult(RESULT_OK, intent);

        finish();
    }

    private void reenviarAnterior() {
        if (registro==null) {
            Toast.makeText(AnterioresActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        reenviarAnterior(true);

                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        break;
                }
                dialog.dismiss();
            }
        };

        Message.question(this, dialogClickListener, "¿Realmente desea volver a enviar este registro [" + registro.getFolio() + "]?");
    }

    private void borrarAnterior(boolean confirmado) {
        //Intent intent = new Intent();
        //intent.putExtra("accion", Constantes.ACCION_BORRAR);
        //intent.putExtra("folio", registro.getFolio());
        //
        //setResult(RESULT_OK, intent);
        //
        //finish();

        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getWritableDatabaseServices();
        ds.beginTransaction();
        try {

            if (daoList.endsWith("CobranzaDAO")) {
                /*CobranzaDAO cobranzaDAO = (CobranzaDAO)registro;

                if (cobranzaDAO.impresiones > 0)
                    throw new Exception("No se puede borrar porque ya se imprimio.");*/
            }

            if (ds.exists((DatabaseRecord)registro)) {
                registro.setStatus(Constantes.ESTADO_BORRADO);

                ds.update((DatabaseRecord)registro);
            }

            if (daoList.endsWith("CobranzaDAO")) {
                /*CobranzaDAO cobranzaDAO = (CobranzaDAO)registro;

                HtlDAO htlDAO = new HtlDAO();
                htlDAO.folio = null;
                htlDAO.foliocobranza = cobranzaDAO.folio;
                htlDAO.status = Constantes.ESTADO_TERMINADO;
                htlDAO.fechacreacion = new Date();
                htlDAO.fechamodificacion = new Date();
                htlDAO.filial = cobranzaDAO.filial;
                htlDAO.agente = cobranzaDAO.agente;
                htlDAO.cliente = cobranzaDAO.cliente;
                htlDAO.monto = cobranzaDAO.total;
                htlDAO.accion = "Eliminado";

                ds.insert(htlDAO);

                List<DatabaseRecord> detalles = ds.select(new DetCobranzaDAO(), "folio = "+registro.getFolio());

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
                }*/
            }

            ds.commit();

            Toast.makeText(AnterioresActivity.this, "Registro con folio [" + registro.getFolio() + "] borrado correctamente.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(AnterioresActivity.this, "Error al borrar el registro anterior: "+e.getMessage());
        }
        ds.close();

        cargaLista();
    }

    private void borrarAnterior() {
        if (registro==null) {
            Toast.makeText(AnterioresActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        borrarAnterior(true);

                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        break;
                }
                dialog.dismiss();
            }
        };

        Message.question(this, dialogClickListener, "¿Realmente desea borrar este registro [" + registro.getFolio() + "]?");
    }

    private void abrirAnterior() {
        if (registro==null) {
            Toast.makeText(AnterioresActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!registro.getStatus().equals(Constantes.ESTADO_TERMINADO)) {
            Toast.makeText(AnterioresActivity.this, "Solo se pueden abrir los registros que estan terminados.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("accion", Constantes.ACCION_ABRIR);
        intent.putExtra("folio", registro.getFolio());

        setResult(RESULT_OK, intent);

        finish();
    }

    private void imprimirAnterior() {
        if (registro==null) {
            Toast.makeText(AnterioresActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!registro.getStatus().equals(Constantes.ESTADO_TERMINADO)
                &&!registro.getStatus().equals(Constantes.ESTADO_ENVIADO)) {
            Toast.makeText(AnterioresActivity.this, "Solo se pueden imprimir los registros que estan terminados o enviados.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("accion", Constantes.ACCION_IMPRIMIR);
        intent.putExtra("folio", registro.getFolio());

        setResult(RESULT_OK, intent);

        finish();
    }

    //
    //
    //

    private void imprimeDepositoCobranza() {
        if (registro==null) {
            Toast.makeText(AnterioresActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        imprimeDepositoCobranza(registro.getFolio());

        registro = null;
    }

    private void imprimeDepositoCobranza(int folio) {
        try {
            /*CobranzaDepositoDAO cobranzaDepositoDAO = new CobranzaDepositoDAO(registro.getFolio());

            if (!ds.exists(cobranzaDepositoDAO)) {
                Toast.makeText(AnterioresActivity.this, "No encuentro el deposito de cobranza con folio ["+registro.getFolio()+"].", Toast.LENGTH_LONG).show();
                return;
            }

            ds.beginTransaction();

            CobranzaDAO cobranzaDAO = new CobranzaDAO(folio);
            ds.exists(cobranzaDAO);

            List<DatabaseRecord> detalles = ds.select(new DetCobranzaDAO(), "folio = " + cobranzaDAO.folio);

            try {
                HtlDAO htlDAO = new HtlDAO();
                htlDAO.folio = null;
                htlDAO.foliocobranza = folio;
                htlDAO.status = Constantes.ESTADO_TERMINADO;
                htlDAO.fechacreacion = new Date();
                htlDAO.fechamodificacion = new Date();
                htlDAO.filial = cobranzaDAO.filial;
                htlDAO.agente = cobranzaDAO.agente;
                htlDAO.cliente = cobranzaDAO.cliente;
                htlDAO.monto = cobranzaDAO.total;
                htlDAO.accion = "ImpresoTD";

                ds.insert(htlDAO);

                cobranzaDAO.impresiones ++;

                ds.execute("UPDATE Cobranza SET impresiones = " + cobranzaDAO.impresiones + " WHERE folio = " + folio);

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

                ds.commit();
            } catch (Exception e) {
                ds.rollback();

                Log.e(LOGTAG, e.getMessage(), e);
            }

            for(int i=0; i<detalles.size(); i++) {
                DetCobranzaDAO detCobranzaDAO = (DetCobranzaDAO)detalles.get(i);
                detCobranzaDAO.tipopago = "99";
                detCobranzaDAO.banco = cobranzaDepositoDAO.banco;
                detCobranzaDAO.referencia = cobranzaDepositoDAO.referencia;
                detCobranzaDAO.fechacobro = cobranzaDepositoDAO.fechacobro;

                detalles.set(i, detCobranzaDAO);
            }

            DatabaseServices dsCatalogos = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();
            try {
                PrintingImp printing = new PrintingImp();
                printing.setDriver(Configuracion.getInstance().impresoraDriver);
                printing.setDatabaseServices(dsCatalogos);
                printing.setRegistros(cobranzaDAO, detalles);

                BluetoothPrint BTPrint = new BluetoothPrint();

                if (cobranzaDAO.impresiones == 1) {
                    printing.setLeyenda("*** Original Cliente ***");
                    BTPrint.doPrint(this, Configuracion.getInstance().impresora, printing.print());
                }

                printing.setLeyenda("*** Copia Intermediario ***");
                BTPrint.doPrint(this, Configuracion.getInstance().impresora, printing.print());

            } catch(Exception ex) {
                Log.e(LOGTAG, ex.getMessage(), ex);
            }
            dsCatalogos.close();
            */

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al imprimir el deposito de cobranza: "+e.toString());
        }
    }

    private void depositoCobranza() {
        if (registro==null) {
            Toast.makeText(AnterioresActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        /*CobranzaDAO cobranzaDAO = new CobranzaDAO(registro.getFolio());
        try {
            if (!ds.exists(cobranzaDAO)) {
                Toast.makeText(AnterioresActivity.this, "No encuentro la cobranza con folio ["+registro.getFolio()+"].", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }

        DatabaseServices dsCatalogos = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();
        ClienteDAO clienteDAO = new ClienteDAO(registro.getCliente());
        try {
            if (!dsCatalogos.exists(clienteDAO)) {
                Toast.makeText(AnterioresActivity.this, "No encuentro el cliente ["+registro.getCliente()+"].", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        dsCatalogos.close();
        */

        /*Bundle bundle = new Bundle();
        bundle.putString("referencia", clienteDAO.referencia);

        Intent intent = new Intent(this, CobranzaReferenciaActivity.class);
        intent.putExtras(bundle);

        startActivityForResult(intent, REFERENCIA_REQUEST);*/
    }

    /*private void aplicaDepositoCobranza(CobranzaReferenciaTO cobranzaReferenciaTO) {
        if (registro==null) {
            Toast.makeText(AnterioresActivity.this, "Seleccione un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        if (cobranzaReferenciaTO.banco.length()==0) {
            Toast.makeText(AnterioresActivity.this, "No se ha seleccionado un Banco.", Toast.LENGTH_LONG).show();
            return;
        }
        if (cobranzaReferenciaTO.referencia.length()==0) {
            Toast.makeText(AnterioresActivity.this, "No se ha escrito una Referencia.", Toast.LENGTH_LONG).show();
            return;
        }
        if (cobranzaReferenciaTO.fechacobro.length()==0) {
            Toast.makeText(AnterioresActivity.this, "No se ha seleccionado una Fecha.", Toast.LENGTH_LONG).show();
            return;
        }

        List<DatabaseRecord> detalles = null;

        CobranzaDAO cobranzaDAO = new CobranzaDAO(registro.getFolio());
        try {
            if (!ds.exists(cobranzaDAO)) {
                Toast.makeText(AnterioresActivity.this, "No encuentro la cobranza con folio ["+registro.getFolio()+"].", Toast.LENGTH_LONG).show();
                return;
            }

            detalles = ds.select(new DetCobranzaDAO(), "folio = "+registro.getFolio());
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }

        TotalCobranzaTO totalCobranzaTO = null;
        try {
            List<Object> totales = ds.collection(new TotalCobranzaTO(),
                    "SELECT folio, SUM(pago) AS pago, SUM(importe) AS importe, SUM(descuento) AS descuento FROM DetCobranza "
                            +"WHERE folio = "+cobranzaDAO.folio);

            totalCobranzaTO = (TotalCobranzaTO)totales.get(0);
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }

        if (totalCobranzaTO==null) {
            Toast.makeText(AnterioresActivity.this, "No pude determinar los totales de la Cobranza.", Toast.LENGTH_LONG).show();
            return;
        }

        if (totalCobranzaTO.descuento > totalCobranzaTO.pago) {
            Message.alert(AnterioresActivity.this, "El descuento para este folio es MAYOR al monto. Por favor de Modificarlo.");
            Message.alert(AnterioresActivity.this, "Los datos del deposito NO SE GUARDARON correctamente.");
            return;
        }

        ds.beginTransaction();

        try {
            CobranzaDepositoDAO cobranzaDepositoDAO = new CobranzaDepositoDAO();
            cobranzaDepositoDAO.folio = cobranzaDAO.folio;
            cobranzaDepositoDAO.referencia = cobranzaReferenciaTO.referencia;
            cobranzaDepositoDAO.banco = cobranzaReferenciaTO.banco;
            cobranzaDepositoDAO.fechacobro = cobranzaReferenciaTO.fechacobro;

            ds.execute("DELETE FROM CobranzaDeposito WHERE folio = "+cobranzaDAO.folio);

            ds.insert(cobranzaDepositoDAO);

            ds.execute("UPDATE DetCobranza SET referencia = '"+cobranzaReferenciaTO.referencia+"', banco = '"+
                    cobranzaReferenciaTO.banco+"', fechacobro = '"+cobranzaReferenciaTO.fechacobro+"' WHERE folio = "+cobranzaDAO.folio);

            HtlDAO htlDAO = new HtlDAO();
            htlDAO.folio = null;
            htlDAO.foliocobranza = cobranzaDAO.folio;
            htlDAO.status = Constantes.ESTADO_TERMINADO;
            htlDAO.fechacreacion = new Date();
            htlDAO.fechamodificacion = new Date();
            htlDAO.filial = cobranzaDAO.filial;
            htlDAO.agente = cobranzaDAO.agente;
            htlDAO.cliente = cobranzaDAO.cliente;
            htlDAO.monto = cobranzaDAO.total;
            htlDAO.accion = "Deposito";

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

            ds.commit();
        } catch(Exception e) {
            ds.rollback();

            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Error al guardar el deposito de cobranza: "+e.getMessage());
        }

        Toast.makeText(AnterioresActivity.this, "Deposito de Cobranza ["+cobranzaDAO.folio+"] guardado correctamente.", Toast.LENGTH_SHORT).show();
    }*/
}
