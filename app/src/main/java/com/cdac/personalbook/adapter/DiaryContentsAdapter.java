package com.cdac.personalbook.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cdac.personalbook.POJO.DiaryContentsPojo;
import com.cdac.personalbook.R;
import com.cdac.personalbook.activities.DashboardActivity;
import com.cdac.personalbook.activities.MakeEntry;
import com.cdac.personalbook.common.AlertMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DiaryContentsAdapter extends RecyclerView.Adapter<DiaryContentsAdapter.ViewHolder> {
    private ArrayList<DiaryContentsPojo> arrayList;
    private Context context;
    public DiaryContentsAdapter(ArrayList<DiaryContentsPojo> arrayList, Context context){
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public DiaryContentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View listItem = layoutInflater.inflate(R.layout.diary_content_item, viewGroup, false);
        DiaryContentsAdapter.ViewHolder viewHolder = new DiaryContentsAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryContentsAdapter.ViewHolder viewHolder, int i) {
        final DiaryContentsPojo diaryContents = arrayList.get(i);
        viewHolder.title.setText(diaryContents.getTitle());
        viewHolder.content.setText(diaryContents.getContent());
        viewHolder.date.setText(diaryContents.getDate());
        viewHolder.contentItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MakeEntry.class);
                intent.putExtra("id", diaryContents.getId());
                context.startActivity(intent);
            }
        });
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertMessage.showMessageDialog((Activity)context, "Are You sure to delete this entry? ",
                        "yes", "no", new AlertMessage.YesNoListener() {
                            @Override
                            public void onDecision(boolean btnClicked) {
                                if (btnClicked){
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                                            .child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("data");
                                    databaseReference.child(diaryContents.getId()).removeValue();
                                }
                            }
                        });
            }
        });
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, date, content;
        MaterialCardView contentItem;
        ImageButton delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            date = itemView.findViewById(R.id.date);
            contentItem = itemView.findViewById(R.id.diary_item);
            delete = itemView.findViewById(R.id.delete);
        }
    }
}
