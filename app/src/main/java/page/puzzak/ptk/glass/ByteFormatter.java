package page.puzzak.ptk.glass;

import java.text.DecimalFormat;

public class ByteFormatter {
    public static String formatBytes(long bytes) {
        double kilobytes = bytes / 1024.0;
        double megabytes = kilobytes / 1024.0;

        DecimalFormat format = new DecimalFormat("0.00");

        if (megabytes >= 1.0) {
            return format.format(megabytes) + " MB";
        } else if (kilobytes >= 1.0) {
            return format.format(kilobytes) + " KB";
        } else {
            return bytes + " Bytes";
        }
    }
}
