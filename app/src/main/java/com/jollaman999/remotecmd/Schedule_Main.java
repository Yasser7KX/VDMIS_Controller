package com.jollaman999.remotecmd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("unused")
public class Schedule_Main extends Fragment {

    ViewGroup root;

    TextView tv_start_time;

    ListView lv_command_list;
    ArrayAdapter<String> mArrayAdapter;

    Button btn_schedule_reset;

    EditText et_schedule_command;
    EditText et_schedule_min;
    EditText et_schedule_sec;
    Button btn_schedule_add;

    static boolean is_schedule_canceled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        root = (ViewGroup) inflater.inflate(R.layout.schedule__main, null);

        lv_command_list = (ListView) root.findViewById(R.id.lv_command_list);
        btn_schedule_reset = (Button) root.findViewById(R.id.btn_schedule_reset);
        et_schedule_command = (EditText) root.findViewById(R.id.et_schedule_command);
        et_schedule_min = (EditText) root.findViewById(R.id.et_schedule_min);
        et_schedule_sec = (EditText) root.findViewById(R.id.et_schedule_sec);
        btn_schedule_add = (Button) root.findViewById(R.id.btn_schedule_add);

        mArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        lv_command_list.setAdapter(mArrayAdapter);
        lv_command_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setMessage(getString(R.string.schedule_main_delete_message));
                ad.setTitle(getString(R.string.schedule_main_delete_title));
                ad.setPositiveButton(getString(R.string.schedule_main_delete_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mArrayAdapter.remove(mArrayAdapter.getItem(pos));
                            }
                        });
                ad.setNegativeButton(getString(R.string.schedule_main_delete_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                ad.show();

                return true;
            }
        });

        btn_schedule_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_schedule_canceled = true;
                mArrayAdapter.clear();
            }
        });

        btn_schedule_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int min, sec;

                if (et_schedule_command.getText() == null ||
                        et_schedule_command.getText().toString().equals("")) {
                    Toast.makeText(getActivity(),
                            getString(R.string.schedule_main_no_command),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (et_schedule_min.getText() == null ||
                        et_schedule_min.getText().toString().equals("")) {
                    min = 0;
                } else {
                    min = Integer.parseInt(et_schedule_min.getText().toString());
                }

                if (et_schedule_sec.getText() == null ||
                        et_schedule_sec.getText().toString().equals("")) {
                    sec = 0;
                } else {
                    sec = Integer.parseInt(et_schedule_sec.getText().toString());
                }

                is_schedule_canceled = false;

                final String text = getTime() + "+" + min + ":" + sec + " " + et_schedule_command.getText().toString();
                mArrayAdapter.add(text);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (is_schedule_canceled) {
                            return;
                        }
                        Socket_Control.SendScheduledCommand(et_schedule_command.getText().toString());
                        mArrayAdapter.remove(text);
                    }
                }, min * 60 * 1000 + sec * 1000);

                et_schedule_command.setText("");
                et_schedule_min.setText("");
                et_schedule_sec.setText("");
            }
        });

        return root;
    }

    String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]", Locale.getDefault());
        return f.format(new Date());
    }
}
