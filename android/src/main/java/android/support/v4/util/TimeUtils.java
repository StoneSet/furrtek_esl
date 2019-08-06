package android.support.v4.util;

import java.io.PrintWriter;

public class TimeUtils {
    public static final int HUNDRED_DAY_FIELD_LEN = 19;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static char[] sFormatStr = new char[24];
    private static final Object sFormatSync = new Object();

    private static int accumField(int i, int i2, boolean z, int i3) {
        return (i > 99 || (z && i3 >= 3)) ? i2 + 3 : (i > 9 || (z && i3 >= 2)) ? i2 + 2 : (z || i > 0) ? i2 + 1 : 0;
    }

    public static void formatDuration(long j, long j2, PrintWriter printWriter) {
        if (j == 0) {
            printWriter.print("--");
        } else {
            formatDuration(j - j2, printWriter, 0);
        }
    }

    public static void formatDuration(long j, PrintWriter printWriter) {
        formatDuration(j, printWriter, 0);
    }

    public static void formatDuration(long j, PrintWriter printWriter, int i) {
        synchronized (sFormatSync) {
            printWriter.print(new String(sFormatStr, 0, formatDurationLocked(j, i)));
        }
    }

    public static void formatDuration(long j, StringBuilder stringBuilder) {
        synchronized (sFormatSync) {
            stringBuilder.append(sFormatStr, 0, formatDurationLocked(j, 0));
        }
    }

    private static int formatDurationLocked(long j, int i) {
        if (sFormatStr.length < i) {
            sFormatStr = new char[i];
        }
        char[] cArr = sFormatStr;
        int i2;
        if (j == 0) {
            i2 = i - 1;
            while (i2 < 0) {
                cArr[0] = ' ';
            }
            cArr[0] = '0';
            return 1;
        }
        char c;
        int i3;
        int i4;
        int i5;
        int i6;
        int accumField;
        if (j > 0) {
            c = '+';
        } else {
            j = -j;
            c = '-';
        }
        int i7 = (int) (j % 1000);
        int floor = (int) Math.floor((double) (j / 1000));
        i2 = 0;
        if (floor > SECONDS_PER_DAY) {
            i2 = floor / SECONDS_PER_DAY;
            floor -= SECONDS_PER_DAY * i2;
        }
        if (floor > SECONDS_PER_HOUR) {
            i3 = floor / SECONDS_PER_HOUR;
            floor -= i3 * SECONDS_PER_HOUR;
            i4 = i3;
        } else {
            i4 = 0;
        }
        if (floor > SECONDS_PER_MINUTE) {
            i3 = floor / SECONDS_PER_MINUTE;
            i5 = i3;
            i6 = floor - (i3 * SECONDS_PER_MINUTE);
        } else {
            i5 = 0;
            i6 = floor;
        }
        if (i != 0) {
            accumField = accumField(i2, 1, false, 0);
            accumField += accumField(i4, 1, accumField > 0, 2);
            accumField += accumField(i5, 1, accumField > 0, 2);
            accumField += accumField(i6, 1, accumField > 0, 2);
            i3 = 0;
            floor = (accumField(i7, 2, true, accumField > 0 ? 3 : 0) + 1) + accumField;
            while (floor < i) {
                cArr[i3] = ' ';
                floor++;
                i3++;
            }
        } else {
            i3 = 0;
        }
        cArr[i3] = c;
        i3++;
        Object obj = i != 0 ? 1 : null;
        int printField = printField(cArr, i2, 'd', i3, false, 0);
        printField = printField(cArr, i4, 'h', printField, printField != i3, obj != null ? 2 : 0);
        printField = printField(cArr, i5, 'm', printField, printField != i3, obj != null ? 2 : 0);
        int printField2 = printField(cArr, i6, 's', printField, printField != i3, obj != null ? 2 : 0);
        accumField = (obj == null || printField2 == i3) ? 0 : 3;
        i2 = printField(cArr, i7, 'm', printField2, true, accumField);
        cArr[i2] = 's';
        return i2 + 1;
    }

    private static int printField(char[] cArr, int i, char c, int i2, boolean z, int i3) {
        if (!z && i <= 0) {
            return i2;
        }
        int i4;
        int i5;
        if ((!z || i3 < 3) && i <= 99) {
            i4 = i2;
            i5 = i;
        } else {
            i5 = i / 100;
            cArr[i2] = (char) (i5 + 48);
            i4 = i2 + 1;
            i5 = i - (i5 * 100);
        }
        if ((z && i3 >= 2) || i5 > 9 || i2 != i4) {
            int i6 = i5 / 10;
            cArr[i4] = (char) (i6 + 48);
            i4++;
            i5 -= i6 * 10;
        }
        cArr[i4] = (char) (i5 + 48);
        i4++;
        cArr[i4] = c;
        return i4 + 1;
    }
}
