package mx.com.sagaji.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Message;
import com.atcloud.android.util.Numero;
import java.util.List;

import mx.com.sagaji.android.adapter.CategoriasDetallesAdapter;
import mx.com.sagaji.android.dao.DatabaseCategoriasOpenHelper;
import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.entity.ProductoDAO;
import mx.com.sagaji.android.dao.entity.PromocionDAO;
import mx.com.sagaji.android.to.CategoriaDetalleTO;
import mx.com.sagaji.android.to.CategoriaTO;

public class CategoriasActivity extends AppCompatActivity {
    public static String LOGTAG = CategoriasActivity.class.getCanonicalName();
    private DatabaseServices dsCategorias = null;
    private DatabaseServices ds = null;
    private int categoriaSelectedIndex = 0;
    private int detallesSelectedIndex = 0;
    private Spinner spinnerCategoria = null;
    private String claveCategoriaA = null;
    private String claveCategoriaB = null;
    private String claveCategoriaC = null;
    private ArrayAdapter<CategoriaTO> adapterCategoriaA = null;
    private ArrayAdapter<CategoriaTO> adapterCategoriaB = null;
    private ArrayAdapter<CategoriaTO> adapterCategoriaC = null;
    private CategoriasDetallesAdapter detallesAdapter = null;
    private ListView lstCategoriaA = null;
    private ListView lstCategoriaB = null;
    private ListView lstCategoriaC = null;
    private EditText edtDetalles = null;
    private ListView lstDetalles = null;
    private ProductoDAO productoDAO = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        setTitle("Categorias");

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

