package mx.com.sagaji.android.adapter;

import com.atcloud.android.util.Numero;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.DevolucionDetalleTO;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DevolucionesDetalleAdapter extends ArrayAdapter<DevolucionDetalleTO> {
    private LayoutInflater vi = null;

    public DevolucionesDetalleAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(R.layout.layout_devoluciones_detalles, null);
        DevolucionDetalleTO o = getItem(position);
        if (o != null) {
            setTextView(v, R.id.descripcion, o.descripcion);
            setTextView(v, R.id.documento, o.documento);
            setTextView(v, R.id.codigo, o.codigo);
            setTextView(v, R.id.piezas, Numero.getIntNumero(o.cantidad));
            setTextView(v, R.id.causa, o.causa);
            setTextView(v, R.id.descuento, Numero.getPorcentaje(o.descuento));
            setTextView(v, R.id.tipo, o.tipo);
        }
        return v;
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}
