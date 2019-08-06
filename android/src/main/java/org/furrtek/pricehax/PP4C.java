package org.furrtek.pricehax;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import java.io.IOException;

public class PP4C {
    static AudioManager mAudioManager;
    static int origVolume;
    static double[] sample = new double[48000];

    private static void sendData(byte[] bArr) {
        Log.d("BT SEND", "SENDING DATA...");
        try {
            byte[] bArr2 = new byte[128];
            MainActivity.outStream.write(bArr);
            do {
            } while (MainActivity.inStream.available() <= 0);
            Log.d("DATA", "Received:" + MainActivity.inStream.read(bArr2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendPP4C(Context context, byte[] bArr, int i, int i2, int i3, AudioTrack audioTrack) {
        int i4;
        double[] dArr = new double[256];
        byte[] bArr2 = new byte[96000];
        if (i2 == 3) {
            byte[] bArr3 = new byte[55];
            bArr3[0] = (byte) -86;
            bArr3[1] = (byte) (((i3 - 1) * 4) >> 8);
            bArr3[2] = (byte) (((i3 - 1) * 4) & MotionEventCompat.ACTION_MASK);
            bArr3[3] = (byte) i;
            for (i4 = 0; i4 < i; i4++) {
                bArr3[i4 + 4] = bArr[i4];
            }
            String str = "";
            for (i4 = 0; i4 < bArr3.length; i4++) {
                str = str + String.format("%02X", new Object[]{Byte.valueOf(bArr3[i4])});
            }
            sendData(bArr3);
            Log.d("DATA", "SENT " + str);
        }
        if (i2 < 3) {
            double d;
            if (i2 == 2) {
                dArr[0] = (double) (i3 & 3);
                dArr[1] = (double) ((i3 >> 2) & 3);
                dArr[2] = (double) ((i3 >> 4) & 3);
                dArr[3] = (double) ((i3 >> 6) & 3);
                i++;
                for (i4 = 1; i4 < i; i4++) {
                    dArr[(i4 * 4) + 0] = (double) (bArr[i4 - 1] & 3);
                    dArr[(i4 * 4) + 1] = (double) ((bArr[i4 - 1] >> 2) & 3);
                    dArr[(i4 * 4) + 2] = (double) ((bArr[i4 - 1] >> 4) & 3);
                    dArr[(i4 * 4) + 3] = (double) ((bArr[i4 - 1] >> 6) & 3);
                }
            } else {
                for (i4 = 0; i4 < i; i4++) {
                    dArr[(i4 * 4) + 0] = (double) (bArr[i4] & 3);
                    dArr[(i4 * 4) + 1] = (double) ((bArr[i4] >> 2) & 3);
                    dArr[(i4 * 4) + 2] = (double) ((bArr[i4] >> 4) & 3);
                    dArr[(i4 * 4) + 3] = (double) ((bArr[i4] >> 6) & 3);
                }
            }
            mAudioManager = (AudioManager) context.getSystemService("audio");
            origVolume = mAudioManager.getStreamVolume(3);
            mAudioManager.setStreamVolume(3, (int) (((double) mAudioManager.getStreamMaxVolume(3)) * (0.6d + ((double) (((float) MainActivity.transmitVolume) / 100.0f)))), 0);
            for (i4 = 0; i4 < 48000; i4++) {
                sample[i4] = 0.0d;
            }
            double d2 = 0.0d;
            for (int i5 = 0; i5 < 15; i5++) {
                int i6 = 0;
                while (i6 < 40) {
                    d = 1.0d + d2;
                    sample[(int) d2] = 0.2d;
                    i6++;
                    d2 = d;
                }
                i4 = 0;
                while (i4 < 40) {
                    double d3 = 1.0d + d2;
                    sample[(int) d2] = -0.2d;
                    i4++;
                    d2 = d3;
                }
            }
            sample[(int) d2] = 1.0d;
            sample[((int) d2) + 1] = 1.0d;
            sample[((int) d2) + 2] = -1.0d;
            sample[((int) d2) + 3] = -1.0d;
            d = d2;
            for (int i7 = 0; i7 < i * 4; i7++) {
                if (dArr[i7] == 0.0d) {
                    d += 12.0d;
                }
                if (dArr[i7] == 1.0d) {
                    d += 12.0d;
                }
                if (dArr[i7] == 2.0d) {
                    d += 6.0d;
                }
                if (dArr[i7] == 3.0d) {
                    d += 6.0d;
                }
                sample[(int) d] = 1.0d;
                sample[((int) d) + 1] = 1.0d;
                sample[((int) d) + 2] = -1.0d;
                sample[((int) d) + 3] = -1.0d;
                if (dArr[i7] == 0.0d) {
                    d += 12.0d;
                }
                if (dArr[i7] == 1.0d) {
                    d += 6.0d;
                }
                if (dArr[i7] == 2.0d) {
                    d += 12.0d;
                }
                if (dArr[i7] == 3.0d) {
                    d += 6.0d;
                }
                sample[(int) d] = 1.0d;
                sample[((int) d) + 1] = 1.0d;
                sample[((int) d) + 2] = -1.0d;
                sample[((int) d) + 3] = -1.0d;
            }
            i4 = 0;
            for (double d4 : sample) {
                short s = (short) ((int) (32767.0d * d4));
                int i8 = i4 + 1;
                bArr2[i4] = (byte) (s & MotionEventCompat.ACTION_MASK);
                i4 = i8 + 1;
                bArr2[i8] = (byte) ((s & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>> 8);
            }
            audioTrack.write(bArr2, 0, 48000);
            audioTrack.setNotificationMarkerPosition(48000);
            try {
                audioTrack.play();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            audioTrack.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
                public void onMarkerReached(AudioTrack audioTrack) {
                    try {
                        audioTrack.stop();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    PP4C.mAudioManager.setStreamVolume(3, PP4C.origVolume, 0);
                }

                public void onPeriodicNotification(AudioTrack audioTrack) {
                }
            });
        }
    }
}
