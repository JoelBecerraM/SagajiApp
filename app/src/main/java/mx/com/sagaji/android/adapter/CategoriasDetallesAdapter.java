package mx.com.sagaji.android.adapter;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.CategoriaDetalleTO;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CategoriasDetallesAdapter extends ArrayAdapter<CategoriaDetalleTO> {
    private LayoutInflater vi = null;

    public CategoriasDetallesAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(R.layout.layout_categorias_detalles, null);
        CategoriaDetalleTO o = getItem(position);
        if (o != null) {
            setTextView(v, R.id.codigo, o.codigo);
            setTextView(v, R.id.descripcion, o.descripcion);
        }
        return v;
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}
