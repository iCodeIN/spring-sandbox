package at.favre.server.springsandbox;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.crypto.HKDF;
import at.favre.lib.crypto.SingleStepKdf;
import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bkdf.BKDF;
import at.favre.lib.crypto.bkdf.KeyDerivationFunction;
import at.favre.lib.crypto.bkdf.PasswordHashVerifier;
import at.favre.lib.crypto.bkdf.PasswordHasher;
import at.favre.lib.crypto.bkdf.Version;
import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMask;
import at.favre.lib.idmask.IdMasks;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These are some simple tests for checking JDK 11 compatibility of some libraries
 */
@SpringBootTest
class TestFavreLibsForCompatibility {

    @Test
    void testBcrypt() {
        String password = "1234";
        String bcryptHashString = BCrypt.withDefaults().hashToString(8, password.toCharArray());
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), bcryptHashString);

        assertTrue(result.verified);

        System.out.println(result);
    }

    @Test
    void testBytes() {
        Bytes b = Bytes.wrap(Bytes.random(16));  //reuse given reference
        b.copy().reverse(); //reverse the bytes on a copied instance
        String hex = b.encodeHex(); //encode base16/hex
        System.out.println(hex);

        assertEquals(Bytes.parseHex(hex), b);
    }

    @Test
    void testHkdf() throws Exception {
        //if no dynamic salt is available, a static salt is better than null
        byte[] staticSalt32Byte = new byte[]{(byte) 0xDA, (byte) 0xAC, 0x3E, 0x10, 0x55, (byte) 0xB5, (byte) 0xF1, 0x3E, 0x53, (byte) 0xE4, 0x70, (byte) 0xA8, 0x77, 0x79, (byte) 0x8E, 0x0A, (byte) 0x89, (byte) 0xAE, (byte) 0x96, 0x5F, 0x19, 0x5D, 0x53, 0x62, 0x58, (byte) 0x84, 0x2C, 0x09, (byte) 0xAD, 0x6E, 0x20, (byte) 0xD4};

        //example input
        byte[] sharedSecret = Bytes.random(16).array();

        HKDF hkdf = HKDF.fromHmacSha256();

        //extract the "raw" data to create output with concentrated entropy
        byte[] pseudoRandomKey = hkdf.extract(staticSalt32Byte, sharedSecret);

        //create expanded bytes for e.g. AES secret key and IV
        byte[] expandedAesKey = hkdf.expand(pseudoRandomKey, "aes-key".getBytes(StandardCharsets.UTF_8), 16);
        byte[] expandedIv = hkdf.expand(pseudoRandomKey, "aes-iv".getBytes(StandardCharsets.UTF_8), 16);

        //Example boilerplate encrypting a simple string with created key/iv
        SecretKey key = new SecretKeySpec(expandedAesKey, "AES"); //AES-128 key
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(expandedIv));
        byte[] encrypted = cipher.doFinal("my secret message".getBytes(StandardCharsets.UTF_8));

        System.out.println(Bytes.wrap(encrypted).encodeHex());
    }

    @Test
    void testIdMask() {
        byte[] key = Bytes.random(16).array();
        long id = 127836129873L;

        IdMask<Long> idMask = IdMasks.forLongIds(Config.builder(key).build());

        String maskedId = idMask.mask(id);
        long originalId = idMask.unmask(maskedId);

        assertEquals(id, originalId);

        System.out.println(maskedId);
    }

    @Test
    void testSingleStepKdf() {
        byte[] sharedSecret = Bytes.random(16).array();
        byte[] salt = Bytes.random(16).array();
        byte[] otherInfo = "macKey".getBytes();
        byte[] keyMaterial = SingleStepKdf.fromHmacSha256().derive(sharedSecret, 32, salt, otherInfo);
        SecretKey secretKey = new SecretKeySpec(keyMaterial, "AES");

        System.out.println(Bytes.wrap(secretKey.getEncoded()).encodeHex());
    }

    @Test
    void testBkdf() {
        PasswordHasher hasher = BKDF.createPasswordHasher();

        char[] pw = "secret".toCharArray();
        int costFactor = 6; // same as with bcrypt 4-31 doubling the iterations every increase

        String hash = hasher.hash(pw, costFactor);

        PasswordHashVerifier verifier = BKDF.createPasswordHashVerifier();
        boolean verified = verifier.verify(pw, hash);

        assertTrue(verified);

        byte[] salt = Bytes.random(16).array();
        KeyDerivationFunction kdf = new KeyDerivationFunction.Default(Version.HKDF_HMAC512);
        byte[] aesKey = kdf.derive(salt, pw, costFactor, Bytes.from("aes-key").array(), 16);

        SecretKey aesSecretKey = new SecretKeySpec(aesKey, "AES");
        System.out.println(Bytes.wrap(aesSecretKey.getEncoded()).encodeHex());

    }
}

