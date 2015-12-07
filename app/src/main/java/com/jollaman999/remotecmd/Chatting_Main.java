package com.jollaman999.remotecmd;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Chatting_Main extends AppCompatActivity {

    EditText et_chat_show;
    EditText et_chat_send;
    Button btn_chat_send;

    static Socket chat_socket;

    boolean close_counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_main);

        et_chat_show = (EditText) findViewById(R.id.et_chat_show);
        et_chat_send = (EditText) findViewById(R.id.et_chat_send);

        et_chat_show.setText("");

        btn_chat_send = (Button) findViewById(R.id.btn_chat_send);

        // Network thread should not run on main thread.
        // So make a new thread here.
        new Thread() {
            @Override
            public void run() {
                try {
                    chat_socket = new Socket(Socket_Control.ServerAddr, 7778);
                } catch (IOException e) {
                    et_chat_show.append(getString(R.string.chatting_main_io_err));
                    e.printStackTrace();
                }

                if (chat_socket != null) {
                    close_counter = false;
                    new ClientReceiver(chat_socket).start();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        try {
            chat_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        close_counter = true;
        super.onDestroy();
    }

    class ClientReceiver extends Thread {

        Socket socket;
        DataInputStream input;
        String message;

        public ClientReceiver(Socket socket) {
            this.socket = socket;

            if (socket != null) {
                try {
                    input = new DataInputStream(socket.getInputStream());
                } catch (IOException e) {
                    et_chat_show.append(getString(R.string.err_msg_io_err));
                    e.printStackTrace();
                }
            } else {
                et_chat_show.append(getString(R.string.chatting_main_disconnected));
            }
        }

        @Override
        public void run() {
            new ClientSender(chat_socket);

            while (input != null) {
                if (chat_socket == null || close_counter ) {
                    return;
                }

                try {
                    message = input.readUTF();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            et_chat_show.append(message + "\n");
                        }
                    });
                } catch (IOException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            et_chat_show.append(getString(R.string.err_msg_io_err));
                        }
                    });
                    e.printStackTrace();
                }
            }
        }
    }

    class ClientSender {
        Socket socket;
        DataOutputStream output;
        String name;
        String message = "";

        public ClientSender(Socket socket) {
            this.socket = socket;

            name = Chatting_Login.et_chat_login_name.getText().toString();

            try {
                if (socket != null) {
                    output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(name);
                }
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        et_chat_show.append(getString(R.string.err_msg_io_err));
                    }
                });
                e.printStackTrace();
            }

            btn_chat_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (et_chat_send.getText() == null ||
                            et_chat_send.getText().toString().equals("")) {
                        return;
                    }

                    message = et_chat_send.getText().toString();
                    try {
                        output.writeUTF(getTime() + "[" + name + "]" + message);
                    } catch (IOException e) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                et_chat_show.append(getString(R.string.err_msg_io_err));
                            }
                        });
                        e.printStackTrace();
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            et_chat_send.setText("");
                        }
                    });
                }
            });
        }
    }

    static String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date());
    }
}
