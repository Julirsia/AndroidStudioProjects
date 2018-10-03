package com.example.julir.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class MainActivity extends Activity {

    TextView showText;
    //EditText showText;
    Button connectBtn;
    Button Button_send;
    EditText ip_EditText;
    EditText port_EditText;
    EditText editText_massage;
    Handler msghandler;

    SocketClient client; // 서버 접속으 위한 클라이언트 클래스
    ReceiveThread receive; // 서버에서 보내온 데이터 안드로이드에서 보이게 하는 거
    SendThread send; // 안드로이드에서 임의의 문자 보내는거
    Socket socket; // 네트워크

    LinkedList<SocketClient> threadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//액티비티 초기화
        setContentView(R.layout.activity_main);//액티비티 레이아웃 설정

        Intent intent = getIntent();
       float time = intent.getFloatExtra("time", 0);
       String strtime = String.valueOf(time);

        //안드로이드 view 소스코드 연동 래이아웃에 정의되어 있는 뷰
        ip_EditText = (EditText) findViewById(R.id.ip_EditText);
        port_EditText = (EditText) findViewById(R.id.port_EditText);
        connectBtn = (Button) findViewById(R.id.connect_Button);
        showText = (TextView) findViewById(R.id.showText_TextView);
        editText_massage = (EditText) findViewById(R.id.editText_massage);
        Button_send = (Button) findViewById(R.id.Button_send);
        threadList = new LinkedList<MainActivity.SocketClient>();

       editText_massage.setText(strtime);
        ip_EditText.setText("ip입력하세요");
        port_EditText.setText("9511");

// ReceiveThread를통해서 받은 메세지를 Handler로 MainThread에서 처리(외부Thread에서는 UI변경이불가)
        msghandler = new Handler() {
            @Override
            public void handleMessage(Message hdmsg) {
                if (hdmsg.what == 1111) {

                    String hdstr = hdmsg.obj.toString();
                    showText.setText(hdstr + "\n");


//식별자.
                    showText.setText(hdmsg.obj.toString() + "\n");//보여줄 객체
                }
            }
        };

// 연결버튼 클릭 이벤트
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//Client 연결부
                client = new SocketClient(ip_EditText.getText().toString(),
                        port_EditText.getText().toString());
                threadList.add(client);
                client.start();
            }
        });
        //전송 버튼 클릭 이벤트
        Button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

//SendThread 시작
                if (editText_massage.getText().toString() != null) {
                    send = new SendThread(socket);
                    send.start();

//시작후 edittext 초기화
                    editText_massage.setText("");
                }
            }
        });
    }

    class SocketClient extends Thread {
        boolean threadAlive;
        String ip;
        String port;

        DataOutputStream output = null; //byte 로 보내고 문자열로 읽고

        public SocketClient(String ip, String port) {
            threadAlive = true;
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {

            try {
// 연결후 바로 ReceiveThread 시작
                socket = new Socket(ip, Integer.parseInt(port));
                output = new DataOutputStream(socket.getOutputStream());
                receive = new ReceiveThread(socket);
                receive.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ReceiveThread extends Thread {
        private Socket sock = null;
        DataInputStream input;

        public ReceiveThread(Socket socket) {
            this.sock = socket;
            try{
                input = new DataInputStream(sock.getInputStream());
            }catch(Exception e){
            }
        }
        // 메세지 수신후 Handler로 전달
        public void run() {
            try {
                while (input != null) {
                    String msg;
                    int count = input.available();
                    byte[] rcv = new byte[count];
                    input.read(rcv);
                    msg = new String(rcv);

                    if (count > 0) {
                        Log.d(ACTIVITY_SERVICE, "test :" +msg);
                        Message hdmsg = msghandler.obtainMessage();
                        hdmsg.what = 1111;
                        hdmsg.obj = msg;
                        msghandler.sendMessage(hdmsg);
                        Log.d(ACTIVITY_SERVICE,hdmsg.obj.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SendThread extends Thread {
        Socket socket;
        String sendmsg = editText_massage.getText().toString()+"\n";
        DataOutputStream output;

        public SendThread(Socket socket) {
            this.socket = socket;
            try {
                output = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
            }
        }

        public void run() {

            try {
// 메세지 전송부
                Log.d(ACTIVITY_SERVICE, "11111");

                if (output != null) {
                    if (sendmsg != null) {
                        output.write(sendmsg.getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();

            }
        }
    }
}