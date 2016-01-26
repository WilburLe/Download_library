package com.idotools.http.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//import com.example.download_library.R;
import com.idotools.http.exception.DbException;
import com.idotools.http.utils.LogUtils;

public class MainActivity extends Activity {
    private DownloadManager downloadManager;
    private EditText mEditTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        downloadManager = DownloadService.getDownloadManager(this);
//        Button btn = (Button) findViewById(R.id.lockscreenTest);
//        mEditTxt = (EditText) findViewById(R.id.download_addr_edit);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                
//            }
//        });
//
//        Button downloadBtn = (Button) findViewById(R.id.download_btn);
//        downloadBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                download();
//            }
//        });

//        registerReceiver(receiver, new IntentFilter("com.dotools.lockScreenRecover"));
    }

    @SuppressLint("SdCardPath")
    public void download() {
//        String time = System.currentTimeMillis() / 1000 + "";
//        String target = "/sdcard/downlib/";
        try {
//            downloadManager.downloadApkFile(mEditTxt.getText().toString(), "力卓文件", "com.test.pkg" + time, 1, target, true,null);
            
            String target = "/sdcard/xUtils/" + System.currentTimeMillis() + "lzfile.tmp";
            downloadManager.addNewDownload(mEditTxt.getText().toString(),
                    "力卓文件",
                    target,
                    true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                    false, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                    null);
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
}
