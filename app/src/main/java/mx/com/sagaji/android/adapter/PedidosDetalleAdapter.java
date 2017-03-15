package mx.com.sagaji.android.adapter;

import com.atcloud.android.util.Numero;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.PedidoDetalleTO;

public class PedidosDetalleAdapter extends ArrayAdapter<PedidoDetalleTO> {
    private LayoutInflater vi = null;

    public PedidosDetalleAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = vi.inflate(R.layout.layout_pedidos_detalles, null);
        PedidoDetalleTO o = getItem(position);

        int colorCodigo = Color.WHITE;
        int colorPiezasSurtir = Color.WHITE;
        if (o.linea.compareTo("LE")==0) {
            colorCodigo = Color.rgb(255, 255, 0);
            colorPiezasSurtir = Color.rgb(255, 255, 0);
        }
        if (o.promocion) {
            colorCodigo = Color.rgb(0, 255, 0);
            colorPiezasSurtir = Color.rgb(0, 255, 0);
        }
        if (o.status.compareTo("03")==0) {
            colorCodigo = Color.rgb(21, 143, 234);
            colorPiezasSurtir = Color.rgb(21, 143, 234);
        }

        if (o != null) {
            setTextView(v, R.id.descripcion, o.descripcion, Color.WHITE);
            setTextView(v, R.id.codigo, o.codigo, colorCodigo);
            setTextView(v, R.id.linea, o.linea, Color.WHITE);
            setTextView(v, R.id.cantidad, Numero.getIntNumero(o.cantidad), Color.WHITE);
            setTextView(v, R.id.cantidadsurtir, Numero.getIntNumero(o.cantidadsurtir), colorPiezasSurtir);
            setTextView(v, R.id.precio, Numero.getMoneda(o.precio), Color.WHITE);
            setTextView(v, R.id.totalsurtir, Numero.getMoneda(o.totalsurtir), Color.WHITE);
            setTextView(v, R.id.numero, String.valueOf(position + 1), Color.WHITE);
        }
        return v;
    }

    private void setTextView(View view, int resource, String value, int color) {
        TextView textView = (TextView)view.findViewById(resource);
        textView.setBackgroundColor(color);
        if (textView != null)
            textView.setText(value);
    }
}
