package mx.com.sagaji.android.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atcloud.android.util.Numero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mx.com.sagaji.android.R;
import mx.com.sagaji.android.to.ClienteTO;

/**
 * Created by jbecerra.
 */
public class ClientesAdapter extends RecyclerView.Adapter<ClientesAdapter.ViewHolder> {
    public static String LOGTAG = ClientesAdapter.class.getCanonicalName();

    private Activity mActivity;
    private List<ClienteTO> mClientes;
    private HashMap<Integer, Boolean> mSelectedViews;
    private int selectedColor;
    private int nonSelectedColor;
    private boolean singleSelection;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public View getView() {
            return mView;
        }
    }

    public ClientesAdapter(Activity activity) {
        mActivity = activity;

        mSelectedViews = new HashMap<>();
        selectedColor = mActivity.getResources().getColor(R.color.colorGrayDark);
        nonSelectedColor = mActivity.getResources().getColor(android.R.color.background_light);
        singleSelection = true;
    }

    public void setClientes(List<ClienteTO> clientes) {
        mClientes = clientes;

        mSelectedViews = new HashMap<>();

        notifyDataSetChanged();
    }

    @Override
    public ClientesAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout_clientes, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = Integer.parseInt(getTextView(view, R.id.id).getText().toString());

                if (mSelectedViews.containsKey(position)) {
                    mSelectedViews.remove(position);
                } else {
                    if (singleSelection) {
                        RecyclerView recyclerView = (RecyclerView) parent;
                        for (int selectedPosition : mSelectedViews.keySet()) {
                            ClientesAdapter.ViewHolder viewHolder = (ClientesAdapter.ViewHolder) recyclerView
                                    .findViewHolderForLayoutPosition(selectedPosition);
                            if (viewHolder != null)
                                viewHolder.getView().setBackgroundColor(nonSelectedColor);
                        }
                        mSelectedViews.clear();
                    }
                    mSelectedViews.put(position, true);
                }
                view.setBackgroundColor(mSelectedViews.containsKey(position) ?
                        selectedColor : nonSelectedColor);
            }
        });

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        View view = viewHolder.getView();
        view.setBackgroundColor(mSelectedViews.containsKey(position) ?
                selectedColor : nonSelectedColor);

        ClienteTO to = mClientes.get(position);
        setTextView(view, R.id.id, String.valueOf(position));
        setTextView(view, R.id.orden, "1");
        setTextView(view, R.id.propietario, to.propietario);
        setTextView(view, R.id.razonsocial, to.razonsocial);
        setTextView(view, R.id.cliente, to.cliente);
        setTextView(view, R.id.saldo, Numero.getMoneda(to.mnsaldo));
    }

    @Override
    public int getItemCount() {
        return mClientes.size();
    }

    private void setTextView(View view, int resource, String value) {
        TextView textView = (TextView) view.findViewById(resource);
        if (textView != null)
            textView.setText(value);
    }

    private TextView getTextView(View view, int resource) {
        TextView textView = (TextView) view.findViewById(resource);
        return textView;
    }

    public List<ClienteTO> getSelectedRows() {
        ArrayList<ClienteTO> selected = new ArrayList<>();
        for (int position : mSelectedViews.keySet())
            selected.add(mClientes.get(position));
        return selected;
    }
}