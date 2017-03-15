package mx.com.sagaji.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.atcloud.android.util.Message;

import mx.com.sagaji.android.delegate.TaskDelegate;
import mx.com.sagaji.android.listener.OnSincronizacionListener;
import mx.com.sagaji.android.to.ConfiguracionTO;
import mx.com.sagaji.android.util.Sincronizacion;

/**
 * Created by jbecerra.
 */
public class SincronizacionFragment extends Fragment implements TaskDelegate {
    public static String LOGTAG = SincronizacionFragment.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;
    private EditText edtLog;
    private CheckBox chkBaseDeDatos;
    private Button btnInicia;
    private boolean actualizaVersion;

    public void setParametros(ConfiguracionTO configuracionTO) {
        this.configuracionTO = configuracionTO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sincronizacion, container, false);
        setUpView(view);
        return view;
    }

    OnSincronizacionListener sincronizacionListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            sincronizacionListener = (OnSincronizacionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSincronizacionListener");
        }
    }

    private void setUpView(View view) {
        edtLog = (EditText) view.findViewById(R.id.edtLog);

        btnInicia = (Button) view.findViewById(R.id.btnInicia);
        btnInicia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                iniciaSincronizacionCatalogos();
            }
        });
    }

    private void iniciaSincronizacionCatalogos() {
        Sincronizacion sincronizacion = new Sincronizacion();
        if (!sincronizacion.hayConexionAInternet(getActivity())) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            iniciaSincronizacionCatalogos(true);
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
            iniciaSincronizacionCatalogos(true);
        }
    }

    private void iniciaSincronizacionCatalogos(boolean confirmado) {
        btnInicia.setEnabled(false);

        sincronizacionListener.onIniciaSincronizacion();
        actualizaVersion = false;

        SincronizacionTask sincronizacionCatalogosTask
                = new SincronizacionTask(getActivity(), configuracionTO, edtLog);
        sincronizacionCatalogosTask.setTaskDelegate(this);
        sincronizacionCatalogosTask.execute();
    }

    private void iniciaActualizaVersion() {
        actualizaVersion = true;

        ActualizaVersionTask actualizaVersionTask = new ActualizaVersionTask(getActivity(), configuracionTO, edtLog);
        actualizaVersionTask.setTaskDelegate(this);
        actualizaVersionTask.execute();
    }

    @Override
    public void taskCompletionResult(int result) {
        btnInicia.setEnabled(true);

        if (actualizaVersion) {
            if (result == getActivity().RESULT_OK) {
                Message.mensaje(getActivity(), "Sincronización de Catálogos terminada correctamente.");
                sincronizacionListener.onTerminaSincronizacion();
            }

        } else {
            if (result == getActivity().RESULT_OK) {
                iniciaActualizaVersion();
            } else {
                Message.alert(getActivity(), "Ocurrieron errores en la Sincronización de Catálogos.");
                sincronizacionListener.onTerminaSincronizacion();
            }
        }
    }
}