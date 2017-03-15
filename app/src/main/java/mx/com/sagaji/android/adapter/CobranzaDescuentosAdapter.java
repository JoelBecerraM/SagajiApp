package mx.com.sagaji.android.adapter;

import com.atcloud.android.util.Numero;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.DescuentoCobranzaTO;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CobranzaDescuentosAdapter extends ArrayAdapter<DescuentoCobranzaTO> {
    private LayoutInflater vi = null;

    public CobranzaDescuentosAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(R.layout.layout_cobranza_descuentos_detalles, null);
        DescuentoCobranzaTO o = getItem(position);
        if (o != null) {
            //setTextView(v, R.id.linea, o.linea);
            setTextView(v, R.id.prdescuento, Numero.getPorcentaje(o.prdescuento));
            setTextView(v, R.id.prdescuentomax, Numero.getPorcentaje(o.prdescuentomax));
            setTextView(v, R.id.plazo, Numero.getIntNumero(o.plazo));
            setTextView(v, R.id.plazomax, Numero.getIntNumero(o.plazomax));
            setTextView(v, R.id.monto, Numero.getMoneda(o.monto));
            //setTextView(v, R.id.montomax, Numero.getMoneda(o.montomax));
            setTextView(v, R.id.identificador, o.identificador);
        }
        return v;
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}