        dsCategorias.close();
        ds.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_categorias, menu);
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
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setUpView() {
        spinnerCategoria = (Spinner)findViewById(R.id.spinnerCategoria);
        ArrayAdapter<?> adapterCategorias = ArrayAdapter.createFromResource(this, R.array.categorias_tipos,
                android.R.layout.simple_spinner_dropdown_item);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategorias);
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoriaSelectedIndex = position;

                iniciaCategorias();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        categoriaSelectedIndex = spinnerCategoria.getSelectedItemPosition();

        adapterCategoriaA = new ArrayAdapter<CategoriaTO>(this, R.layout.layout_list_item);
        adapterCategoriaB = new ArrayAdapter<CategoriaTO>(this, R.layout.layout_list_item);
        adapterCategoriaC = new ArrayAdapter<CategoriaTO>(this, R.layout.layout_list_item);

        lstCategoriaA = (ListView)findViewById(R.id.lstCategoriaA);
        lstCategoriaA.setAdapter(adapterCategoriaA);
        lstCategoriaA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoriaTO categoriaTO = (CategoriaTO)parent.getAdapter().getItem(position);

                claveCategoriaA = categoriaTO.clave;
                cargaCategoriaB();
            }
        });
        lstCategoriaB = (ListView)findViewById(R.id.lstCategoriaB);
        lstCategoriaB.setAdapter(adapterCategoriaB);
        lstCategoriaB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoriaTO categoriaTO = (CategoriaTO)parent.getAdapter().getItem(position);

                claveCategoriaB = categoriaTO.clave;
                cargaCategoriaC();
            }
        });
        lstCategoriaC = (ListView)findViewById(R.id.lstCategoriaC);
        lstCategoriaC.setAdapter(adapterCategoriaC);
        lstCategoriaC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoriaTO categoriaTO = (CategoriaTO)parent.getAdapter().getItem(position);

                claveCategoriaC = categoriaTO.clave;
                buscaProductos();
            }
        });

        edtDetalles = (EditText)findViewById(R.id.edtDetalles);

        lstDetalles = (ListView)findViewById(R.id.lstDetalles);

        detallesAdapter = new CategoriasDetallesAdapter(this, R.layout.layout_categorias_detalles);
        lstDetalles.setAdapter(detallesAdapter);
        lstDetalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoriaDetalleTO categoriaDetalleTO = (CategoriaDetalleTO)parent.getAdapter().getItem(position);
                detallesSelectedIndex = position;

                try {
                    productoDAO = new ProductoDAO();
                    productoDAO.codigo = categoriaDetalleTO.codigo;
                    ds.exists(productoDAO);

                    verDetalle();

                } catch(Exception e) {
                    Log.e(LOGTAG, e.getMessage(), e);
                    Message.alert(CategoriasActivity.this, "Exception: "+e.getMessage());
                }
            }
        });

        Button btnPasar = (Button)findViewById(R.id.btnPasar);
        btnPasar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pasarRegistros();
            }
        });

        dsCategorias = DatabaseCategoriasOpenHelper.getInstance().getWritableDatabaseServices();
        ds = DatabaseOpenHelper.getInstance().getWritableDatabaseServices();

        cargaCategoriaA();
    }

    static final int ArmadoraModeloDescripcion = 0;
    static final int LineaDescripcionModelo = 1;
    static final int DescripcionModeloLinea = 2;
    static final int ModeloDescripcionPeriodo = 3;

    private void iniciaCategorias() {
        detallesSelectedIndex = -1;
        detallesAdapter.clear();

        adapterCategoriaA.clear();
        adapterCategoriaB.clear();
        adapterCategoriaC.clear();

        cargaCategoriaA();
    }

    private void verDetalle() throws Exception {
        StringBuilder detalle = new StringBuilder();
        detalle
                .append(productoDAO.codigo).append(" ")
                .append(productoDAO.descripcion)
                .append(" ").append(Numero.getMoneda(productoDAO.precio))
                .append(" U.Med.:").append(productoDAO.unidadmedida)
                .append(" Exist:").append(Numero.getIntNumero(productoDAO.existencia))
        ;

        PromocionDAO promocionDAO = (PromocionDAO)ds.first(new PromocionDAO(), "codigo = '"+productoDAO.codigo+"'");
        if (promocionDAO!=null) {
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

    private void buscaProductos() {
        detallesSelectedIndex = -1;
        detallesAdapter.clear();

        String sql = null;

        switch(categoriaSelectedIndex) {
            case ArmadoraModeloDescripcion:
                sql = "SELECT clproducto AS codigo, descripcion " +
                        "FROM Catalogo " +
                        "WHERE clarmadora = '"+claveCategoriaA+"' AND clmodelo = '"+claveCategoriaB+"' AND cldescripcion = '"+claveCategoriaC+"' " +
                        "ORDER BY descripcion;";
                break;
            case LineaDescripcionModelo:
                sql = "SELECT clproducto AS codigo, descripcion " +
                        "FROM Catalogo " +
                        "WHERE cllinea = '"+claveCategoriaA+"' AND cldescripcion = '"+claveCategoriaB+"' AND clmodelo = '"+claveCategoriaC+"' " +
                        "ORDER BY descripcion;";
                break;
            case DescripcionModeloLinea:
                sql = "SELECT clproducto AS codigo, descripcion " +
                        "FROM Catalogo " +
                        "WHERE cldescripcion = '"+claveCategoriaA+"' AND clmodelo = '"+claveCategoriaB+"' AND cllinea = '"+claveCategoriaC+"' " +
                        "ORDER BY descripcion;";
                break;
            case ModeloDescripcionPeriodo:
                sql = "SELECT clproducto AS codigo, descripcion " +
                        "FROM Catalogo " +
                        "WHERE clmodelo = '"+claveCategoriaA+"' AND cldescripcion = '"+claveCategoriaB+"' AND clperiodo = '"+claveCategoriaC+"' " +
                        "ORDER BY descripcion;";
                break;
        }

        try {
            List<?> array = dsCategorias.collection(new CategoriaDetalleTO(), sql);

            if (array.size()!=0) {
                for (Object categoriaDetalleTO : array)
                    detallesAdapter.add((CategoriaDetalleTO)categoriaDetalleTO);
            } else {

                Toast.makeText(CategoriasActivity.this, "No se encontraron productos con las categorias seleccionadas.", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void cargaCategoriaC() {
        detallesSelectedIndex = -1;
        detallesAdapter.clear();

        adapterCategoriaC.clear();

        String sql = null;

        switch(categoriaSelectedIndex) {
            case ArmadoraModeloDescripcion:
                sql = "SELECT ArmadoraModeloDescripcion.cldescripcion AS clave, dsdescripcion AS descripcion " +
                        "FROM ArmadoraModeloDescripcion LEFT JOIN Descripcion ON ArmadoraModeloDescripcion.cldescripcion = Descripcion.cldescripcion " +
                        "WHERE ArmadoraModeloDescripcion.clarmadora = '"+claveCategoriaA+"' " +
                        "AND ArmadoraModeloDescripcion.clmodelo = '"+claveCategoriaB+"' " +
                        "ORDER BY dsdescripcion;";
                break;
            case LineaDescripcionModelo:
                sql = "SELECT LineaDescripcionModelo.clmodelo AS clave, dsmodelo AS descripcion " +
                        "FROM LineaDescripcionModelo LEFT JOIN Modelo ON LineaDescripcionModelo.clmodelo = Modelo.clmodelo " +
                        "WHERE LineaDescripcionModelo.cllinea = '"+claveCategoriaA+"' " +
                        "AND LineaDescripcionModelo.cldescripcion = '"+claveCategoriaB+"' " +
                        "ORDER BY dsmodelo;";
                break;
            case DescripcionModeloLinea:
                sql = "SELECT DescripcionModeloLinea.cllinea AS clave, dslinea AS descripcion " +
                        "FROM DescripcionModeloLinea LEFT JOIN Linea ON DescripcionModeloLinea.cllinea = Linea.cllinea " +
                        "WHERE DescripcionModeloLinea.cldescripcion = '"+claveCategoriaA+"' " +
                        "AND DescripcionModeloLinea.clmodelo = '"+claveCategoriaB+"' " +
                        "ORDER BY dslinea;";
                break;
            case ModeloDescripcionPeriodo:
                sql = "SELECT ModeloDescripcionPeriodo.clperiodo AS clave, dsperiodo AS descripcion " +
                        "FROM ModeloDescripcionPeriodo LEFT JOIN Periodo ON ModeloDescripcionPeriodo.clperiodo = Periodo.clperiodo " +
                        "WHERE ModeloDescripcionPeriodo.clmodelo = '"+claveCategoriaA+"' " +
                        "AND ModeloDescripcionPeriodo.cldescripcion = '"+claveCategoriaB+"' " +
                        "ORDER BY dsperiodo;";
                break;
        }

        try {
            List<?> array = dsCategorias.collection(new CategoriaTO(), sql);

            if (array.size()!=0) {
                for (Object categoriaTO : array) {
                    adapterCategoriaC.add((CategoriaTO)categoriaTO);
                }
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void cargaCategoriaB() {
        detallesSelectedIndex = -1;
        detallesAdapter.clear();

        adapterCategoriaC.clear();
        adapterCategoriaB.clear();

        String sql = null;

        switch(categoriaSelectedIndex) {
            case ArmadoraModeloDescripcion:
                sql = "SELECT ArmadoraModelo.clmodelo AS clave, dsmodelo AS descripcion " +
                        "FROM ArmadoraModelo LEFT JOIN Modelo ON ArmadoraModelo.clmodelo = Modelo.clmodelo " +
                        "WHERE ArmadoraModelo.clarmadora = '"+claveCategoriaA+"' " +
                        "ORDER BY dsmodelo;";
                break;
            case LineaDescripcionModelo:
                sql = "SELECT LineaDescripcion.cldescripcion AS clave, dsdescripcion AS descripcion " +
                        "FROM LineaDescripcion LEFT JOIN Descripcion ON LineaDescripcion.cldescripcion = Descripcion.cldescripcion " +
                        "WHERE LineaDescripcion.cllinea = '"+claveCategoriaA+"' " +
                        "ORDER BY dsdescripcion;";
                break;
            case DescripcionModeloLinea:
                sql = "SELECT DescripcionModelo.clmodelo AS clave, dsmodelo AS descripcion " +
                        "FROM DescripcionModelo LEFT JOIN Modelo ON DescripcionModelo.clmodelo = Modelo.clmodelo " +
                        "WHERE DescripcionModelo.cldescripcion = '"+claveCategoriaA+"' " +
                        "ORDER BY dsmodelo;";
                break;
            case ModeloDescripcionPeriodo:
                sql = "SELECT ModeloDescripcion.cldescripcion AS clave, dsdescripcion AS descripcion " +
                        "FROM ModeloDescripcion LEFT JOIN Descripcion ON ModeloDescripcion.cldescripcion = Descripcion.cldescripcion " +
                        "WHERE ModeloDescripcion.clmodelo = '"+claveCategoriaA+"' " +
                        "ORDER BY dsdescripcion;";
                break;
        }

        try {
            List<?> array = dsCategorias.collection(new CategoriaTO(), sql);

            if (array.size()!=0) {
                for (Object categoriaTO : array) {
                    adapterCategoriaB.add((CategoriaTO)categoriaTO);
                }
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void cargaCategoriaA() {
        detallesSelectedIndex = -1;
        detallesAdapter.clear();

        adapterCategoriaC.clear();
        adapterCategoriaB.clear();
        adapterCategoriaA.clear();

        String sql = null;

        switch(categoriaSelectedIndex) {
            case ArmadoraModeloDescripcion:
                sql = "SELECT clarmadora AS clave, dsarmadora AS descripcion FROM Armadora ORDER BY dsarmadora;";
                break;
            case LineaDescripcionModelo:
                sql = "SELECT cllinea AS clave, dslinea AS descripcion FROM Linea ORDER BY dslinea;";
                break;
            case DescripcionModeloLinea:
                sql = "SELECT cldescripcion AS clave, dsdescripcion AS descripcion FROM Descripcion ORDER BY dsdescripcion;";
                break;
            case ModeloDescripcionPeriodo:
                sql = "SELECT clmodelo AS clave, dsmodelo AS descripcion FROM Modelo ORDER BY dsmodelo;";
                break;
        }

        try {
            List<?> array = dsCategorias.collection(new CategoriaTO(), sql);

            if (array.size()!=0) {
                for (Object categoriaTO : array) {
                    adapterCategoriaA.add((CategoriaTO)categoriaTO);
                }
            }
        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
            Message.alert(this, "Exception: "+e.getMessage());
        }
    }

    private void pasarRegistros() {
        if (detallesAdapter.getCount()==0) {
            Toast.makeText(CategoriasActivity.this, "Debe de encontrar por lo menos un registro para poder pasarlo.", Toast.LENGTH_LONG).show();
            return;
        }

        if (detallesSelectedIndex == -1) {
            Toast.makeText(CategoriasActivity.this, "Debe de seleccionar un registro primero.", Toast.LENGTH_LONG).show();
            return;
        }

        CategoriaDetalleTO categoriaDetalleTO = detallesAdapter.getItem(detallesSelectedIndex);

        Intent intent = new Intent();
        intent.putExtra("registro", categoriaDetalleTO);

        setResult(RESULT_OK, intent);

        finish();
    }
}
