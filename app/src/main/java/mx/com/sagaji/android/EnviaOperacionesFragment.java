package mx.com.sagaji.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.atcloud.android.util.Message;

import mx.com.sagaji.android.dao.entity.CobranzaDAO;
import mx.com.sagaji.android.dao.entity.DevolucionDAO;
import mx.com.sagaji.android.dao.entity.PedidoDAO;
import mx.com.sagaji.android.dao.entity.UbicacionDAO;
import mx.com.sagaji.android.dao.entity.VisitaDAO;
import mx.com.sagaji.android.delegate.TaskDelegate;
import mx.com.sagaji.android.listener.OnEnviaOperacionesListener;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Constantes;
import mx.com.sagaji.android.util.Sincronizacion;

public class EnviaOperacionesFragment extends Fragment implements TaskDelegate {
    private ConfiguracionTO configuracionTO;
    private View view;
    private EditText edtLog;
    private EditText edtOperaciones;
    private Button btnInicia;

    public void setParametros(ConfiguracionTO configuracionTO) {
        this.configuracionTO = configuracionTO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enviaoperaciones, container, false);
        setUpView(view);
        return view;
    }

    OnEnviaOperacionesListener enviaOperacionesListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            enviaOperacionesListener = (OnEnviaOperacionesListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement OnEnviaOperacionesListener");
        }
    }

    private void setUpView(View view) {
        this.view = view;

        edtLog = (EditText) view.findViewById(R.id.edtLog);
        edtOperaciones = (EditText) view.findViewById(R.id.edtOperaciones);

        edtOperaciones = (EditText)view.findViewById(R.id.edtOperaciones);

        btnInicia = (Button)view.findViewById(R.id.btnInicia);
        btnInicia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                enviaOperaciones();
            }
        });

        setUpOperaciones();
    }

    private void setUpOperaciones() {
        long pendientes = 0;
        StringBuilder operaciones = new StringBuilder();

        pendientes = enviaOperacionesListener.onCuentaRegistros(new PedidoDAO());
        activaOperacion(pendientes, R.id.chkPedidos);
        operaciones.append("(").append(pendientes).append(") pedidos por enviar.").append("\n");

        pendientes = enviaOperacionesListener.onCuentaRegistros(new DevolucionDAO());
        activaOperacion(pendientes, R.id.chkDevoluciones);
        operaciones.append("(").append(pendientes).append(") devoluciones por enviar.").append("\n");

        pendientes = enviaOperacionesListener.onCuentaRegistros(new CobranzaDAO());
        activaOperacion(pendientes, R.id.chkCobranza);
        operaciones.append("(").append(pendientes).append(") cobranzas por enviar.").append("\n");

        pendientes = enviaOperacionesListener.onCuentaRegistros(new VisitaDAO());
        activaOperacion(pendientes, R.id.chkVisitas);
        operaciones.append("(").append(pendientes).append(") visitas por enviar.").append("\n");

        pendientes = 0l; //enviaOperacionesListener.onCuentaRegistros(new MensajeDAO());
        activaOperacion(pendientes, R.id.chkMensajes);
        operaciones.append("(").append(pendientes).append(") mensajes por enviar.").append("\n");

        pendientes = enviaOperacionesListener.onCuentaRegistros(new UbicacionDAO());
        activaOperacion(pendientes, R.id.chkUbicaciones);
        operaciones.append("(").append(pendientes).append(") ubicaciones por enviar.").append("\n");

        edtOperaciones.setText(operaciones.toString());
    }

    private void activaOperacion(long pendientes, int id) {
        CheckBox checkBox = (CheckBox)view.findViewById(id);
        checkBox.setEnabled(pendientes > 0l);
        checkBox.setChecked(pendientes > 0l);
    }

    private boolean obtenCheck(int id) {
        CheckBox checkBox = (CheckBox)view.findViewById(id);
        return checkBox.isChecked();
    }

    private void enviaOperaciones() {
        Sincronizacion sincronizacion = new Sincronizacion();
        if (!sincronizacion.hayConexionAInternet(getActivity())) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            enviaOperaciones(true);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                        default:
                            break;
                    }
                    dialog.dismiss();
                }
            };

            Message.question(getActivity(), dialogClickListener,
                    "No se ha detectado ninguna conexión a Internet, ¿realmente desea continuar?");
        } else {
            enviaOperaciones(true);
        }
    }

    private void enviaOperaciones(boolean confirmado) {
        btnInicia.setEnabled(false);

        enviaOperacionesListener.onIniciaEnvioOperaciones();
        long operaciones = 0;

        if (obtenCheck(R.id.chkPedidos))
            operaciones += Constantes.ENVIAR_PEDIDOS;
        if (obtenCheck(R.id.chkDevoluciones))
            operaciones += Constantes.ENVIAR_DEVOLUCIONES;
        if (obtenCheck(R.id.chkCobranza))
            operaciones += Constantes.ENVIAR_COBRANZAS;
        if (obtenCheck(R.id.chkVisitas))
            operaciones += Constantes.ENVIAR_VISITAS;
        if (obtenCheck(R.id.chkMensajes))
            operaciones += Constantes.ENVIAR_MENSAJES;
        if (obtenCheck(R.id.chkUbicaciones))
            operaciones += Constantes.ENVIAR_UBICACIONES;

        EnviaOperacionesTask enviaOperacionesTask
                = new EnviaOperacionesTask(getActivity(), configuracionTO, edtLog, operaciones);
        enviaOperacionesTask.setTaskDelegate(this);
        enviaOperacionesTask.execute();
    }

    @Override
    public void taskCompletionResult(int result) {
        btnInicia.setEnabled(true);

        if (result == getActivity().RESULT_OK) {
            setUpOperaciones();
        }

        enviaOperacionesListener.onTerminaEnvioOperaciones();
    }
}