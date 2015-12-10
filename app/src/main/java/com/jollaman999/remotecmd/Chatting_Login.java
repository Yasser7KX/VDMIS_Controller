package com.jollaman999.remotecmd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class Chatting_Login extends Fragment {

    static EditText et_chat_login_name;
    Button btn_chat_login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.chatting_login, null);

        et_chat_login_name = (EditText) root.findViewById(R.id.et_chat_login_name);
        btn_chat_login = (Button) root.findViewById(R.id.btn_chat_login);

        et_chat_login_name.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    RunChattingMain();
                    return true;
                }
                return false;
            }
        });
        btn_chat_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RunChattingMain();
            }
        });

        return root;
    }

    void RunChattingMain() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), Chatting_Main.class);
        startActivity(intent);
    }
}
