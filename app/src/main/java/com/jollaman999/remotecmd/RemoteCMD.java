package com.jollaman999.remotecmd;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class RemoteCMD extends ActionBarActivity {

    static Socket socket = null;

    static InetAddress serverAddr;
    final int SERVER_PORT = 7777;

    static boolean connection_error = true;

    static DataInputStream data_in;
    static DataOutputStream data_out;

    static EditText command_input;
    static EditText command_output;

    static String input = new String("");
    static String output = new String("");

    static boolean is_linux = false;

    public void Connect_and_Ready() {
        final EditText input_address = (EditText) findViewById(R.id.input_address);

        if (input_address.getText().toString().equals("")) {
            Toast.makeText(RemoteCMD.this, "Please input address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            serverAddr = InetAddress.getByName(input_address.getText().toString());
        } catch (UnknownHostException e) {
            Toast.makeText(RemoteCMD.this, "Unknow host!", Toast.LENGTH_SHORT).show();
            return;
        }

        new setSocket().start();

        if (connection_error)
            return;

        setContentView(R.layout.command);

        command_input = (EditText) findViewById(R.id.command_input);
        command_input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    input = command_input.getText().toString();
                    input = input.substring(0, input.length());
                    new Exec_command().start();
                    return true;
                }

                return false;
            }
        });
        command_output = (EditText) findViewById(R.id.command_output);

        Button btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = command_input.getText().toString();
                new Exec_command().start();
            }
        });

        Button btn_shutdown = (Button) findViewById(R.id.btn_shutdown);
        btn_shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_linux) {
                    command_input.setText("sudo shutdown -h now");
                } else {
                    command_input.setText("shutdown -s -t 0");
                }
            }
        });

        Button btn_reboot = (Button) findViewById(R.id.btn_reboot);
        btn_reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_linux) {
                    command_input.setText("sudo reboot");
                } else {
                    command_input.setText("shutdown -r -t 0");
                }
            }
        });

        final CheckBox chkbox_linux = (CheckBox) findViewById(R.id.chkbox_linux);
        chkbox_linux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_linux) {
                    is_linux = false;
                    chkbox_linux.setChecked(false);
                } else {
                    is_linux = true;
                    chkbox_linux.setChecked(true);
                }
            }
        });
    }

    public void Main_View_Ready() {
        final EditText input_address = (EditText) findViewById(R.id.input_address);
        input_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_address.setText("");
            }
        });
        input_address.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Connect_and_Ready();
                    return true;
                }

                return false;
            }
        });

        Button btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Connect_and_Ready();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Main_View_Ready();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote_cmd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_main) {
            setContentView(R.layout.main);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Main_View_Ready();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    final Handler Show_Unknow_Host = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(RemoteCMD.this, "Unknown Host!\n" +
                            "Can't connect to server!",
                    Toast.LENGTH_SHORT).show();
        }
    };

    final Handler Show_Connect_Error = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(RemoteCMD.this, "Can't connect to server!\n" +
                            "Connection refused from server or server is down!",
                    Toast.LENGTH_SHORT).show();
        }
    };

    class setSocket extends Thread {

        @Override
        public void run() {
            try {
                socket = new Socket(serverAddr, SERVER_PORT);
                connection_error = false;
            } catch (UnknownHostException e) {
                setContentView(R.layout.main);
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Main_View_Ready();

                Message msg = Show_Unknow_Host.obtainMessage();
                Show_Unknow_Host.sendMessage(msg);
                connection_error = true;

                return;
            } catch (IOException e) {
                setContentView(R.layout.main);
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Main_View_Ready();

                Message msg = Show_Connect_Error.obtainMessage();
                Show_Connect_Error.sendMessage(msg);
                connection_error = true;

                return;
            }
        }
    }

    final Handler Change_Text = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            command_output.setText(output);
            command_input.setText("");
        }
    };


    class Exec_command extends Thread {

        @Override
        public void run() {
            try {
                data_in = new DataInputStream(socket.getInputStream());
                data_out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                data_out.writeUTF(input);
                output = data_in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Message msg = Change_Text.obtainMessage();
            Change_Text.sendMessage(msg);
        }
    }
}
