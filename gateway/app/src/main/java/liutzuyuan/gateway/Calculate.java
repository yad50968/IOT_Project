package liutzuyuan.gateway;


import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;

import threegpp.milenage.Milenage;
import threegpp.milenage.MilenageBufferFactory;
import threegpp.milenage.MilenageResult;
import threegpp.milenage.biginteger.BigIntegerBuffer;
import threegpp.milenage.biginteger.BigIntegerBufferFactory;
import threegpp.milenage.cipher.Ciphers;

import static liutzuyuan.gateway.Setting.amf;
import static liutzuyuan.gateway.Setting.op;


class Calculate {


    final private static char[] VALID_CHARACTERS =
            "ABCDEF0123456879".toCharArray();


    static String sha3(String input) {

        SHA3.DigestSHA3 md = new SHA3.DigestSHA3(512);
        md.update(input.getBytes());
        return bytesToHex(md.digest()).toLowerCase();
    }


    private static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = VALID_CHARACTERS[v >>> 4];
            hexChars[j * 2 + 1] = VALID_CHARACTERS[v & 0x0F];
        }
        return new String(hexChars);
    }


    static String xor(String in, String out) {

        char[] keyarray = out.toCharArray(); //Can be any chars, and any length array
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < in.length(); i++) {
            output.append((char) (in.charAt(i) ^ keyarray[i % keyarray.length]));
        }
        return output.toString();
    }


    static String encrypt(String input, String key) {
        // do xor for encrypt
        return xor(input, key);
    }


    static String decrypt(String input, String key) {
        // do xor for decrypt
        return xor(input, key);
    }


    static String generateRandom256bit01() throws NoSuchAlgorithmException {
        SecureRandom number = SecureRandom.getInstance("SHA1PRNG");

        String randomString = "";
        for (int i = 0; i < 256; i++) {
            randomString += number.nextInt(10000) % 2;
        }
        return randomString;
    }


    private static String generateRandomLenString(int len) {

        SecureRandom srand = new SecureRandom();
        Random rand = new Random();
        char[] buff = new char[len];

        for (int i = 0; i < len; ++i) {
            if ((i % 10) == 0) {
                rand.setSeed(srand.nextLong());
            }
            buff[i] = VALID_CHARACTERS[rand.nextInt(VALID_CHARACTERS.length)];
        }
        return new String(buff);
    }


    static Map<Object, Object> generateAuthVector(String Ku) throws NoSuchAlgorithmException, DecoderException {


        Map<MilenageResult, byte[]> av = new HashMap<>();
        Map<Object, Object> result = new HashMap<>();
        Ku = checkKuLen(Ku);

        String rand = generateRandomLenString(32);
        String sqn = generateRandomLenString(12);

        byte[] K = Hex.decodeHex(Ku.toCharArray());
        byte[] RAND = Hex.decodeHex(rand.toCharArray());
        byte[] OP = Hex.decodeHex(op.toCharArray());
        byte[] SQN = Hex.decodeHex(sqn.toCharArray());
        byte[] AMF = Hex.decodeHex(amf.toCharArray());

        MilenageBufferFactory<BigIntegerBuffer> bufferFactory = BigIntegerBufferFactory.getInstance();
        Cipher cipher = Ciphers.createRijndaelCipher(K);
        byte[] OPc = Milenage.calculateOPc(OP, cipher, bufferFactory);

        Milenage<BigIntegerBuffer> milenage = new Milenage<>(OPc, cipher, bufferFactory);

        try {
            av = milenage.calculateAll(RAND, SQN, AMF, Executors.newCachedThreadPool());
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        result.put("av", av);
        result.put("RAND", rand);
        result.put("SQN", sqn);
        result.put("AMF", amf);

        return result;

    }


    private static String checkKuLen(String Ku) {

        int needLen = 32 - Ku.length();
        if (needLen > 0) {
            StringUtils.rightPad(Ku, needLen, '0');
        } else if (needLen < 0) {
            Ku = Ku.substring(0, 32);
        }
        return Ku;
    }
}

