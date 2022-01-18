package com.example.myboardgames.control;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myboardgames.R;

public class ItemBGViewHolder extends RecyclerView.ViewHolder {

    // Widgets do card item bg
    public ImageView cardImage;
    public TextView cardName;
    public View viewBG;

    public ItemBGViewHolder(@NonNull View itemView) {
        super(itemView);

        // Inicializar
        cardImage = itemView.findViewById(R.id.cardImage);
        cardName = itemView.findViewById(R.id.cardName);
        viewBG = itemView;
    }
}
