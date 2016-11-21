package dmoe.cn.nettyclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.netty.PushClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getIntent().getStringExtra("hi"));
        Button btnStart = (Button) findViewById(R.id.btn1);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushClient.start();
            }
        });

        Button btnClose = (Button) findViewById(R.id.btn2);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushClient.close();
            }
        });

        Button btnCheck = (Button) findViewById(R.id.btn3);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushClient.isOpen();
            }
        });

    }
}
