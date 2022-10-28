package com.example.walkingmate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class ChatFragment extends Fragment {

    Context mcxt;
    LinearLayout linearLayout;

    public ChatFragment(Context mcxt){
        this.mcxt=mcxt;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_chat, container, false);
        linearLayout=root.findViewById(R.id.chatToplayout);

        Button gomainbtn=root.findViewById(R.id.gomain);
        gomainbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mcxt,MainActivity.class));
            }
        });


        return root;
    }
}