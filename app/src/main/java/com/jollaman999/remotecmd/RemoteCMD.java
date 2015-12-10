package com.jollaman999.remotecmd;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

public class RemoteCMD extends AppCompatActivity {

    EditText et_main_address;
    static EditText et_main_password;
    static Button bt_main_connect;
    static TextView tv_main_error_msg;

    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        context = getApplicationContext();

        et_main_address = (EditText) findViewById(R.id.et_main_address);
        et_main_password = (EditText) findViewById(R.id.et_main_password);
        bt_main_connect = (Button) findViewById(R.id.bt_main_connect);
        tv_main_error_msg = (TextView) findViewById(R.id.tv_main_error_msg);

        OnKey mOnKey = new OnKey();
        et_main_address.setOnKeyListener(mOnKey);
        et_main_password.setOnKeyListener(mOnKey);

        bt_main_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect();
            }
        });

        et_main_password.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        Socket_Control.closeSocket();
        super.onDestroy();
    }

    class OnKey implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                Connect();
                return true;
            }
            return false;
        }
    }

    void Connect() {
        // If there is no input of address
        if (et_main_address.getText() == null ||
                et_main_address.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.main_no_address),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (Socket_Control.is_connected) {
            Socket_Control.ConnectedControl();
        } else {
            Socket_Control.setSocket(et_main_address.getText().toString());
        }
    }
}
