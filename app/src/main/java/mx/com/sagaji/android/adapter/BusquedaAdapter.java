package mx.com.sagaji.android.adapter;

import com.atcloud.android.util.Numero;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.BusquedaTO;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BusquedaAdapter extends ArrayAdapter<BusquedaTO> {
    private int textViewResourceId = 0;
    private LayoutInflater vi = null;

    public BusquedaAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        this.textViewResourceId = textViewResourceId;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(textViewResourceId, null);
        BusquedaTO o = getItem(position);
        if (o != null) {
            setTextView(v, R.id.descripcion, o.descripcion);
            setTextView(v, R.id.unidadmedida, o.unidadmedida);
            setTextView(v, R.id.codigo, o.codigo);
            setTextView(v, R.id.precio, Numero.getMoneda(o.precio));
            setTextView(v, R.id.linea, o.linea);
        }
        return v;
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}