package im.langchainjava.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteArrays {

    public static byte[] floatArrayToByteArray(float[] floatArray) throws IOException {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bas);) {
            for (float f : floatArray) {
                dos.write(getBytesLittleEndianOrder(f));
            }
            return bas.toByteArray();
        }
    }
    private static byte[] getBytesLittleEndianOrder(float f) {
        int intBits =  Float.floatToIntBits(f);
        return new byte[]{(byte) intBits, (byte) (intBits >> 8), (byte) (intBits >> 16), (byte) (intBits >> 24)};
    }

    // public static byte[] FloatArrayToByteArray(float[] data){
    //     byte[] Resutl = {};

    //     for (int i = 0; i < data.length; i++){
    //         byte[] intToBytes2 = intToBytes2(Float.floatToIntBits(data[i]));
    //         byte[] temp = new byte[4];
    //         temp[0] = intToBytes2[3];
    //         temp[1] = intToBytes2[2];
    //         temp[2] = intToBytes2[1];
    //         temp[3] = intToBytes2[0];
    //         Resutl = concat(Resutl,temp);
    //     }

    //     return Resutl;
    // }

    
    /** 
     * 将int类型的数据转换为byte数组 原理：将int数据中的四个byte取出，分别存储 
     *  
     * @param n  int数据 
     * @return 生成的byte数组 
     */  
    public static byte[] intToBytes2(int n) {  
        byte[] b = new byte[4];  
        for (int i = 0; i < 4; i++) {  
            b[i] = (byte) (n >> (24 - i * 8));  
        }  
        return b;  
    }  
    
    /** 
     * 将byte数组转换为int数据 
     *  
     * @param b 字节数组 
     * @return 生成的int数据 
     */  
    public static int byteToInt2(byte[] b) { 
        return (((int) b[0]) << 24) + (((int) b[1]) << 16)  
                + (((int) b[2]) << 8) + b[3];  
    } 

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c= new byte[a.length+b.length];

        System.arraycopy(a, 0, c, 0, a.length);

        System.arraycopy(b, 0, c, a.length, b.length);

        return c;

    }


    // public static float[] ByteArrayToFloatArray(byte[] data){
    //     float[] result = new float[data.length / 4];
    //     int temp = 0;
    //     for (int i = 0; i < data.length; i += 4){
    //         temp = temp | (data[i] & 0xff) << 0;
    //         temp = temp | (data[i+1] & 0xff) << 8;
    //         temp = temp | (data[i+2] & 0xff) << 16;
    //         temp = temp | (data[i+3] & 0xff) << 24;
    //         result[i / 4] = Float.intBitsToFloat(temp);
    //     }
    //     return result;
    // }
}