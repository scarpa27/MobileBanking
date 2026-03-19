package hr.cizmic.seebanking.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import hr.cizmic.seebanking.data.local.entity.TransactionEntity;
import hr.cizmic.seebanking.databinding.ItemTransactionBinding;
import hr.cizmic.seebanking.util.MoneyUtils;

// shows list of tx in recyclerview
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    private final List<TransactionEntity> items = new ArrayList<>();

    // update list when new data comes in
    public void submit(List<TransactionEntity> txs) {
        items.clear();
        if (txs != null) items.addAll(txs);
        notifyDataSetChanged(); // refresh list
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate item layout
        ItemTransactionBinding b = ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TransactionEntity t = items.get(position);

        // figure out if it's incoming or outgoing
        boolean incoming = "IN".equalsIgnoreCase(t.direction);
        boolean outgoing = "OUT".equalsIgnoreCase(t.direction);
        String sign = incoming ? "+" : (outgoing ? "-" : "");
        String amt = MoneyUtils.format(t.amount);
        holder.b.tvAmount.setText(sign + amt + " " + (t.currency == null ? "" : t.currency));

        // show who sent or received the money (name, phone, or user id
        String who = t.counterpartyName != null ? t.counterpartyName :
                (t.counterpartyMobileNumber != null ? t.counterpartyMobileNumber :
                        (t.counterpartyUserId != null ? t.counterpartyUserId : ""));
        if (who == null || who.isBlank()) {
            who = "Unknown";
        }
        String prefix = incoming ? "From: " : (outgoing ? "To: " : "Counterparty: ");
        holder.b.tvCounterparty.setText(prefix + who);

        holder.b.tvNote.setText(t.note != null ? t.note : "");
        holder.b.tvBalanceAfter.setText(balanceText(t.balanceAfter, t.currency));
        holder.b.tvDirection.setText(incoming ? "IN" : (outgoing ? "OUT" : "TX"));
        // color-code direction (green for incoming, red for outgoing
        int dirColor = ContextCompat.getColor(holder.itemView.getContext(),
                incoming ? hr.cizmic.seebanking.R.color.tx_incoming :
                        (outgoing ? hr.cizmic.seebanking.R.color.tx_outgoing : hr.cizmic.seebanking.R.color.text_secondary));
        holder.b.tvDirection.setTextColor(dirColor);

        // show date/time if available
        if (t.createdAtEpochMs != null) {
            holder.b.tvDate.setText(DateFormat.getDateTimeInstance().format(new Date(t.createdAtEpochMs)));
        } else {
            holder.b.tvDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // viewholder to hold item views
    static class VH extends RecyclerView.ViewHolder {
        final ItemTransactionBinding b;
        VH(ItemTransactionBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    // format balance after tx
    private static String balanceText(java.math.BigDecimal balanceAfter, String currency) {
        String bal = MoneyUtils.formatOrDash(balanceAfter);
        return "Balance after: " + bal + " " + (currency == null ? "" : currency);
    }
}
