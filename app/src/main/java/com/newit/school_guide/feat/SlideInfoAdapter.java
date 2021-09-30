package com.newit.school_guide.feat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.newit.school_guide.R;
import com.newit.school_guide.feat.model.Info;

import java.util.List;

public class SlideInfoAdapter extends RecyclerView.Adapter<SlideInfoAdapter.ViewHolder> {

    private List<Info> data;

    private IClickDescription iClickDescription;

    public SlideInfoAdapter(List<Info> data) {
        this.data = data;
    }

    public void setiClickDescription(IClickDescription iClickDescription) {
        this.iClickDescription = iClickDescription;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_shop_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(data.get(position).getThumbnail()).placeholder(R.drawable.err_picture)
                .into(holder.image);
        holder.tvDescription.setText(data.get(position).getTitle());
        holder.tvDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(iClickDescription != null){
                     iClickDescription.onClickDetail(data.get(position));
                 }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    void setData(List<Info> data){
        this.data = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView tvDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            tvDescription = itemView.findViewById(R.id.tvDescription);

        }
    }

    public interface IClickDescription{
        void onClickDetail(Info shop);
    }
}
