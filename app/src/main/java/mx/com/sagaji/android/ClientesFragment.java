package mx.com.sagaji.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Fecha;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mx.com.sagaji.android.adapter.ClientesAdapter;
import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.dao.DatabaseOperacionesOpenHelper;
import mx.com.sagaji.android.dao.entity.UbicacionDAO;
import mx.com.sagaji.android.listener.OnClienteSelectedListener;
import mx.com.sagaji.android.to.CategoriaTO;
import mx.com.sagaji.android.to.ClienteTO;

/**
 * Created by jbecerra.
 */
public class ClientesFragment extends Fragment {
    public static String LOGTAG = ClientesFragment.class.getCanonicalName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Spinner spinnerDias;
    private CheckBox chkOrden;
    private EditText edtFiltro;
    private EditText edtTotalClientes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clientes, container, false);
        setUpView(view);
        return view;
    }

    OnClienteSelectedListener clientesSelectedListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            clientesSelectedListener = (OnClienteSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement OnClienteSelectedListener");
        }
    }

    private void setUpView(View view) {
        Button btnAceptar = (Button) view.findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                List<ClienteTO> selected = ((ClientesAdapter)mAdapter).getSelectedRows();
                if (selected.size() == 0) {
                    Toast.makeText(getActivity(), "Debe de seleccionar un Cliente primero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (ClienteTO clienteTO : selected)
                    elijeCiente(clienteTO);
            }
        });

        edtTotalClientes = (EditText) view.findViewById(R.id.edtTotalClientes);

        ArrayAdapter adapterDias = new ArrayAdapter<CategoriaTO>(getActivity(), android.R.layout.simple_spinner_item);
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterDias.add(new CategoriaTO("", "Todos"));
        adapterDias.add(new CategoriaTO("7", "Domingo"));
        adapterDias.add(new CategoriaTO("1", "Lunes"));
        adapterDias.add(new CategoriaTO("2", "Martes"));
        adapterDias.add(new CategoriaTO("3", "Miércoles"));
        adapterDias.add(new CategoriaTO("4", "Jueves"));
        adapterDias.add(new CategoriaTO("5", "Viernes"));
        adapterDias.add(new CategoriaTO("6", "Sábado"));
        spinnerDias = (Spinner) view.findViewById(R.id.spinnerDias);
        spinnerDias.setAdapter(adapterDias);

        edtFiltro = (EditText) view.findViewById(R.id.edtFiltro);
        edtFiltro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                aplicaFiltro();
            }
        });

        chkOrden = (CheckBox) view.findViewById(R.id.chkOrden);
        chkOrden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aplicaFiltro();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.clientes_recycler);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ClientesAdapter(getActivity());
        ((ClientesAdapter) mAdapter).setClientes(new ArrayList<ClienteTO>());

        mRecyclerView.setAdapter(mAdapter);
    }

    public void clientesDelDia() {
        Calendar calendar = Fecha.getCalendar();
        int diaSemana = calendar.get(Calendar.DAY_OF_WEEK);
        spinnerDias.setSelection(diaSemana);

        spinnerDias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicaFiltro();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void aplicaFiltro() {
        StringBuilder where = new StringBuilder("1 = 1");

        CategoriaTO categoriaTO = (CategoriaTO) spinnerDias.getSelectedItem();
        //if (categoriaTO != null && categoriaTO.clave.length() > 0)
        //    where.append(" AND diasvisita = '").append(categoriaTO.clave).append("'");

        String filtro = edtFiltro.getText().toString();
        if (filtro.length() > 0)
            where.append(" AND (c.razonsocial LIKE '%").append(filtro).append("%' ")
                    .append(" OR c.cliente LIKE '").append(filtro).append("%')");

        List<ClienteTO> clientes = getClientes(where.toString());

        ((ClientesAdapter) mAdapter).setClientes(clientes);
    }

    private List<ClienteTO> getClientes(String where) {
        List<ClienteTO> clientes = new ArrayList<>();
        DatabaseServices ds = DatabaseOpenHelper.getInstance().getReadableDatabaseServices();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT cliente, razonsocial, propietario, rfc, mnsaldo, 0 AS ubicado ")
                    .append("FROM Cliente c ")
                    .append("WHERE ").append(where).append(" ")
                    .append("ORDER BY ")
                    .append(chkOrden.isChecked() ? "cliente" : "razonsocial");

            List<?> array = ds.collection(new ClienteTO(), sql.toString());
            for (Object clienteTO : array)
                clientes.add((ClienteTO) clienteTO);

            edtTotalClientes.setText(String.valueOf(array.size()));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        ds.close();
        return clientes;
    }

    private void elijeCiente(ClienteTO clienteTO) {
        DatabaseServices ds = DatabaseOperacionesOpenHelper.getInstance().getReadableDatabaseServices();
        try {
            UbicacionDAO ubicacionDAO = (UbicacionDAO) ds.first(new UbicacionDAO(), "cliente = '"+clienteTO.cliente+"'");
            clienteTO.ubicado = ubicacionDAO == null ? 0 : 1;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        ds.close();

        clientesSelectedListener.onSelected(clienteTO);
    }
}