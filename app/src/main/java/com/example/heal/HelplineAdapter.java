package com.example.heal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HelplineAdapter extends RecyclerView.Adapter<HelplineAdapter.HelplineViewHolder> {

    private Context context;
    private List<Helpline> helplineList;

    public HelplineAdapter(Context context, List<Helpline> helplineList) {
        this.context = context;
        this.helplineList = helplineList;
    }

    @NonNull
    @Override
    public HelplineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_helpline, parent, false);
        return new HelplineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HelplineViewHolder holder, int position) {
        Helpline helpline = helplineList.get(position);
        holder.tvName.setText(helpline.getName());
        holder.tvNumber.setText(helpline.getNumber());
        holder.tvDescription.setText(helpline.getDescription());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + helpline.getNumber()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return helplineList.size();
    }

    public static class HelplineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvDescription;

        public HelplineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHelplineName);
            tvNumber = itemView.findViewById(R.id.tvHelplineNumber);
            tvDescription = itemView.findViewById(R.id.tvHelplineDescription);
        }
    }
}
