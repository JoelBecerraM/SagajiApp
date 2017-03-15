package mx.com.sagaji.android;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import mx.com.sagaji.android.to.ConfiguracionTO;

/**
 * Created by jbecerra.
 */
public class AcercaDeFragment extends Fragment {
    public static String LOGTAG = AcercaDeFragment.class.getCanonicalName();

    private ConfiguracionTO configuracionTO;
    private EditText edtVersion;
    private Handler handler = new Handler();

    public void setParametros(ConfiguracionTO configuracionTO) {
        this.configuracionTO = configuracionTO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acercade, container, false);
        setUpView(view);
        return view;
    }

    private void setUpView(View view) {
        String aplicacion = getActivity().getResources().getString(R.string.app_name);
        StringBuilder versionText = new StringBuilder();
        versionText
                .append("<h1>").append(aplicacion).append("</h1>")
                .append("<br>")
                .append("<h4>Versión 0.0.2 - 2016-03-14</h4>")
                .append("<h5>- Se consultan en linea las existencias.</h5>")
                .append("<br>")
                .append("<h4>Versión 0.0.1 - 2016-03-01</h4>")
                .append("<h5>- Se crea la primera versión.</h5>")
                .append("<br>");

        edtVersion = (EditText) view.findViewById(R.id.edtVersion);
        edtVersion.setText(Html.fromHtml(versionText.toString()));

        Button btnEnviaInfo = (Button) view.findViewById(R.id.btnEnviaInfo);
        btnEnviaInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.setEnabled(false);
                enviaInformacion();
            }
        });

        Button btnActualizaVersion = (Button) view.findViewById(R.id.btnActualizaVersion);
        btnActualizaVersion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.setEnabled(false);
                actualizaVersión();
            }
        });
    }

    private void enviaInformacion() {
        //EnviaInformacionTask enviaInformacionTask = new EnviaInformacionTask(getActivity(), configuracionTO, edtVersion);
        //enviaInformacionTask.execute();
    }

    private void actualizaVersión() {
        //ActualizaVersionTask actualizaVersionTask = new ActualizaVersionTask(getActivity(), configuracionTO, edtVersion);
        //actualizaVersionTask.execute();
    }
}