package mx.com.sagaji.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import com.atcloud.android.dao.engine.DatabaseServices;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Message;
import com.atcloud.android.util.Numero;
import com.atcloud.android.view.DatePickerFragment;
import java.util.Calendar;
import java.util.List;

import mx.com.sagaji.android.dao.DatabaseOpenHelper;
import mx.com.sagaji.android.listener.OnClienteAceptarCancelarListener;
import mx.com.sagaji.android.to.CausaNoVentaTO;
import mx.com.sagaji.android.to.ClienteTO;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.to.DireccionEntregaTO;
import mx.com.sagaji.android.util.Constantes;

/**
 * Created by jbecerra.
 */
public class ClienteFragment extends Fragment {
    public static String LOGTAG = ClienteFragment.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;
    private ClienteTO clienteTO;
    private Spinner spinnerCausaNoVenta;
    private Calendar calendarFechaEntrega;
    private TextView edtFechaEntrega;
    private Spinner spinnerDireccionEntrega;
    private TextView edtUbicacion;
    private Button btnAceptar;
    private int tiempoEspera;

    AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {
        private boolean espera;

        protected Void doInBackground(Void... params) {
            espera = true;
            for (int i = 0; i < tiempoEspera; i++) {
                publishProgress(i);
                if (!espera)
                    break;

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            int esperaSegundos = tiempoEspera - progress[0];
            btnAceptar.setText("Espera ("+esperaSegundos+")");

            espera = edtUbicacion.getText().toString().length() == 0;
        }

        protected void onPostExecute(Void result) {
            btnAceptar.setText("Aceptar");
            btnAceptar.setEnabled(true);
        }
    };

    public void setParametros(ConfiguracionTO configuracionTO, ClienteTO clienteTO) {
        this.configuracionTO = configuracionTO;
        this.clienteTO = clienteTO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cliente, container, false);
        setUpView(view);
        return view;
    }

