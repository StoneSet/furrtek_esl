package org.furrtek.pricehax;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;

public class MainActivity extends Activity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int SELECT_PHOTO = 100;
    static AudioTrack audioTrack;
    static BluetoothAdapter btAdapter = null;
    static BluetoothSocket btSocket = null;
    static boolean btok = false;
    public static InputStream inStream = null;
    public static OutputStream outStream = null;
    static int transmitVolume;
    Integer PLType;
    final int REQUEST_ENABLE_BT = 1;
    Activity at = this;
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean z, Camera camera) {
            MainActivity.this.autoFocusHandler.postDelayed(MainActivity.this.doAutoFocus, 1000);
        }
    };
    private Handler autoFocusHandler;
    private boolean barcodeScanned = false;
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (MainActivity.this.previewing && MainActivity.this.tabHost.getCurrentTab() == 1 && MainActivity.this.mCamera != null) {
                MainActivity.this.mCamera.autoFocus(MainActivity.this.autoFocusCB);
            }
        }
    };
    int donglever;
    Handler handler = new Handler();
    int hi;
    InputStream imageStream = null;
    ImageView imgbmp;
    TextView label_plhex;
    private Camera mCamera;
    private CameraPreview mPreview;
    ProgressBar pgb;
    int plBitDef;
    long plID;
    FrameLayout preview;
    PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] bArr, Camera camera) {
            Size previewSize = camera.getParameters().getPreviewSize();
            Image image = new Image(previewSize.width, previewSize.height, "Y800");
            image.setData(bArr);
            if (MainActivity.this.scanner.scanImage(image) != 0) {
                MainActivity.this.previewing = false;
                MainActivity.this.mCamera.setPreviewCallback(null);
                MainActivity.this.mCamera.stopPreview();
                Iterator it = MainActivity.this.scanner.getResults().iterator();
                while (it.hasNext()) {
                    String data = ((Symbol) it.next()).getData();
                    MainActivity.this.plID = (long) ((Integer.parseInt(data.substring(2, 7)) << 16) + Integer.parseInt(data.substring(7, 12)));
                    String toHexString = Long.toHexString(MainActivity.this.plID);
                    MainActivity.this.PLType = Integer.valueOf(Integer.parseInt(data.substring(12, 16)));
                    MainActivity.this.scaneibarcode.setText("Barcode: " + data);
                    MainActivity.this.scaneiserial.setText("ID: " + toHexString.toUpperCase());
                    switch (MainActivity.this.PLType.intValue()) {
                        case 1206:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (E2 HCS)");
                            break;
                        case 1207:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (E2 HCN)");
                            MainActivity.this.plBitDef = 4;
                            break;
                        case 1217:
                        case 1265:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (E5 SMALL)");
                            MainActivity.this.plBitDef = 2;
                            break;
                        case 1219:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (E5 MEDIUM)");
                            MainActivity.this.plBitDef = 1;
                            break;
                        case 1240:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (E4 HCS)");
                            MainActivity.this.plBitDef = 3;
                            break;
                        case 1241:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (E4 HCN)");
                            break;
                        case 1242:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (E4 HCN FZ)");
                            MainActivity.this.plBitDef = 0;
                            break;
                        case 1300:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (DotMatrix 172x72)");
                            MainActivity.this.wi = 172;
                            MainActivity.this.hi = 72;
                            break;
                        case 1318:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (ST 208x112)");
                            MainActivity.this.wi = 208;
                            MainActivity.this.hi = 112;
                            break;
                        default:
                            MainActivity.this.scaneitype.setText("Type: " + MainActivity.this.PLType + " (incompatible)");
                            break;
                    }
                    MainActivity.this.barcodeScanned = true;
                }
            }
        }
    };
    private boolean previewing = true;
    byte[] rawbitstream = new byte[23296];
    private boolean repeatMode = false;
    private boolean repeatModeDM = false;
    Bitmap scaledimage;
    Button scanButton;
    TextView scaneibarcode;
    TextView scaneiserial;
    TextView scaneitype;
    ImageScanner scanner;
    SharedPreferences settings;
    Spinner spinner;
    TabHost tabHost;
    int tabPos;
    Timer timer;
    Timer timerdm;
    TextView txtworkh;
    int wi;
    int x;
    int y;
    int ymax;

    static {
        System.loadLibrary("iconv");
    }

    private static void checkBTState() {
        if (btAdapter == null) {
            Log.d("BT", "NO BT ADAPTER");
        } else if (btAdapter.isEnabled()) {
            Log.d("BT", "BT ENABLED");
            BluetoothDevice bluetoothDevice = null;
            for (BluetoothDevice bluetoothDevice2 : btAdapter.getBondedDevices()) {
                BluetoothDevice bluetoothDevice22;
                Log.d("BT", "LIST: " + bluetoothDevice22.getName());
                if (bluetoothDevice22.getName().equals("PRICEHAX TX V3")) {
                    bluetoothDevice22 = btAdapter.getRemoteDevice(bluetoothDevice22.getAddress());
                    Log.d("BT", "FOUND BT PRICEHAX TX V3 :)");
                    bluetoothDevice = bluetoothDevice22;
                }
            }
            if (bluetoothDevice != null) {
                try {
                    btSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    Log.d("BT", "EXCEPTION IN RF COMM SOCKET CREATION");
                }
                btAdapter.cancelDiscovery();
                Log.d("BT", "CONNECTING...");
                try {
                    btSocket.connect();
                    btok = true;
                    Log.d("BT", "CONNECTION OK");
                } catch (IOException e2) {
                    try {
                        btSocket.close();
                    } catch (IOException e3) {
                        Log.d("BT", "EXCEPTION IN CONNECT");
                    }
                }
                try {
                    outStream = btSocket.getOutputStream();
                    inStream = btSocket.getInputStream();
                } catch (IOException e4) {
                    Log.d("BT", "EXCEPTION IN STREAM CREATION");
                }
            }
        }
    }

    public static Camera getCameraInstance() {
        try {
            return Camera.open(0);
        } catch (Exception e) {
            return null;
        }
    }

    private void releaseCamera() {
        if (this.mCamera != null) {
            this.previewing = false;
            this.mCamera.setPreviewCallback(null);
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    public void convertImage(View view) {
        new Thread(new Runnable() {
            public void run() {
                int i;
                int i2;
                int pixel;
                MainActivity.this.txtworkh = (TextView) MainActivity.this.findViewById(R.id.txtwork);
                MainActivity.this.pgb = (ProgressBar) MainActivity.this.findViewById(R.id.pgb1);
                MainActivity.this.imgbmp = (ImageView) MainActivity.this.findViewById(R.id.imgvbmp);
                MainActivity.this.scaledimage = ((BitmapDrawable) MainActivity.this.imgbmp.getDrawable()).getBitmap();
                if (MainActivity.this.PLType.intValue() == 1318) {
                    i = 208;
                    i2 = 112;
                } else {
                    i = 172;
                    i2 = 72;
                }
                MainActivity.this.wi = i;
                MainActivity.this.hi = i2;
                MainActivity.this.scaledimage = Bitmap.createScaledBitmap(MainActivity.this.scaledimage, MainActivity.this.wi, MainActivity.this.hi, true);
                int i3 = 0;
                MainActivity.this.y = 0;
                while (MainActivity.this.y < i2) {
                    MainActivity mainActivity;
                    MainActivity.this.x = 0;
                    while (MainActivity.this.x < i) {
                        pixel = MainActivity.this.scaledimage.getPixel(MainActivity.this.x, MainActivity.this.y);
                        if (((int) (((((double) Color.green(pixel)) * 0.587d) + (0.299d * ((double) Color.red(pixel)))) + (0.114d * ((double) Color.blue(pixel))))) < 128) {
                            MainActivity.this.rawbitstream[i3] = (byte) 0;
                        } else {
                            MainActivity.this.rawbitstream[i3] = (byte) 1;
                        }
                        mainActivity = MainActivity.this;
                        mainActivity.x++;
                        i3++;
                    }
                    MainActivity.this.handler.post(new Runnable() {
                        public void run() {
                            MainActivity.this.pgb.setProgress((MainActivity.this.y * 36) / MainActivity.this.wi);
                        }
                    });
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            MainActivity.this.txtworkh.setText("Converting to monochrome: line " + MainActivity.this.y);
                        }
                    });
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mainActivity = MainActivity.this;
                    mainActivity.y++;
                }
                int i4 = i3 - 1;
                byte b = MainActivity.this.rawbitstream[0];
                MainActivity.this.y = 0;
                List<Integer> arrayList = new ArrayList();
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.txtworkh.setText("RLE compress...");
                    }
                });
                pixel = 1;
                byte b2 = b;
                i = 1;
                byte b3 = b2;
                while (i <= i4) {
                    byte b4 = MainActivity.this.rawbitstream[i];
                    if (b4 == b3) {
                        i2 = pixel + 1;
                        if (i == i4 - 1) {
                            arrayList.add(Integer.valueOf(i2));
                        }
                    } else {
                        arrayList.add(Integer.valueOf(pixel));
                        i2 = 1;
                        if (i == i4 - 1) {
                            arrayList.add(Integer.valueOf(1));
                        }
                    }
                    if ((i & 31) == 0) {
                        MainActivity.this.handler.post(new Runnable() {
                            public void run() {
                                MainActivity.this.pgb.setProgress((MainActivity.this.y / 1238) + 36);
                            }
                        });
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                    i++;
                    mainActivity = MainActivity.this;
                    mainActivity.y++;
                    pixel = i2;
                    b3 = b4;
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.txtworkh.setText("Hexadecimalifying...");
                    }
                });
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(MainActivity.this.rawbitstream[0]);
                for (Integer intValue : arrayList) {
                    String toBinaryString = Integer.toBinaryString(intValue.intValue());
                    StringBuffer stringBuffer = new StringBuffer(toBinaryString.length());
                    for (i2 = 0; i2 < toBinaryString.length() - 1; i2++) {
                        stringBuffer.append("0");
                    }
                    stringBuilder.append(stringBuffer.toString());
                    stringBuilder.append(toBinaryString);
                }
                i2 = stringBuilder.toString().length() % 320;
                if (i2 > 0) {
                    i2 = 320 - i2;
                }
                for (i = 0; i < i2; i++) {
                    stringBuilder.append("0");
                }
                String stringBuilder2 = stringBuilder.toString();
                List arrayList2 = new ArrayList();
                for (i2 = 0; i2 < stringBuilder2.length(); i2 += 8) {
                    arrayList2.add(Byte.valueOf((byte) Integer.parseInt(stringBuilder2.substring(i2, i2 + 8), 2)));
                }
                int size = arrayList2.size();
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
                MainActivity.this.handler.post(new Runnable() {
                    public void run() {
                        MainActivity.this.pgb.setProgress(MainActivity.SELECT_PHOTO);
                    }
                });
                MainActivity.audioTrack = new AudioTrack(3, 48000, 4, 2, 48000, 1);
                byte[] bArr = new byte[]{(byte) -123, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) -105, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1};
                bArr[1] = (byte) ((int) (255 & MainActivity.this.plID));
                bArr[2] = (byte) ((int) (MainActivity.this.plID >> 8));
                bArr[3] = (byte) ((int) (MainActivity.this.plID >> 16));
                bArr[4] = (byte) ((int) (MainActivity.this.plID >> 24));
                byte[] GetCRC = CRCCalc.GetCRC(bArr, 30);
                bArr[30] = GetCRC[0];
                bArr[31] = GetCRC[1];
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.txtworkh.setText("Waking up ESL...");
                    }
                });
                PP4C.sendPP4C(MainActivity.this.at.getApplicationContext(), bArr, 32, MainActivity.this.donglever, 50, MainActivity.audioTrack);
                if (MainActivity.this.donglever == 2) {
                    SystemClock.sleep(4600);
                } else if (MainActivity.this.donglever == 1) {
                    SystemClock.sleep(1800);
                    PP4C.sendPP4C(MainActivity.this.at.getApplicationContext(), bArr, 32, MainActivity.this.donglever, 35, MainActivity.audioTrack);
                    SystemClock.sleep(1800);
                    PP4C.sendPP4C(MainActivity.this.at.getApplicationContext(), bArr, 32, MainActivity.this.donglever, 35, MainActivity.audioTrack);
                    SystemClock.sleep(1800);
                }
                bArr = new byte[54];
                bArr[0] = (byte) -123;
                bArr[1] = (byte) ((int) (255 & MainActivity.this.plID));
                bArr[2] = (byte) ((int) (MainActivity.this.plID >> 8));
                bArr[3] = (byte) ((int) (MainActivity.this.plID >> 16));
                bArr[4] = (byte) ((int) (MainActivity.this.plID >> 24));
                bArr[5] = (byte) 52;
                bArr[6] = (byte) 0;
                bArr[7] = (byte) 0;
                bArr[8] = (byte) 0;
                bArr[9] = (byte) 5;
                bArr[10] = (byte) (size >> 8);
                bArr[11] = (byte) (size & MotionEventCompat.ACTION_MASK);
                byte[] bArr2;
                if (MainActivity.this.PLType.intValue() == 1318) {
                    bArr2 = new byte[]{(byte) 0, (byte) 2, (byte) 3, (byte) 0, (byte) -48, (byte) 0, (byte) 112, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) -120, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                    for (i2 = 0; i2 < 20; i2++) {
                        bArr[i2 + 12] = bArr2[i2];
                    }
                } else {
                    bArr2 = new byte[]{(byte) 0, (byte) 2, (byte) 4, (byte) 0, (byte) -84, (byte) 0, (byte) 72, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) -120, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                    for (i2 = 0; i2 < 20; i2++) {
                        bArr[i2 + 12] = bArr2[i2];
                    }
                }
                GetCRC = CRCCalc.GetCRC(bArr, 32);
                bArr[32] = GetCRC[0];
                bArr[33] = GetCRC[1];
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.txtworkh.setText("Start frame...");
                    }
                });
                PP4C.sendPP4C(MainActivity.this.at.getApplicationContext(), bArr, 34, MainActivity.this.donglever, 6, MainActivity.audioTrack);
                if (MainActivity.this.donglever == 2) {
                    SystemClock.sleep(1000);
                }
                if (MainActivity.this.donglever == 1) {
                    SystemClock.sleep(1600);
                }
                MainActivity.this.ymax = size / 40;
                MainActivity.this.y = 0;
                while (MainActivity.this.y < size / 40) {
                    bArr[0] = (byte) -123;
                    bArr[1] = (byte) ((int) (255 & MainActivity.this.plID));
                    bArr[2] = (byte) ((int) (MainActivity.this.plID >> 8));
                    bArr[3] = (byte) ((int) (MainActivity.this.plID >> 16));
                    bArr[4] = (byte) ((int) (MainActivity.this.plID >> 24));
                    bArr[5] = (byte) 52;
                    bArr[6] = (byte) 0;
                    bArr[7] = (byte) 0;
                    bArr[8] = (byte) 0;
                    bArr[9] = (byte) 32;
                    bArr[10] = (byte) (MainActivity.this.y >> 16);
                    bArr[11] = (byte) (MainActivity.this.y & MotionEventCompat.ACTION_MASK);
                    for (i3 = 0; i3 < 40; i3++) {
                        bArr[i3 + 12] = ((Byte) arrayList2.get((MainActivity.this.y * 40) + i3)).byteValue();
                    }
                    GetCRC = CRCCalc.GetCRC(bArr, 52);
                    bArr[52] = GetCRC[0];
                    bArr[53] = GetCRC[1];
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            MainActivity.this.txtworkh.setText("Data frame " + MainActivity.this.y + "/" + MainActivity.this.ymax);
                        }
                    });
                    PP4C.sendPP4C(MainActivity.this.at.getApplicationContext(), bArr, 54, MainActivity.this.donglever, 1, MainActivity.audioTrack);
                    if (MainActivity.this.donglever == 2) {
                        SystemClock.sleep(550);
                    }
                    if (MainActivity.this.donglever == 1) {
                        SystemClock.sleep(1800);
                    }
                    MainActivity mainActivity2 = MainActivity.this;
                    mainActivity2.y++;
                }
                bArr = new byte[]{(byte) -123, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 52, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                bArr[1] = (byte) ((int) (255 & MainActivity.this.plID));
                bArr[2] = (byte) ((int) (MainActivity.this.plID >> 8));
                bArr[3] = (byte) ((int) (MainActivity.this.plID >> 16));
                bArr[4] = (byte) ((int) (MainActivity.this.plID >> 24));
                GetCRC = CRCCalc.GetCRC(bArr, 28);
                bArr[28] = GetCRC[0];
                bArr[29] = GetCRC[1];
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.txtworkh.setText("Verify frame...");
                    }
                });
                PP4C.sendPP4C(MainActivity.this.at.getApplicationContext(), bArr, 30, MainActivity.this.donglever, 10, MainActivity.audioTrack);
                SystemClock.sleep(2000);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.txtworkh.setText("Done ! ;-)");
                    }
                });
            }
        }).start();
    }

    public void launchBarDialog(View view) {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(intent, SELECT_PHOTO);
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        switch (i) {
            case SELECT_PHOTO /*100*/:
                if (i2 == -1 && intent.getData() != null) {
                    try {
                        this.imageStream = getContentResolver().openInputStream(intent.getData());
                        this.imgbmp = (ImageView) findViewById(R.id.imgvbmp);
                        this.scaledimage = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(this.imageStream), this.wi, this.hi, true);
                        this.imgbmp.setImageBitmap(this.scaledimage);
                        return;
                    } catch (Exception e) {
                        Log.d("PHX", e.getLocalizedMessage());
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        this.settings = getSharedPreferences("Pricehax", 0);
        audioTrack = new AudioTrack(3, 48000, 4, 2, 48000, 1);
        if (btok) {
            ((TextView) findViewById(R.id.tvbtt)).setText("Connected to BT transmitter !");
        }
        if (bundle != null) {
            this.tabPos = bundle.getInt("tabPos");
        } else {
            this.tabPos = 0;
        }
        getActionBar().hide();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
        SeekBar seekBar = (SeekBar) findViewById(R.id.sb);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                MainActivity.transmitVolume = i;
                ((TextView) MainActivity.this.findViewById(R.id.tvvolume)).setText("Transmit volume: " + (MainActivity.transmitVolume + 60) + "%");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Editor edit = MainActivity.this.getSharedPreferences("Pricehax", 0).edit();
                edit.putInt("tvolume", MainActivity.transmitVolume);
                edit.commit();
            }
        });
        this.label_plhex = (TextView) findViewById(R.id.label_dbgbs);
        this.donglever = this.settings.getInt("donglever", 0);
        if (this.donglever == 0) {
            this.donglever = 1;
        }
        if (this.donglever > 3) {
            this.donglever = 3;
        }
        transmitVolume = this.settings.getInt("tvolume", 20);
        seekBar.setProgress(transmitVolume);
        if (this.donglever == 2) {
            ((CheckBox) findViewById(R.id.chk_donglever)).setChecked(true);
        } else {
            ((CheckBox) findViewById(R.id.chk_donglever)).setChecked(false);
            if (this.donglever == 3) {
                ((CheckBox) findViewById(R.id.chk_donglebt)).setChecked(true);
            } else {
                ((CheckBox) findViewById(R.id.chk_donglebt)).setChecked(false);
            }
        }
        this.tabHost = (TabHost) findViewById(R.id.tabHost);
        this.tabHost.setup();
        View inflate = LayoutInflater.from(this).inflate(R.layout.tab_indicator, this.tabHost.getTabWidget(), false);
        ((TextView) inflate.findViewById(2131296299)).setText("CHANGE PAGE");
        TabSpec newTabSpec = this.tabHost.newTabSpec("Tab 1");
        newTabSpec.setIndicator(inflate);
        newTabSpec.setContent(R.id.tab1);
        inflate = LayoutInflater.from(this).inflate(R.layout.tab_indicator, this.tabHost.getTabWidget(), false);
        ((TextView) inflate.findViewById(2131296299)).setText("PLID SCAN");
        TabSpec newTabSpec2 = this.tabHost.newTabSpec("Tab 2");
        newTabSpec2.setIndicator(inflate);
        newTabSpec2.setContent(R.id.tab2);
        inflate = LayoutInflater.from(this).inflate(R.layout.tab_indicator, this.tabHost.getTabWidget(), false);
        ((TextView) inflate.findViewById(2131296299)).setText("CHANGE SEGMENTS");
        TabSpec newTabSpec3 = this.tabHost.newTabSpec("Tab 3");
        newTabSpec3.setIndicator(inflate);
        newTabSpec3.setContent(R.id.tab3);
        inflate = LayoutInflater.from(this).inflate(R.layout.tab_indicator, this.tabHost.getTabWidget(), false);
        ((TextView) inflate.findViewById(2131296299)).setText("CONFIG");
        TabSpec newTabSpec4 = this.tabHost.newTabSpec("Tab 4");
        newTabSpec4.setIndicator(inflate);
        newTabSpec4.setContent(R.id.tab4);
        inflate = LayoutInflater.from(this).inflate(R.layout.tab_indicator, this.tabHost.getTabWidget(), false);
        ((TextView) inflate.findViewById(2131296299)).setText("LEGAL INFO");
        TabSpec newTabSpec5 = this.tabHost.newTabSpec("Tab 5");
        newTabSpec5.setIndicator(inflate);
        newTabSpec5.setContent(R.id.tab5);
        inflate = LayoutInflater.from(this).inflate(R.layout.tab_indicator, this.tabHost.getTabWidget(), false);
        ((TextView) inflate.findViewById(2131296299)).setText("CHANGE IMAGE");
        TabSpec newTabSpec6 = this.tabHost.newTabSpec("Tab 6");
        newTabSpec6.setIndicator(inflate);
        newTabSpec6.setContent(R.id.tab6);
        this.tabHost.addTab(newTabSpec);
        this.tabHost.addTab(newTabSpec2);
        this.tabHost.addTab(newTabSpec3);
        this.tabHost.addTab(newTabSpec6);
        this.tabHost.addTab(newTabSpec4);
        this.tabHost.addTab(newTabSpec5);
        this.tabHost.setCurrentTab(this.tabPos);
        for (int i = 0; i < this.tabHost.getTabWidget().getChildCount(); i++) {
            this.tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 128;
        }
        for (int i2 = 0; i2 < this.tabHost.getTabWidget().getTabCount(); i2++) {
            ((TextView) ((ViewGroup) this.tabHost.getTabWidget().getChildAt(i2)).getChildAt(0)).setTextColor(Color.parseColor("#E03030"));
        }
        this.spinner = (Spinner) findViewById(R.id.spn);
        SpinnerAdapter createFromResource = ArrayAdapter.createFromResource(this, R.array.segChoices, 17367048);
        createFromResource.setDropDownViewResource(17367049);
        this.spinner.setAdapter(createFromResource);
        this.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                int[][] iArr = new int[][]{new int[]{1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0}, new int[]{1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0}, new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0}, new int[]{0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0}};
                for (int i2 = 0; i2 < 21; i2++) {
                    ToggleButton toggleButton = (ToggleButton) MainActivity.this.findViewById(MainActivity.this.getResources().getIdentifier("seg" + i2, "id", MainActivity.this.getPackageName()));
                    if (iArr[i][i2] == 1) {
                        toggleButton.setChecked(true);
                    } else {
                        toggleButton.setChecked(false);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.spinner = (Spinner) findViewById(R.id.spnimg);
        createFromResource = ArrayAdapter.createFromResource(this, R.array.imgChoices, 17367048);
        createFromResource.setDropDownViewResource(17367049);
        this.spinner.setAdapter(createFromResource);
        this.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                ((ImageView) MainActivity.this.findViewById(R.id.imgvbmp)).setImageResource(new int[]{R.drawable.burd, R.drawable.dolan, R.drawable.gratuit, R.drawable.holyshit, R.drawable.link, R.drawable.mahboi, R.drawable.sanic, R.drawable.troll}[i]);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.autoFocusHandler = new Handler();
        this.mCamera = getCameraInstance();
        Parameters parameters = this.mCamera.getParameters();
        parameters.setPreviewSize(640, 480);
        this.mCamera.setParameters(parameters);
        this.mPreview = new CameraPreview(this, this.mCamera, this.previewCb, this.autoFocusCB);
        this.preview = (FrameLayout) findViewById(R.id.cameraPreview);
        this.preview.addView(this.mPreview);
        this.scanner = new ImageScanner();
        this.scanner.setConfig(0, 256, 3);
        this.scanner.setConfig(0, Config.Y_DENSITY, 3);
        this.scanner.setConfig(0, 0, 0);
        this.scanner.setConfig(128, 0, 1);
        this.scaneibarcode = (TextView) findViewById(R.id.eslinfo_barcode);
        this.scaneiserial = (TextView) findViewById(R.id.eslinfo_serial);
        this.scaneitype = (TextView) findViewById(R.id.eslinfo_type);
        this.scanButton = (Button) findViewById(R.id.scan_button);
        this.scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (MainActivity.this.mCamera == null) {
                    MainActivity.this.mCamera = MainActivity.getCameraInstance();
                    MainActivity.this.mCamera.setPreviewCallback(MainActivity.this.previewCb);
                    MainActivity.this.mCamera.startPreview();
                    MainActivity.this.previewing = true;
                    MainActivity.this.mCamera.autoFocus(MainActivity.this.autoFocusCB);
                    ((FrameLayout) MainActivity.this.findViewById(R.id.cameraPreview)).addView(MainActivity.this.mPreview);
                }
                if (MainActivity.this.barcodeScanned) {
                    MainActivity.this.barcodeScanned = false;
                    MainActivity.this.scaneibarcode.setText("Scan ESL barcode");
                    MainActivity.this.mCamera.setPreviewCallback(MainActivity.this.previewCb);
                    MainActivity.this.mCamera.startPreview();
                    MainActivity.this.previewing = true;
                    MainActivity.this.mCamera.autoFocus(MainActivity.this.autoFocusCB);
                }
            }
        });
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
        this.preview.removeView(this.mPreview);
        audioTrack.release();
    }

    public void onResume() {
        super.onResume();
        if (this.mCamera == null) {
            this.mCamera = getCameraInstance();
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("tabPos", this.tabHost.getCurrentTab());
    }

    public void repeatSendPage(View view) {
        this.repeatMode = ((CheckBox) findViewById(R.id.chkrepeat)).isChecked();
        if (this.repeatMode) {
            if (this.timerdm != null) {
                ((CheckBox) findViewById(R.id.chkrepeatdm)).setChecked(false);
                this.timerdm.cancel();
                this.timerdm.purge();
            }
            this.timer = new Timer();
            final Handler handler = new Handler();
            this.timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            MainActivity.this.sendPageUpdate();
                        }
                    });
                }
            }, 0, 1500);
            return;
        }
        this.timer.cancel();
        this.timer.purge();
    }

    public void repeatSendPageDM(View view) {
        this.repeatModeDM = ((CheckBox) findViewById(R.id.chkrepeatdm)).isChecked();
        if (this.repeatModeDM) {
            if (this.timer != null) {
                ((CheckBox) findViewById(R.id.chkrepeat)).setChecked(false);
                this.timer.cancel();
                this.timer.purge();
            }
            this.timerdm = new Timer();
            final Handler handler = new Handler();
            this.timerdm.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            MainActivity.this.sendPageUpdate();
                        }
                    });
                }
            }, 0, 1500);
            return;
        }
        this.timerdm.cancel();
        this.timerdm.purge();
    }

    public void sendDMKC(View view) {
        new Thread(new Runnable() {
            public void run() {
                MainActivity.this.handler.post(new Runnable() {
                    public void run() {
                        MainActivity.this.sendDMKeyChange();
                    }
                });
            }
        }).start();
    }

    void sendDMKeyChange() {
        byte[] bArr = new byte[]{(byte) -123, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 52, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 19, (byte) 55, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
        bArr[1] = (byte) ((int) (255 & this.plID));
        bArr[2] = (byte) ((int) (this.plID >> 8));
        bArr[3] = (byte) ((int) (this.plID >> 16));
        bArr[4] = (byte) ((int) (this.plID >> 24));
        byte[] GetCRC = CRCCalc.GetCRC(bArr, 28);
        bArr[28] = GetCRC[0];
        bArr[29] = GetCRC[1];
        PP4C.sendPP4C(getApplicationContext(), bArr, 30, this.donglever, 60, audioTrack);
        String str = "";
        for (int i = 0; i < 30; i++) {
            str = str + String.format("%02X", new Object[]{Byte.valueOf(bArr[i])});
        }
        ((TextView) findViewById(R.id.label_dbgbs)).setText("DM change MK: " + str);
    }

    public void sendDMPage(View view) {
        new Thread(new Runnable() {
            public void run() {
                MainActivity.this.handler.post(new Runnable() {
                    public void run() {
                        MainActivity.this.sendDMPageUpdate();
                    }
                });
            }
        }).start();
    }

    void sendDMPageUpdate() {
        int i;
        byte[] bArr = new byte[18];
        EditText editText = (EditText) findViewById(R.id.etxtPage);
        if (editText.getText() == null) {
            editText.setText("0");
        }
        byte[] bytes = editText.getText().toString().getBytes();
        int i2 = 1;
        while (i2 < 9) {
            if (((RadioButton) findViewById(getResources().getIdentifier("raddur" + i2, "id", getPackageName()))).isChecked()) {
                break;
            }
            i2++;
        }
        i2 = 0;
        if (i2 == 1) {
            i = 2;
        } else {
            byte b = (byte) 0;
        }
        if (i2 == 2) {
            i = 4;
        }
        if (i2 == 3) {
            i = 15;
        }
        if (i2 == 4) {
            i = 240;
        }
        if (i2 == 5) {
            i = 900;
        }
        if (i2 == 6) {
            i = 1800;
        }
        if (i2 == 7) {
            i = 2700;
        }
        bArr[0] = (byte) -123;
        bArr[1] = (byte) 0;
        bArr[2] = (byte) 0;
        bArr[3] = (byte) 0;
        bArr[4] = (byte) 0;
        bArr[5] = (byte) 6;
        if (i2 == 8) {
            bArr[6] = (byte) -15;
            i = 10;
        } else {
            bArr[6] = (byte) (((bytes[0] & 15) << 3) | 1);
        }
        bArr[7] = (byte) 0;
        bArr[8] = (byte) 0;
        bArr[9] = (byte) (i >> 8);
        bArr[10] = (byte) (i & MotionEventCompat.ACTION_MASK);
        byte[] GetCRC = CRCCalc.GetCRC(bArr, 11);
        bArr[11] = GetCRC[0];
        bArr[12] = GetCRC[1];
        PP4C.sendPP4C(getApplicationContext(), bArr, 13, this.donglever, 60, audioTrack);
    }

    void sendFlashUpdate() {
        int[][] iArr = new int[][]{new int[]{174, 162, 70, 24, 82, 128, 116, 150, 139, 47, 1, 58, 104, 93, 175, 163, 71, 25, 83, 129, 117}, new int[]{139, 104, 12, 1, 47, 93, 58, 162, 128, 36, 24, 70, 116, 82, 138, 103, 11, 0, 46, 92, 57}, new int[]{145, 110, 18, 7, 53, 99, 64, 168, 134, 42, 30, 76, 122, 88, 144, 109, 17, 6, 52, 98, 63}, new int[]{146, SELECT_PHOTO, 54, 8, 65, 157, 111, 170, 124, 78, 32, 90, 182, 136, 147, 101, 55, 9, 66, 158, 112}};
        byte[] bArr = new byte[23];
        for (int i = 0; i < 21; i++) {
            if (((ToggleButton) findViewById(getResources().getIdentifier("seg" + i, "id", getPackageName()))).isChecked()) {
                int i2 = iArr[this.plBitDef][i];
                int i3 = i2 / 8;
                bArr[i3] = (byte) (((byte) (1 << ((byte) (i2 % 8)))) | bArr[i3]);
            }
        }
        byte[] bArr2 = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 17, (byte) 0, (byte) 96};
        byte[] bArr3 = new byte[43];
        bArr3[0] = (byte) -124;
        bArr3[1] = (byte) ((int) (255 & this.plID));
        bArr3[2] = (byte) ((int) (this.plID >> 8));
        bArr3[3] = (byte) ((int) (this.plID >> 16));
        bArr3[4] = (byte) ((int) (this.plID >> 24));
        bArr3[5] = (byte) -70;
        bArr3[6] = (byte) 29;
        bArr3[7] = (byte) 0;
        bArr3[8] = (byte) 0;
        for (i2 = 0; i2 < 23; i2++) {
            bArr3[i2 + 9] = bArr[i2];
        }
        byte[] GetCRC = CRCCalc.GetCRC(bArr, 23);
        bArr3[32] = GetCRC[0];
        bArr3[33] = GetCRC[1];
        for (i2 = 0; i2 < 7; i2++) {
            bArr3[i2 + 34] = bArr2[i2];
        }
        GetCRC = CRCCalc.GetCRC(bArr3, 41);
        bArr3[41] = GetCRC[0];
        bArr3[42] = GetCRC[1];
        String str = "";
        for (i2 = 0; i2 < bArr3.length; i2++) {
            str = str + String.format("%02X", new Object[]{Byte.valueOf(bArr3[i2])});
        }
        ((TextView) findViewById(R.id.label_dbgbs)).setText("Data update: " + str);
        PP4C.sendPP4C(getApplicationContext(), bArr3, 43, this.donglever, 15, audioTrack);
    }

    public void sendPage(View view) {
        new Thread(new Runnable() {
            public void run() {
                MainActivity.this.handler.post(new Runnable() {
                    public void run() {
                        MainActivity.this.sendPageUpdate();
                    }
                });
            }
        }).start();
    }

    void sendPageUpdate() {
        byte[] bArr = new byte[11];
        EditText editText = (EditText) findViewById(R.id.etxtPage);
        if (!editText.getText().toString().matches("")) {
            int i;
            byte[] bytes = editText.getText().toString().getBytes();
            for (int i2 = 1; i2 < 9; i2++) {
                if (((RadioButton) findViewById(getResources().getIdentifier("raddur" + i2, "id", getPackageName()))).isChecked()) {
                    i = (byte) i2;
                    break;
                }
            }
            i = 1;
            if (i == 8) {
                i = -128;
            }
            bArr[0] = (byte) -124;
            bArr[1] = (byte) 0;
            bArr[2] = (byte) 0;
            bArr[3] = (byte) 0;
            bArr[4] = (byte) 0;
            bArr[5] = (byte) -85;
            bArr[6] = (byte) (i | ((bytes[0] & 7) << 3));
            bArr[7] = (byte) 0;
            bArr[8] = (byte) 0;
            byte[] GetCRC = CRCCalc.GetCRC(bArr, 9);
            bArr[9] = GetCRC[0];
            bArr[10] = GetCRC[1];
            PP4C.sendPP4C(getApplicationContext(), bArr, 11, this.donglever, 60, audioTrack);
        }
    }

    void sendSegUpdate() {
        int[][] iArr = new int[][]{new int[]{174, 162, 70, 24, 82, 128, 116, 150, 139, 47, 1, 58, 104, 93, 175, 163, 71, 25, 83, 129, 117}, new int[]{139, 104, 12, 1, 47, 93, 58, 162, 128, 36, 24, 70, 116, 82, 138, 103, 11, 0, 46, 92, 57}, new int[]{145, 110, 18, 7, 53, 99, 64, 168, 134, 42, 30, 76, 122, 88, 144, 109, 17, 6, 52, 98, 63}, new int[]{146, SELECT_PHOTO, 54, 8, 65, 157, 111, 170, 124, 78, 32, 90, 182, 136, 147, 101, 55, 9, 66, 158, 112}, new int[]{172, 160, 68, 34, 80, TransportMediator.KEYCODE_MEDIA_PLAY, 114, 173, 161, 69, 35, 81, TransportMediator.KEYCODE_MEDIA_PAUSE, 115, 149, 138, 46, 11, 57, 103, 92}};
        byte[] bArr = new byte[23];
        for (int i = 0; i < 21; i++) {
            if (((ToggleButton) findViewById(getResources().getIdentifier("seg" + i, "id", getPackageName()))).isChecked()) {
                int i2 = iArr[this.plBitDef][i];
                int i3 = i2 / 8;
                bArr[i3] = (byte) (((byte) (1 << ((byte) (i2 % 8)))) | bArr[i3]);
            }
        }
        byte[] bArr2 = new byte[]{(byte) 0, (byte) 0, (byte) 9, (byte) 0, (byte) 16, (byte) 0, (byte) 49};
        byte[] bArr3 = new byte[43];
        bArr3[0] = (byte) -124;
        bArr3[1] = (byte) ((int) (255 & this.plID));
        bArr3[2] = (byte) ((int) (this.plID >> 8));
        bArr3[3] = (byte) ((int) (this.plID >> 16));
        bArr3[4] = (byte) ((int) (this.plID >> 24));
        bArr3[5] = (byte) -70;
        bArr3[6] = (byte) 0;
        bArr3[7] = (byte) 0;
        bArr3[8] = (byte) 0;
        for (i2 = 0; i2 < 23; i2++) {
            bArr3[i2 + 9] = bArr[i2];
        }
        byte[] GetCRC = CRCCalc.GetCRC(bArr, 23);
        bArr3[32] = GetCRC[0];
        bArr3[33] = GetCRC[1];
        for (i2 = 0; i2 < 7; i2++) {
            bArr3[i2 + 34] = bArr2[i2];
        }
        GetCRC = CRCCalc.GetCRC(bArr3, 41);
        bArr3[41] = GetCRC[0];
        bArr3[42] = GetCRC[1];
        String str = "";
        for (i2 = 0; i2 < bArr3.length; i2++) {
            str = str + String.format("%02X", new Object[]{Byte.valueOf(bArr3[i2])});
        }
        ((TextView) findViewById(R.id.label_dbgbs)).setText("Segment bitstream: " + str);
        PP4C.sendPP4C(getApplicationContext(), bArr3, 43, this.donglever, 15, audioTrack);
    }

    public void sendSegs(View view) {
        new Thread(new Runnable() {
            public void run() {
                MainActivity.this.handler.post(new Runnable() {
                    public void run() {
                        MainActivity.this.sendSegUpdate();
                    }
                });
            }
        }).start();
    }

    public void setdonglever(View view) {
        if (((CheckBox) findViewById(R.id.chk_donglever)).isChecked()) {
            this.donglever = 2;
        } else {
            this.donglever = 1;
        }
        if (((CheckBox) findViewById(R.id.chk_donglebt)).isChecked()) {
            this.donglever = 3;
        }
        Editor edit = getSharedPreferences("Pricehax", 0).edit();
        edit.putInt("donglever", this.donglever);
        edit.commit();
    }
}
