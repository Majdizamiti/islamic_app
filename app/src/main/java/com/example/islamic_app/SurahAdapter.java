package com.example.islamic_app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.islamic_app.databinding.ItemSurahBinding;
import java.util.List;

public class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.SurahViewHolder> {

    // 1. Define the Interface
    public interface OnSurahClickListener {
        void onSurahClick(Surah surah);
    }

    private final List<Surah> surahList;
    private final OnSurahClickListener listener;

    // 2. Pass the listener through the constructor
    public SurahAdapter(List<Surah> surahList, OnSurahClickListener listener) {
        this.surahList = surahList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SurahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSurahBinding binding = ItemSurahBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SurahViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SurahViewHolder holder, int position) {
        Surah surah = surahList.get(position);
        holder.bind(surah, listener);
    }

    @Override
    public int getItemCount() {
        return surahList.size();
    }

    public static class SurahViewHolder extends RecyclerView.ViewHolder {
        private final ItemSurahBinding binding;

        public SurahViewHolder(@NonNull ItemSurahBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Surah surah, final OnSurahClickListener listener) {
            binding.tvSurahNumber.setText(surah.getNumber());
            binding.tvSurahArabic.setText(surah.getNameArabic());
            binding.tvSurahEnglish.setText(surah.getNameEnglish());
            binding.tvSurahType.setText(surah.getType());

            // Apply dynamic colors
            if ("مدنية".equals(surah.getType())) {
                binding.ivSurahStar.setColorFilter(itemView.getContext().getColor(R.color.medinan_text));
                binding.tvSurahType.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.medinan_yellow));
                binding.tvSurahType.setTextColor(itemView.getContext().getColor(R.color.medinan_text));
            } else {
                binding.ivSurahStar.setColorFilter(itemView.getContext().getColor(R.color.meccan_text));
                binding.tvSurahType.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.meccan_green));
                binding.tvSurahType.setTextColor(itemView.getContext().getColor(R.color.meccan_text));
            }

            // 3. Set the Click Listener on the whole item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSurahClick(surah);
                }
            });
        }
    }
}