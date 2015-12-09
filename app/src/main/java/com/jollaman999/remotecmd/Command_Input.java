package com.jollaman999.remotecmd;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Command_Input extends Fragment {

    ViewGroup root;

    EditText et_input;
    static Button btn_send;
    static EditText et_result;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        root = (ViewGroup) inflater.inflate(R.layout.command_content_input, null);

        et_input = (EditText) root.findViewById(R.id.et_input);
        btn_send = (Button) root.findViewById(R.id.btn_send);
        et_result = (EditText) root.findViewById(R.id.et_result);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_input.getText() == null ||
                        et_input.getText().toString().equals("")) {
                    Toast.makeText(getActivity(),
                            R.string.command_input_noinput,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Socket_Control.input = et_input.getText().toString();
                Socket_Control.SendCommand();
            }
        });

        return root;
    }
}
