package hr.cizmic.seebanking.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import hr.cizmic.seebanking.data.local.entity.AccountEntity;
import hr.cizmic.seebanking.databinding.ItemAccountBinding;
import hr.cizmic.seebanking.util.MoneyUtils;

// shows list of accs in recyclerview
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.VH> {

    // callback interface so fragment knows when acc is clicked
    public interface Listener {
        void onAccountClicked(AccountEntity account);
    }

    private final Listener listener;
    private final List<AccountEntity> items = new ArrayList<>();

    public AccountAdapter(Listener listener) {
        this.listener = listener;
    }

    // update list when new data comes in
    public void submit(List<AccountEntity> accounts) {
        items.clear();
        if (accounts != null) items.addAll(accounts);
        notifyDataSetChanged(); // tell list to refresh
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate item layout
        ItemAccountBinding b = ItemAccountBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AccountEntity a = items.get(position);
        // show acc name or fallback to "Account 123"
        holder.b.tvName.setText(a.name != null ? a.name : ("Account " + a.id));
        holder.b.tvIban.setText(a.iban != null ? a.iban : "");
        // format balance with 2 decimals
        String bal = MoneyUtils.format(a.balance);
        holder.b.tvBalance.setText(bal + " " + (a.currency == null ? "" : a.currency));
        // when user taps item, notify listener
        holder.b.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onAccountClicked(a);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // helper to get acc at position
    public AccountEntity getItem(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    // viewholder to hold item views
    static class VH extends RecyclerView.ViewHolder {
        final ItemAccountBinding b;
        VH(ItemAccountBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
