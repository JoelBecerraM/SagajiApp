package mx.com.sagaji.android.adapter;

import com.atcloud.android.util.Numero;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.CobranzaDetalleTO;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CobranzaDetalleAdapter extends ArrayAdapter<CobranzaDetalleTO> {
    private LayoutInflater vi = null;

    public CobranzaDetalleAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(R.layout.layout_cobranza_detalles, null);
        CobranzaDetalleTO o = getItem(position);
        if (o != null) {
            setTextView(v, R.id.documento, o.documento);
            setTextView(v, R.id.tipopago, o.getTipoPago());
            setTextView(v, R.id.referencia, o.referencia);
            setTextView(v, R.id.pago, Numero.getMoneda(o.pago));
            setTextView(v, R.id.banco, o.banco);
            setTextView(v, R.id.fechacobro, o.fechacobro);
            setTextView(v, R.id.prdescuento, Numero.getPorcentaje(o.prdescuento));
            setTextView(v, R.id.descuento, Numero.getMoneda(o.descuento));
        }
        return v;
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}