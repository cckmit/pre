package com.xd.pre.modules.px.cotroller;
import java.io.ByteArrayOutputStream;

public class ModifiedBase64 {
    public static void main(String[] args) throws Exception {
        ModifiedBase64 zObj = new ModifiedBase64();
        String strIn = "DwO4EJPrDJCmY2G2EJq5EG==";
//        strIn = args[0];
        String strOut = new String(zObj.m23209eC(strIn));
        System.out.println(strOut);

    }

    private static char[] aae = {'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static byte[] aaf = new byte[128];

    public ModifiedBase64() {
        try {
            m23208pf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* renamed from: pf */
    public static void m23208pf() throws Exception {
        int i = 0;
        int i2 = 0;
        while (true) {
            byte[] bArr = aaf;
            if (i2 <= bArr.length - 1) {
                bArr[i2] = -1;
                i2++;
            } else {
                break;
            }
        }

        while (true) {
            char[] cArr = aae;
            if (i <= cArr.length - 1) {
                aaf[cArr[i]] = (byte) i;
                i++;
            } else {
                return;
            }
        }
    }

    /* renamed from: r */
    public static String m23207r(byte[] bArr) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= bArr.length - 1; i += 3) {
            byte[] bArr2 = new byte[4];
            byte b = 0;
            for (int i2 = 0; i2 <= 2; i2++) {
                int i3 = i + i2;
                if (i3 <= bArr.length - 1) {
                    bArr2[i2] = (byte) (b | ((bArr[i3] & 255) >>> ((i2 * 2) + 2)));
                    b = (byte) ((((bArr[i3] & 255) << (((2 - i2) * 2) + 2)) & 255) >>> 2);
                } else {
                    bArr2[i2] = b;
                    b = 0x40;  // SignedBytes.MAX_POWER_OF_TWO;
                }
            }
            bArr2[3] = b;
            for (int i4 = 0; i4 <= 3; i4++) {
                if (bArr2[i4] <= 63) {
                    sb.append(aae[bArr2[i4]]);
                } else {
                    sb.append('=');
                }
            }
        }
        return sb.toString();
    }

    /* renamed from: eC */
    public static byte[] m23209eC(String str) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = str.getBytes();
        byte[] bArr = new byte[bytes.length];
        for (int i = 0; i <= bytes.length - 1; i++) {
            bArr[i] = aaf[bytes[i]];
        }
        for (int i2 = 0; i2 <= bArr.length - 1; i2 += 4) {
            byte[] bArr2 = new byte[3];
            int i3 = 0;
            for (int i4 = 0; i4 <= 2; i4++) {
                int i5 = i2 + i4;
                int i6 = i5 + 1;
                if (i6 <= bArr.length - 1 && bArr[i6] >= 0) {
                    bArr2[i4] = (byte) ((((bArr[i5] & 255) << ((i4 * 2) + 2)) & 255) | ((byte) ((bArr[i6] & 255) >>> (((2 - (i4 + 1)) * 2) + 2))));
                    i3++;
                }
            }
            for (int i7 = 0; i7 <= i3 - 1; i7++) {
                byteArrayOutputStream.write(bArr2[i7]);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }
}
