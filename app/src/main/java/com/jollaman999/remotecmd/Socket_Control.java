package com.jollaman999.remotecmd;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Socket_Control {

    private static Socket socket;

    static InetAddress ServerAddr;
    private static final int SERVER_PORT = 7777;
    private static final int SOCKET_TIME_OUT_MS = 10000;

    private static boolean connection_ok; // For check connection error
    public static boolean is_connected = false; // For check connected state

    private static DataInputStream data_in;
    private static DataOutputStream data_out;

    static String input;
    static String output;

    private static String err_msg;
    private static int time_sec;

    private static boolean init;
    private static boolean command_success;

    // Use for setSocket
    private static String addr;

    private static NotificationManager mNotificationManager;
    private static Notification.Builder mNotificationBuilder;
    private static int notifyID = 0;

    private static class ConnectingTimerThread extends TimerTask {

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (init) {
                        if (!connection_ok || is_connected ||
                                time_sec == SOCKET_TIME_OUT_MS / 1000) {
                            cancel();
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    RemoteCMD.bt_main_connect.setEnabled(true);
                                }
                            });
                            return;
                        }
                    } else {
                        if (command_success) {
                            cancel();
                            return;
                        } else if (time_sec == SOCKET_TIME_OUT_MS / 1000) {
                            cancel();
                            Command_Input.et_result.setText(RemoteCMD.context.getString(R.string.command_timed_out));
                            ResetMain();
                            closeSocket();
                            return;
                        }
                    }

                    time_sec++;

                    if (init) {
                        HandleErr(RemoteCMD.context.getString(R.string.main_connecting) + " (" + time_sec + ")");
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                RemoteCMD.bt_main_connect.setEnabled(false);
                            }
                        });
                    } else {
                        HandleErr(RemoteCMD.context.getString(R.string.command_sending) + " (" + time_sec + ")");
                    }
                }
            });
        }
    }

    static void HandleErr(String s) {
        err_msg = s;

        if (init) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    RemoteCMD.tv_main_error_msg.setText(err_msg);
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Command_Input.et_result.setText(err_msg);
                }
            });
        }
    }

    private static class SocketThread extends Thread {

        @Override
        public void run() {
            init = true;
            connection_ok = true;
            is_connected = false;
            time_sec = 0;
            Timer mTimer = new Timer();
            mTimer.schedule(new ConnectingTimerThread(), 0, 1000);

            try {
                ServerAddr = InetAddress.getByName(Socket_Control.addr);
            } catch (UnknownHostException e) {
                connection_ok = false;
                HandleErr(RemoteCMD.context.getString(R.string.err_msg_unknown_host));
                return;
            }

            try {
                SocketAddress socketAddr = new InetSocketAddress(ServerAddr, SERVER_PORT);
                socket = new Socket();
                socket.connect(socketAddr, SOCKET_TIME_OUT_MS);
            } catch (UnknownHostException e) {
                connection_ok = false;
                e.printStackTrace();
                HandleErr(RemoteCMD.context.getString(R.string.err_msg_unknown_host));
                closeSocket();
                return;
            } catch (SocketTimeoutException e) {
                connection_ok = false;
                e.printStackTrace();
                HandleErr(RemoteCMD.context.getString(R.string.err_msg_timed_out));
                closeSocket();
                return;
            } catch (IOException e) {
                connection_ok = false;
                e.printStackTrace();
                HandleErr(RemoteCMD.context.getString(R.string.err_msg_io_err));
                closeSocket();
                return;
            }

            is_connected = true;
            HandleErr(RemoteCMD.context.getString(R.string.main_connected));

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    RemoteCMD.et_main_password.setVisibility(View.VISIBLE);
                    RemoteCMD.bt_main_connect.setText(RemoteCMD.context.getString(R.string.main_login));
                }
            });
        }
    }

    public static void setSocket(String addr) {
        Socket_Control.addr = addr;
        new SocketThread().start();
    }

    static void closeSocket() {
        try {
            socket.close();
            is_connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void ConnectedControl() {
        // Network thread should not run on main thread.
        // So make a new thread here.
        new Thread() {
            @Override
            public void run() {
                String password = RemoteCMD.et_main_password.getText().toString();
                final String input;

                try {
                    data_in = new DataInputStream(socket.getInputStream());
                    data_out = new DataOutputStream(socket.getOutputStream());

                    data_out.writeUTF(password);
                    input = data_in.readUTF();

                    if (input.equals("CONNECTED")) {
                        HandleErr(RemoteCMD.context.getString(R.string.main_login_ok));

                        Command_Activity.socket = socket;
                        Intent intent = new Intent();
                        intent.setClass(RemoteCMD.context, Command_Activity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        RemoteCMD.context.startActivity(intent);

                        mNotificationManager = (NotificationManager) RemoteCMD.context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationBuilder = new Notification.Builder(RemoteCMD.context);
                        mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(false);

                        init = false;
                    } else if (input.equals("DISCONNECTED")) {
                        HandleErr(RemoteCMD.context.getString(R.string.main_disconnected));
                        closeSocket();
                        ResetMain();
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RemoteCMD.context,
                                    input,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    HandleErr(RemoteCMD.context.getString(R.string.err_msg_io_err));
                    closeSocket();
                    ResetMain();
                }
            }
        }.start();
    }

    public static void SendCommand() {
        // Network thread should not run on main thread.
        // So make a new thread here.
        new Thread() {
            @Override
            public void run() {
                command_success = false;
                try {
                    data_out.writeUTF(input);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            time_sec = 0;
                            Timer mTimer = new Timer();
                            mTimer.schedule(new ConnectingTimerThread(), 0, 1000);
                            Command_Input.btn_send.setEnabled(false);
                        }
                    });
                    output = data_in.readUTF();

                } catch (IOException e) {
                    if (socket != null &&
                            time_sec != SOCKET_TIME_OUT_MS / 1000) {
                        e.printStackTrace();
                        output = RemoteCMD.context.getString(R.string.err_msg_io_err);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Command_Input.et_result.setText(output);
                            }
                        });
                    }
                    return;
                }

                command_success = true;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (output.equals("")) {
                            output = RemoteCMD.context.getString(R.string.command_input_success);
                        }
                        Command_Input.et_result.setText(output);
                        Command_Input.btn_send.setEnabled(true);
                    }
                });
            }
        }.start();
    }

    public static void SendShutdownCommand() {
        // Network thread should not run on main thread.
        // So make a new thread here.
        new Thread() {
            @Override
            public void run() {
                command_success = false;
                try {
                    data_out.writeUTF(input);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            time_sec = 0;
                            Timer mTimer = new Timer();
                            TimerTask mTimerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (command_success) {
                                                cancel();
                                                return;
                                            } else if (time_sec == SOCKET_TIME_OUT_MS / 1000) {
                                                cancel();
                                                Toast.makeText(RemoteCMD.context,
                                                        RemoteCMD.context.getString(R.string.command_timed_out),
                                                        Toast.LENGTH_SHORT).show();
                                                ResetMain();
                                                closeSocket();
                                                return;
                                            }

                                            time_sec++;
                                        }
                                    });
                                }
                            };

                            mTimer.schedule(mTimerTask, 0, 1000);
                        }
                    });
                    output = data_in.readUTF();

                } catch (IOException e) {
                    if (socket != null) {
                        e.printStackTrace();
                        Toast.makeText(RemoteCMD.context,
                                RemoteCMD.context.getString(R.string.err_msg_io_err),
                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                command_success = true;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (output.equals("")) {
                            output = RemoteCMD.context.getString(R.string.command_input_success);
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RemoteCMD.context,
                                        output,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }.start();
    }

    private static void Notify(String cmd, String output, int flag) {
        switch (flag) {
            case 1:
                mNotificationBuilder.setTicker(RemoteCMD.context.getString(R.string.command_input_success) + ": " + cmd)
                        .setContentTitle(RemoteCMD.context.getString(R.string.command_input_success))
                        .setStyle(new Notification.BigTextStyle().bigText(cmd + System.getProperty("line.separator") + output));
                break;
            case 2:
                mNotificationBuilder.setTicker(RemoteCMD.context.getString(R.string.err_msg_timed_out) + ": " + cmd)
                        .setContentTitle(RemoteCMD.context.getString(R.string.err_msg_timed_out))
                        .setContentText(cmd);
                break;
            case 3:
                mNotificationBuilder.setTicker(RemoteCMD.context.getString(R.string.err_msg_io_err) + ": " + cmd)
                        .setContentTitle(RemoteCMD.context.getString(R.string.err_msg_io_err))
                        .setContentText(cmd);
                break;
        }

        if (notifyID > 100) {
            notifyID = 0;
        }
        mNotificationManager.notify(notifyID++, mNotificationBuilder.build());
    }

    public static void SendScheduledCommand(String command) {
        final String cmd = command;

        // Network thread should not run on main thread.
        // So make a new thread here.
        new Thread() {
            @Override
            public void run() {
                command_success = false;
                try {
                    data_out.writeUTF(cmd);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            time_sec = 0;
                            Timer mTimer = new Timer();
                            TimerTask mTimerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (command_success) {
                                                cancel();
                                                return;
                                            } else if (time_sec == SOCKET_TIME_OUT_MS / 1000) {
                                                cancel();
                                                Notify(cmd, null, 2);
                                                return;
                                            }

                                            time_sec++;
                                        }
                                    });
                                }
                            };

                            mTimer.schedule(mTimerTask, 0, 1000);
                        }
                    });
                    output = data_in.readUTF();

                } catch (IOException e) {
                    e.printStackTrace();
                    Notify(cmd, null, 3);
                    return;
                }

                command_success = true;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (output.equals("")) {
                            output = RemoteCMD.context.getString(R.string.command_input_success);
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Notify(cmd, output, 1);
                            }
                        });
                    }
                });
            }
        }.start();
    }

    public static void ResetMain() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                init = true;
                HandleErr(RemoteCMD.context.getString(R.string.main_disconnected));
                RemoteCMD.bt_main_connect.setText(RemoteCMD.context.getString(R.string.main_connect));
                RemoteCMD.et_main_password.setText("");
                RemoteCMD.et_main_password.setVisibility(View.INVISIBLE);
            }
        });
    }
}
