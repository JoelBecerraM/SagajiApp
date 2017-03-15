package mx.com.sagaji.android.adapter;

import java.util.List;
import com.atcloud.android.dao.engine.DatabaseList;
import com.atcloud.android.util.Fecha;
import com.atcloud.android.util.Numero;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import mx.com.sagaji.android.R;

public class AnterioresAdapter extends ArrayAdapter<DatabaseList> {
    private LayoutInflater vi = null;
    private List<DatabaseList> items;

    public AnterioresAdapter(Context context, int textViewResourceId, List<DatabaseList> items) {
        super(context, textViewResourceId, items);

        this.items = items;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(R.layout.layout_anteriores_detalles, null);
        DatabaseList o = items.get(position);
        if (o != null) {
            setTextView(v, R.id.folio, Numero.getIntNumero(o.getFolio()));
            setTextView(v, R.id.fecha, Fecha.getFechaHora(o.getFecha()));
            setTextView(v, R.id.status, o.getStatus());
            setTextView(v, R.id.tipo, o.getTipo());
            setTextView(v, R.id.cliente, o.getCliente());
            setTextView(v, R.id.nombre, o.getNombre());
            setTextView(v, R.id.lineas, Numero.getIntNumero(o.getLineas()));
            setTextView(v, R.id.piezas, Numero.getIntNumero(o.getPiezas()));
            setTextView(v, R.id.total, Numero.getMoneda(o.getTotal()));
        }
        return v;
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView)view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }
}
