package com.skydigital.acsdatamanager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RecoverySystem;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BootReceiver extends BroadcastReceiver {
    private Context mContext;
    private static final String KEY_WRITE_FILE_PATH = "/sys/class/unifykeys/write";
    private static final String KEY_NAME_FILE_PATH = "/sys/class/unifykeys/name";
    private static final String KEY_READ_FILE_PATH = "/sys/class/unifykeys/read";
    private static final String KEY_NAME_ACSDATA = "acsdata";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");

        mContext = context;
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){


            Log.d("yw-test","receved  boot broad");
            String currentAcsdata = getAcsdata();

            if (currentAcsdata.length()==16){
                Log.d("yw-test","length is 16 do nothting");
                toast("acsdata already 16 bit now");
                setLedGreen(true);
                getUpdate();

            }else if (currentAcsdata.length() < 16){
                Log.d("yw-test","length is less than 16 do nothting");
                toast("acsdata is less than 16 bit");
                setLedGreen(false);
            } else {
                setLedGreen(false);
                Log.d("yw-test","length is more than 16 we need write again");

                Log.d("yw-test", "---------->currentAcsdata sub 0-15 is:" + currentAcsdata.substring(0,15));
                Log.d("yw-test", "---------->currentAcsdata sub 16-31 is:" + currentAcsdata.substring(16,31));

                String last16Bit = getLast16Bit(currentAcsdata);
                Log.d("yw-test", "last 16 is:" + last16Bit);

                if (last16Bit.length() == 16) {
                    Log.d("yw-test", "current data lenght is 16, let's write acsdata now");
                    writeAcsdata(last16Bit);
                } else {
                    Log.d("yw-test", "current length is not 16 do nothing");
                }

            }

        }
    }

    public void setLedGreen(boolean flag){
        if (flag){
            FileOperatorUtils.writeStringToFile("/sys/class/leds/sys-led-green/brightness","1",true);
            FileOperatorUtils.writeStringToFile("/sys/class/leds/sys-led-red/brightness","0",true);
        }else {
            FileOperatorUtils.writeStringToFile("/sys/class/leds/sys-led-green/brightness","0",true);
            FileOperatorUtils.writeStringToFile("/sys/class/leds/sys-led-red/brightness","1",true);
        }

    }


    public void toast(String message){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle("acsdata");
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("OK", null);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();

    }

    public String getLast16Bit(String origin) {
        Log.d("yw-test","getLast16Bit origin is:" + origin);
        Log.d("yw-test","getLast16Bit origin length is:" + origin.length());
        if (origin.length() <=16){
            Log.d("yw-test","getLast16Bit AAAAA");
            return origin;
        }else {
            Log.d("yw-test","getLast16Bit bbbbbbb");
            Log.d("yw-test","getLast16Bit bbbbbbb origin.length()-16 is:" + origin.length());
            Log.d("yw-test","getLast16Bit bbbbbbb origin.substring(origin.length()-16,origin.length()) is:" + origin.substring(origin.length()-16,origin.length()));
            Log.d("yw-test","getLast16Bit bbbbbbb origin.substring(origin.length()-16,origin.length()-1) is:" + origin.substring(origin.length()-16,origin.length()-1));
            return origin.substring(origin.length()-16,origin.length());

        }
    }

    public void writeAcsdata(String acsdata){

        boolean bSuccess = false;
        bSuccess = FileOperatorUtils.writeStringToFile(KEY_WRITE_FILE_PATH, acsdata, true);
        if (bSuccess) {
            Log.d("yw-test","write success");
            toast("acsdata modify to 16 bit successful");
            setLedGreen(true);
            getUpdate();

        }else {
            Log.d("yw-test","not 0");
        }

    }

    public void  getUpdate(){
        UsbManager.setContext(mContext);
        String fileKeyPath = UsbManager.checkUsbFileExist();
        Log.d("yw-test","path is:" + fileKeyPath);
        if (fileKeyPath != null) {
            fileKeyPath = fileKeyPath + "update2.zip";
            Log.d("yw-test", "--------->onClick -> serialno file path = " + fileKeyPath);

            copyUpdateToCache(new File(fileKeyPath), "/cache/update.zip");

        }

    }


    private void copyUpdateToCache(final File updateFile, final String savePath) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                FileInputStream fis = null;
                FileOutputStream fos = null;
                File saveFile = new File(savePath);
                try {
                    Log.e("yw-test", "before copy");
                    if (saveFile.exists()) {
                        saveFile.delete();
                    }
                    fis = new FileInputStream(updateFile);
                    fos = new FileOutputStream(savePath);
                    byte[] buffer = new byte[1024 * 1024];
                    int count = 0;
                    while ((count = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, count);
                    }
                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("yw-test", "before fail");
//                    sendMessage(WHAT_FINISH_COPY, -1, null);
                    return;
                } finally {
                    try {
                        if (fis != null) {
                            Log.e("yw-test", "end0 copy");
                            fis.close();
                        }
                        if (fos != null) {
                            Log.e("yw-test", "end1 copy");
                            fos.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            try {
                Log.e("yw-test", "before update");
                RecoverySystem.installPackage(mContext, new File("/cache/update.zip"));
            } catch (IOException e) {
                Log.e("yw-test", e.getMessage());
            }

//                sendMessage(WHAT_FINISH_COPY, 0, null);
            }
        }).start();
    }


    public String getAcsdata(){

        String retAcsdata = null;

        boolean bSuccess = FileOperatorUtils.writeStringToFile(KEY_NAME_FILE_PATH, KEY_NAME_ACSDATA,false);

        if (bSuccess) {
            String name = FileOperatorUtils.readStringFromFile(KEY_NAME_FILE_PATH,"");
            if (KEY_NAME_ACSDATA.equals(name.trim())) {
                Log.d("yw-test", "current read name is: " + KEY_NAME_FILE_PATH);
                Log.d("yw-test", "let's prepare to read/write acsdata");
            } else {
                Log.d("yw-test", "can't read /sys/class/unifykeys/name");
                return retAcsdata;
            }
        } else {
            Log.e("yw-test", "can't write /sys/class/unifykeys/name");
            return retAcsdata;
        }

        String usid = FileOperatorUtils.readStringFromFile(KEY_READ_FILE_PATH,"");

        Log.d("yw-test", "---------->isAcsdataKeyExist -> Success to write usid length is:" + usid.length());


         Log.d("yw-test", "---------->isAcsdataKeyExist -> Success to write usid name is:" + usid);
        return usid;
    }

}
