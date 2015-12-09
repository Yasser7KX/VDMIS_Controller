package com.jollaman999.remotecmd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

@SuppressWarnings("unused")
public class Command_Shutdown extends Fragment implements View.OnClickListener{

    String selected_os;

    boolean is_directly;

    RadioButton rb_immediately;
    RadioButton rb_directly;

    EditText et_hour;
    EditText et_min;
    EditText et_sec;

    Button btn_shutdown;
    Button btn_restart;
    Button btn_cancel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.command_content_shutdown, null);

        Spinner spinner = (Spinner) root.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.command_shutdown_os_array, R.layout.command_shutdown_sppiner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_os = parent.getItemAtPosition(position).toString();

                if (!selected_os.equals("Windows")) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                    ad.setMessage(getString(R.string.command_shutdown_dialog_msg));
                    ad.setTitle(getString(R.string.command_shutdown_dialog_title));
                    ad.setPositiveButton(getString(R.string.command_shutdown_dislog_ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    ad.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        rb_immediately = (RadioButton) root.findViewById(R.id.rb_immediately);
        rb_directly = (RadioButton) root.findViewById(R.id.rb_directly);

        is_directly = true;
        rb_directly.setChecked(true);
        rb_immediately.setOnClickListener(this);
        rb_directly.setOnClickListener(this);

        et_hour = (EditText) root.findViewById(R.id.et_hour);
        et_min = (EditText) root.findViewById(R.id.et_min);
        et_sec = (EditText) root.findViewById(R.id.et_sec);

        class time_touch_listner implements View.OnFocusChangeListener {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    is_directly = true;
                    rb_directly.setChecked(true);
                }
            }
        }

        time_touch_listner ttl = new time_touch_listner();
        et_hour.setOnFocusChangeListener(ttl);
        et_min.setOnFocusChangeListener(ttl);
        et_sec.setOnFocusChangeListener(ttl);

        btn_shutdown = (Button) root.findViewById(R.id.btn_shutdown);
        btn_restart = (Button) root.findViewById(R.id.btn_restart);
        btn_cancel = (Button) root.findViewById(R.id.btn_cancel);

        class shutdown_onClick_listener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                int total = 0;
                char param = 'r';

                switch (v.getId()) {
                    case R.id.btn_shutdown:
                        if (selected_os.equals("Windows")) {
                            param = 's';
                        } else {
                            param = 'h';
                        }
                        break;
                    case R.id.btn_restart:
                        param = 'r';
                        break;
                }

                if (is_directly) {
                    total = getTime();

                    if (selected_os.equals("Windows")) {
                        Socket_Control.input = "shutdown -" + param + " -t " + total;
                    } else {
                        Socket_Control.input = "sudo shutdown -" + param + " +" + (total / 60);
                    }
                } else {
                    if (selected_os.equals("Windows")) {
                        Socket_Control.input = "shutdown -" + param + " -t " + 0;
                    } else {
                        Socket_Control.input = "sudo shutdown -" + param + " now";
                    }
                }

                Toast.makeText(getActivity(),
                        selected_os + ": " + Socket_Control.input,
                        Toast.LENGTH_SHORT).show();
                Socket_Control.SendShutdownCommand();
            }
        };

        shutdown_onClick_listener sol = new shutdown_onClick_listener();
        btn_shutdown.setOnClickListener(sol);
        btn_restart.setOnClickListener(sol);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_os.equals("Windows")) {
                    Socket_Control.input = "shutdown -a";
                } else {
                    Socket_Control.input = "sudo shutdown -c";
                }

                Toast.makeText(getActivity(),
                        selected_os + ": " + Socket_Control.input,
                        Toast.LENGTH_SHORT).show();
                Socket_Control.SendShutdownCommand();
            }
        });

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_immediately:
                is_directly = false;
                et_hour.setText("");
                et_min.setText("");
                et_sec.setText("");
                break;
            case R.id.rb_directly:
                is_directly = true;
                break;
        }
    }

    int getTime() {
        int h, m, s;

        if (et_hour.getText() == null || et_hour.getText().toString().equals("")) {
            h = 0;
        } else {
            h = Integer.parseInt(et_hour.getText().toString()) * 3600;
        }

        if (et_min.getText() == null || et_min.getText().toString().equals("")) {
            m = 0;
        } else {
            m = Integer.parseInt(et_min.getText().toString()) * 60;
        }

        if (et_sec.getText() == null || et_sec.getText().toString().equals("") ||
                !selected_os.equals("Windows")) {
            s = 0;
        } else {
            s = Integer.parseInt(et_sec.getText().toString());
        }

        return h + m + s;
    }
}