    OnClienteAceptarCancelarListener clienteAceptarCancelarListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            clienteAceptarCancelarListener = (OnClienteAceptarCancelarListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement OnClienteAceptarCancelarListener");
        }
    }

    private void setUpView(View view) {
        setTextView(view, R.id.edtRazonSocial, clienteTO.razonsocial);
        setTextView(view, R.id.edtCliente, clienteTO.cliente);
        setTextView(view, R.id.edtSaldo, Numero.getMoneda(clienteTO.mnsaldo));

        switch (Numero.getIntFromString("1")) {
            case 7:
                setTextView(view, R.id.textDo, "X");
                break;
            case 1:
                setTextView(view, R.id.textLu, "X");
                break;
            case 2:
                setTextView(view, R.id.textMa, "X");
                break;
            case 3:
                setTextView(view, R.id.textMi, "X");
                break;
            case 4:
                setTextView(view, R.id.textJu, "X");
                break;
            case 5:
                setTextView(view, R.id.textVi, "X");
                break;
            case 6:
                setTextView(view, R.id.textSa, "X");
                break;
        }

        btnAceptar = (Button) view.findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                clienteAceptarCancelarListener.onAceptar();
            }
        });

        Button btnSalir = (Button)view.findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                clienteAceptarCancelarListener.onCancelar();
            }
        });

        Button btnObjetivos = (Button)view.findViewById(R.id.btnObjetivos);
        btnObjetivos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constantes.EXTRA_CLIENTE, clienteTO);

                //Intent intent = new Intent(getActivity(), ObjetivosActivity.class);
                //intent.putExtras(bundle);

                //startActivityForResult(intent, 0);
            }
        });
        btnObjetivos.setVisibility(View.INVISIBLE);

        Button btnUbicacion = (Button)view.findViewById(R.id.btnUbicacion);
        btnUbicacion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ubicacionCliente();
            }
        });
        btnUbicacion.setVisibility(View.INVISIBLE);

        ArrayAdapter adapterCausaNoVenta = new ArrayAdapter<CausaNoVentaTO>(getActivity(), android.R.layout.simple_spinner_item);
        adapterCausaNoVenta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterCausaNoVenta.add(new CausaNoVentaTO("", "Seleccione una opción"));
        agregaCausaNoVenta(adapterCausaNoVenta);

        spinnerCausaNoVenta = (Spinner)view.findViewById(R.id.spinnerCausaNoVenta);
        spinnerCausaNoVenta.setAdapter(adapterCausaNoVenta);
        spinnerCausaNoVenta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                causaNoVenta();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtUbicacion = (TextView)view.findViewById(R.id.edtUbicacion);

        calendarFechaEntrega = Calendar.getInstance();
        calendarFechaEntrega.add(Calendar.DAY_OF_MONTH, 1);
        calendarFechaEntrega.set(Calendar.HOUR, 0);
        calendarFechaEntrega.set(Calendar.MINUTE, 0);
        calendarFechaEntrega.set(Calendar.SECOND, 0);
        calendarFechaEntrega.set(Calendar.MILLISECOND, 0);
        clienteAceptarCancelarListener.onFechaEntrega(calendarFechaEntrega.getTime());

        edtFechaEntrega = (TextView)view.findViewById(R.id.edtFechaEntrega);
        edtFechaEntrega.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                @SuppressLint("ValidFragment")
                DialogFragment datePickerFragment = new DatePickerFragment() {
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        calendarFechaEntrega.set(Calendar.YEAR, year);
                        calendarFechaEntrega.set(Calendar.MONTH, month);
                        calendarFechaEntrega.set(Calendar.DAY_OF_MONTH, day);

                        clienteAceptarCancelarListener.onFechaEntrega(calendarFechaEntrega.getTime());
                        edtFechaEntrega.setText(Fecha.getFormat("EEEE dd MMMM", calendarFechaEntrega.getTime()));
                    }
                };

                Bundle bundle = new Bundle();
                bundle.putSerializable("date", calendarFechaEntrega);

                datePickerFragment.setArguments(bundle);
                datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        edtFechaEntrega.setText(Fecha.getFormat("EEEE dd MMMM", calendarFechaEntrega.getTime()));

        clienteAceptarCancelarListener.onDireccionEntrega("");

        ArrayAdapter adapterDireccionEntrega = new ArrayAdapter<DireccionEntregaTO>(getActivity(), android.R.layout.simple_spinner_item);
        adapterDireccionEntrega.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterDireccionEntrega.add(new DireccionEntregaTO("", "Seleccione una opción"));
        agregaDireccionesEntrega(adapterDireccionEntrega);

        spinnerDireccionEntrega = (Spinner)view.findViewById(R.id.spinnerDireccionEntrega);
        spinnerDireccionEntrega.setAdapter(adapterDireccionEntrega);
        spinnerDireccionEntrega.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                direccionDeEntrega();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        esperaUbicacion();
    }

    private void esperaUbicacion() {
        super.onStart();

        String texto = btnAceptar.getText().toString();
        if (texto.startsWith("Espera"))
            return;

        btnAceptar.setText("Aceptar (999)");
        btnAceptar.setEnabled(false);

        try {
            tiempoEspera = Numero.getIntFromString(configuracionTO.parametros.get(Constantes.PARAMETRO_ESPERAUBICACION), 300);

            task.execute();

        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);

            Message.alert(getActivity(), "Error al ejecutar la tarea de Espera por Ubicacion "+e.getMessage());

            btnAceptar.setText("Aceptar");
            btnAceptar.setEnabled(true);
        }
    }

    private void ubicacionCliente() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constantes.EXTRA_CONFIGURACION, configuracionTO);
        bundle.putSerializable(Constantes.EXTRA_CLIENTE, clienteTO);

        //Intent intent = new Intent(getActivity(), UbicacionClienteActivity.class);
        //intent.putExtras(bundle);

        //startActivityForResult(intent, 0);
    }

    private void agregaDireccionesEntrega(ArrayAdapter adapterDireccionEntrega) {
        DatabaseServices ds = DatabaseOpenHelper.getInstance().getReadableDatabaseServices();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT cliente, direccion, descripcion ")
                    .append("FROM DireccionEntrega ")
                    .append("WHERE cliente = '").append(clienteTO.cliente).append("'");

            List<?> array = ds.collection(new DireccionEntregaTO(), sql.toString());

            for (Object direccionEntregaTO : array)
                adapterDireccionEntrega.add((DireccionEntregaTO)direccionEntregaTO);

        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        ds.close();
    }

    private void agregaCausaNoVenta(ArrayAdapter adapterCausaNoVenta) {
        DatabaseServices ds = DatabaseOpenHelper.getInstance().getReadableDatabaseServices();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT causa, descripcion ")
                    .append("FROM CausaNoVenta ");

            List<?> array = ds.collection(new CausaNoVentaTO(), sql.toString());

            for (Object causaNoVentaTO : array)
                adapterCausaNoVenta.add((CausaNoVentaTO)causaNoVentaTO);

        } catch(Exception e) {
            Log.e(LOGTAG, e.getMessage(), e);
        }
        ds.close();
    }

    private void direccionDeEntrega() {
        DireccionEntregaTO direccionEntregaTO = (DireccionEntregaTO)spinnerDireccionEntrega.getSelectedItem();
        clienteAceptarCancelarListener.onDireccionEntrega(direccionEntregaTO.direccion);
    }

    private void causaNoVenta() {
        final CausaNoVentaTO causaNoVentaTO = (CausaNoVentaTO)spinnerCausaNoVenta.getSelectedItem();
        if (causaNoVentaTO.causa.isEmpty())
            return;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        guardaCausaNoVenta(causaNoVentaTO);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        spinnerCausaNoVenta.setSelection(0);
                    default:
                        break;
                }
                dialog.dismiss();
            }
        };

        Message.question(getActivity(), dialogClickListener, "¿Esta seguro de registrar al cliente "
               +clienteTO.cliente+"-"+clienteTO.razonsocial+" Sin Venta?");
    }

    private void guardaCausaNoVenta(CausaNoVentaTO causaNoVentaTO) {
        clienteAceptarCancelarListener.onGuardarCasaNoVenta(causaNoVentaTO);
    }

    public void updateLocation(Location location) {
        if (location != null) {
            edtUbicacion.setText(location.toString());
        }
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}