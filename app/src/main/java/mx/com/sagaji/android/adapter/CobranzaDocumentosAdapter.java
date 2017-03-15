package mx.com.sagaji.android.adapter;

import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Numero;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.FacturaCobranzaTO;

public class CobranzaDocumentosAdapter extends ArrayAdapter<FacturaCobranzaTO> {
    private LayoutInflater vi = null;

    public CobranzaDocumentosAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(R.layout.layout_cobranza_documentos_detalles, null);
        FacturaCobranzaTO o = getItem(position);
        if (o != null) {
            setTextView(v, R.id.documento, o.Documento);
            setTextView(v, R.id.fechadocumento, Fecha.getFecha(o.Fecha_Factura));
            setTextView(v, R.id.fechavencimiento, Fecha.getFecha(o.Fecha_Vence));
            setTextView(v, R.id.plazo, Numero.getIntNumero(o.PLAZO));
            setTextView(v, R.id.diastranscurridos, Numero.getIntNumero(o.DiasTrascurridos));
            setTextView(v, R.id.saldo, Numero.getMoneda(o.Saldo));
            setTextView(v, R.id.cobrado, Numero.getMoneda(o.MontoOrginal - o.Saldo));
            setTextView(v, R.id.total, Numero.getMoneda(o.MontoOrginal));
        }
        return v;
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}