package android.support.v4.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalBroadcastManager {
    private static final boolean DEBUG = false;
    static final int MSG_EXEC_PENDING_BROADCASTS = 1;
    private static final String TAG = "LocalBroadcastManager";
    private static LocalBroadcastManager mInstance;
    private static final Object mLock = new Object();
    private final HashMap<String, ArrayList<ReceiverRecord>> mActions = new HashMap();
    private final Context mAppContext;
    private final Handler mHandler;
    private final ArrayList<BroadcastRecord> mPendingBroadcasts = new ArrayList();
    private final HashMap<BroadcastReceiver, ArrayList<IntentFilter>> mReceivers = new HashMap();

    private static class BroadcastRecord {
        final Intent intent;
        final ArrayList<ReceiverRecord> receivers;

        BroadcastRecord(Intent intent, ArrayList<ReceiverRecord> arrayList) {
            this.intent = intent;
            this.receivers = arrayList;
        }
    }

    private static class ReceiverRecord {
        boolean broadcasting;
        final IntentFilter filter;
        final BroadcastReceiver receiver;

        ReceiverRecord(IntentFilter intentFilter, BroadcastReceiver broadcastReceiver) {
            this.filter = intentFilter;
            this.receiver = broadcastReceiver;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(128);
            stringBuilder.append("Receiver{");
            stringBuilder.append(this.receiver);
            stringBuilder.append(" filter=");
            stringBuilder.append(this.filter);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    private LocalBroadcastManager(Context context) {
        this.mAppContext = context;
        this.mHandler = new Handler(context.getMainLooper()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        LocalBroadcastManager.this.executePendingBroadcasts();
                        return;
                    default:
                        super.handleMessage(message);
                        return;
                }
            }
        };
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void executePendingBroadcasts() {
        /*
        r8 = this;
        r2 = 0;
    L_0x0001:
        r1 = r8.mReceivers;
        monitor-enter(r1);
        r0 = r8.mPendingBroadcasts;	 Catch:{ all -> 0x003f }
        r0 = r0.size();	 Catch:{ all -> 0x003f }
        if (r0 > 0) goto L_0x000e;
    L_0x000c:
        monitor-exit(r1);	 Catch:{ all -> 0x003f }
        return;
    L_0x000e:
        r4 = new android.support.v4.content.LocalBroadcastManager.BroadcastRecord[r0];	 Catch:{ all -> 0x003f }
        r0 = r8.mPendingBroadcasts;	 Catch:{ all -> 0x003f }
        r0.toArray(r4);	 Catch:{ all -> 0x003f }
        r0 = r8.mPendingBroadcasts;	 Catch:{ all -> 0x003f }
        r0.clear();	 Catch:{ all -> 0x003f }
        monitor-exit(r1);	 Catch:{ all -> 0x003f }
        r1 = r2;
    L_0x001c:
        r0 = r4.length;
        if (r1 >= r0) goto L_0x0001;
    L_0x001f:
        r5 = r4[r1];
        r3 = r2;
    L_0x0022:
        r0 = r5.receivers;
        r0 = r0.size();
        if (r3 >= r0) goto L_0x0042;
    L_0x002a:
        r0 = r5.receivers;
        r0 = r0.get(r3);
        r0 = (android.support.v4.content.LocalBroadcastManager.ReceiverRecord) r0;
        r0 = r0.receiver;
        r6 = r8.mAppContext;
        r7 = r5.intent;
        r0.onReceive(r6, r7);
        r0 = r3 + 1;
        r3 = r0;
        goto L_0x0022;
    L_0x003f:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x003f }
        throw r0;
    L_0x0042:
        r0 = r1 + 1;
        r1 = r0;
        goto L_0x001c;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.content.LocalBroadcastManager.executePendingBroadcasts():void");
    }

    public static LocalBroadcastManager getInstance(Context context) {
        LocalBroadcastManager localBroadcastManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new LocalBroadcastManager(context.getApplicationContext());
            }
            localBroadcastManager = mInstance;
        }
        return localBroadcastManager;
    }

    public void registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        synchronized (this.mReceivers) {
            ReceiverRecord receiverRecord = new ReceiverRecord(intentFilter, broadcastReceiver);
            ArrayList arrayList = (ArrayList) this.mReceivers.get(broadcastReceiver);
            if (arrayList == null) {
                arrayList = new ArrayList(1);
                this.mReceivers.put(broadcastReceiver, arrayList);
            }
            arrayList.add(intentFilter);
            for (int i = 0; i < intentFilter.countActions(); i++) {
                String action = intentFilter.getAction(i);
                arrayList = (ArrayList) this.mActions.get(action);
                if (arrayList == null) {
                    arrayList = new ArrayList(1);
                    this.mActions.put(action, arrayList);
                }
                arrayList.add(receiverRecord);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean sendBroadcast(android.content.Intent r17) {
        /*
        r16 = this;
        r0 = r16;
        r13 = r0.mReceivers;
        monitor-enter(r13);
        r2 = r17.getAction();	 Catch:{ all -> 0x00ff }
        r0 = r16;
        r1 = r0.mAppContext;	 Catch:{ all -> 0x00ff }
        r1 = r1.getContentResolver();	 Catch:{ all -> 0x00ff }
        r0 = r17;
        r3 = r0.resolveTypeIfNeeded(r1);	 Catch:{ all -> 0x00ff }
        r5 = r17.getData();	 Catch:{ all -> 0x00ff }
        r4 = r17.getScheme();	 Catch:{ all -> 0x00ff }
        r6 = r17.getCategories();	 Catch:{ all -> 0x00ff }
        r1 = r17.getFlags();	 Catch:{ all -> 0x00ff }
        r1 = r1 & 8;
        if (r1 == 0) goto L_0x015d;
    L_0x002b:
        r1 = 1;
        r12 = r1;
    L_0x002d:
        if (r12 == 0) goto L_0x005d;
    L_0x002f:
        r1 = "LocalBroadcastManager";
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ff }
        r7.<init>();	 Catch:{ all -> 0x00ff }
        r8 = "Resolving type ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x00ff }
        r7 = r7.append(r3);	 Catch:{ all -> 0x00ff }
        r8 = " scheme ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x00ff }
        r7 = r7.append(r4);	 Catch:{ all -> 0x00ff }
        r8 = " of intent ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x00ff }
        r0 = r17;
        r7 = r7.append(r0);	 Catch:{ all -> 0x00ff }
        r7 = r7.toString();	 Catch:{ all -> 0x00ff }
        android.util.Log.v(r1, r7);	 Catch:{ all -> 0x00ff }
    L_0x005d:
        r0 = r16;
        r1 = r0.mActions;	 Catch:{ all -> 0x00ff }
        r7 = r17.getAction();	 Catch:{ all -> 0x00ff }
        r1 = r1.get(r7);	 Catch:{ all -> 0x00ff }
        r0 = r1;
        r0 = (java.util.ArrayList) r0;	 Catch:{ all -> 0x00ff }
        r8 = r0;
        if (r8 == 0) goto L_0x015a;
    L_0x006f:
        if (r12 == 0) goto L_0x0089;
    L_0x0071:
        r1 = "LocalBroadcastManager";
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ff }
        r7.<init>();	 Catch:{ all -> 0x00ff }
        r9 = "Action list: ";
        r7 = r7.append(r9);	 Catch:{ all -> 0x00ff }
        r7 = r7.append(r8);	 Catch:{ all -> 0x00ff }
        r7 = r7.toString();	 Catch:{ all -> 0x00ff }
        android.util.Log.v(r1, r7);	 Catch:{ all -> 0x00ff }
    L_0x0089:
        r1 = 0;
        r10 = 0;
        r11 = r1;
    L_0x008c:
        r1 = r8.size();	 Catch:{ all -> 0x00ff }
        if (r11 >= r1) goto L_0x016d;
    L_0x0092:
        r1 = r8.get(r11);	 Catch:{ all -> 0x00ff }
        r0 = r1;
        r0 = (android.support.v4.content.LocalBroadcastManager.ReceiverRecord) r0;	 Catch:{ all -> 0x00ff }
        r9 = r0;
        if (r12 == 0) goto L_0x00b6;
    L_0x009c:
        r1 = "LocalBroadcastManager";
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ff }
        r7.<init>();	 Catch:{ all -> 0x00ff }
        r14 = "Matching against filter ";
        r7 = r7.append(r14);	 Catch:{ all -> 0x00ff }
        r14 = r9.filter;	 Catch:{ all -> 0x00ff }
        r7 = r7.append(r14);	 Catch:{ all -> 0x00ff }
        r7 = r7.toString();	 Catch:{ all -> 0x00ff }
        android.util.Log.v(r1, r7);	 Catch:{ all -> 0x00ff }
    L_0x00b6:
        r1 = r9.broadcasting;	 Catch:{ all -> 0x00ff }
        if (r1 == 0) goto L_0x00c9;
    L_0x00ba:
        if (r12 == 0) goto L_0x0172;
    L_0x00bc:
        r1 = "LocalBroadcastManager";
        r7 = "  Filter's target already added";
        android.util.Log.v(r1, r7);	 Catch:{ all -> 0x00ff }
        r1 = r10;
    L_0x00c4:
        r7 = r11 + 1;
        r10 = r1;
        r11 = r7;
        goto L_0x008c;
    L_0x00c9:
        r1 = r9.filter;	 Catch:{ all -> 0x00ff }
        r7 = "LocalBroadcastManager";
        r1 = r1.match(r2, r3, r4, r5, r6, r7);	 Catch:{ all -> 0x00ff }
        if (r1 < 0) goto L_0x0102;
    L_0x00d3:
        if (r12 == 0) goto L_0x00f1;
    L_0x00d5:
        r7 = "LocalBroadcastManager";
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ff }
        r14.<init>();	 Catch:{ all -> 0x00ff }
        r15 = "  Filter matched!  match=0x";
        r14 = r14.append(r15);	 Catch:{ all -> 0x00ff }
        r1 = java.lang.Integer.toHexString(r1);	 Catch:{ all -> 0x00ff }
        r1 = r14.append(r1);	 Catch:{ all -> 0x00ff }
        r1 = r1.toString();	 Catch:{ all -> 0x00ff }
        android.util.Log.v(r7, r1);	 Catch:{ all -> 0x00ff }
    L_0x00f1:
        if (r10 != 0) goto L_0x0175;
    L_0x00f3:
        r1 = new java.util.ArrayList;	 Catch:{ all -> 0x00ff }
        r1.<init>();	 Catch:{ all -> 0x00ff }
    L_0x00f8:
        r1.add(r9);	 Catch:{ all -> 0x00ff }
        r7 = 1;
        r9.broadcasting = r7;	 Catch:{ all -> 0x00ff }
        goto L_0x00c4;
    L_0x00ff:
        r1 = move-exception;
        monitor-exit(r13);	 Catch:{ all -> 0x00ff }
        throw r1;
    L_0x0102:
        if (r12 == 0) goto L_0x0172;
    L_0x0104:
        switch(r1) {
            case -4: goto L_0x0164;
            case -3: goto L_0x0161;
            case -2: goto L_0x0167;
            case -1: goto L_0x016a;
            default: goto L_0x0107;
        };
    L_0x0107:
        r1 = "unknown reason";
    L_0x0109:
        r7 = "LocalBroadcastManager";
        r9 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ff }
        r9.<init>();	 Catch:{ all -> 0x00ff }
        r14 = "  Filter did not match: ";
        r9 = r9.append(r14);	 Catch:{ all -> 0x00ff }
        r1 = r9.append(r1);	 Catch:{ all -> 0x00ff }
        r1 = r1.toString();	 Catch:{ all -> 0x00ff }
        android.util.Log.v(r7, r1);	 Catch:{ all -> 0x00ff }
        r1 = r10;
        goto L_0x00c4;
    L_0x0123:
        r1 = r10.size();	 Catch:{ all -> 0x00ff }
        if (r2 >= r1) goto L_0x0136;
    L_0x0129:
        r1 = r10.get(r2);	 Catch:{ all -> 0x00ff }
        r1 = (android.support.v4.content.LocalBroadcastManager.ReceiverRecord) r1;	 Catch:{ all -> 0x00ff }
        r3 = 0;
        r1.broadcasting = r3;	 Catch:{ all -> 0x00ff }
        r1 = r2 + 1;
        r2 = r1;
        goto L_0x0123;
    L_0x0136:
        r0 = r16;
        r1 = r0.mPendingBroadcasts;	 Catch:{ all -> 0x00ff }
        r2 = new android.support.v4.content.LocalBroadcastManager$BroadcastRecord;	 Catch:{ all -> 0x00ff }
        r0 = r17;
        r2.<init>(r0, r10);	 Catch:{ all -> 0x00ff }
        r1.add(r2);	 Catch:{ all -> 0x00ff }
        r0 = r16;
        r1 = r0.mHandler;	 Catch:{ all -> 0x00ff }
        r2 = 1;
        r1 = r1.hasMessages(r2);	 Catch:{ all -> 0x00ff }
        if (r1 != 0) goto L_0x0157;
    L_0x014f:
        r0 = r16;
        r1 = r0.mHandler;	 Catch:{ all -> 0x00ff }
        r2 = 1;
        r1.sendEmptyMessage(r2);	 Catch:{ all -> 0x00ff }
    L_0x0157:
        monitor-exit(r13);	 Catch:{ all -> 0x00ff }
        r1 = 1;
    L_0x0159:
        return r1;
    L_0x015a:
        monitor-exit(r13);	 Catch:{ all -> 0x00ff }
        r1 = 0;
        goto L_0x0159;
    L_0x015d:
        r1 = 0;
        r12 = r1;
        goto L_0x002d;
    L_0x0161:
        r1 = "action";
        goto L_0x0109;
    L_0x0164:
        r1 = "category";
        goto L_0x0109;
    L_0x0167:
        r1 = "data";
        goto L_0x0109;
    L_0x016a:
        r1 = "type";
        goto L_0x0109;
    L_0x016d:
        if (r10 == 0) goto L_0x015a;
    L_0x016f:
        r1 = 0;
        r2 = r1;
        goto L_0x0123;
    L_0x0172:
        r1 = r10;
        goto L_0x00c4;
    L_0x0175:
        r1 = r10;
        goto L_0x00f8;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.content.LocalBroadcastManager.sendBroadcast(android.content.Intent):boolean");
    }

    public void sendBroadcastSync(Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        synchronized (this.mReceivers) {
            ArrayList arrayList = (ArrayList) this.mReceivers.remove(broadcastReceiver);
            if (arrayList == null) {
                return;
            }
            for (int i = 0; i < arrayList.size(); i++) {
                IntentFilter intentFilter = (IntentFilter) arrayList.get(i);
                for (int i2 = 0; i2 < intentFilter.countActions(); i2++) {
                    String action = intentFilter.getAction(i2);
                    ArrayList arrayList2 = (ArrayList) this.mActions.get(action);
                    if (arrayList2 != null) {
                        int i3 = 0;
                        while (i3 < arrayList2.size()) {
                            int i4;
                            if (((ReceiverRecord) arrayList2.get(i3)).receiver == broadcastReceiver) {
                                arrayList2.remove(i3);
                                i4 = i3 - 1;
                            } else {
                                i4 = i3;
                            }
                            i3 = i4 + 1;
                        }
                        if (arrayList2.size() <= 0) {
                            this.mActions.remove(action);
                        }
                    }
                }
            }
        }
    }
}
