package dzida.server.app.user;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordHash {
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int iterationIndex = 0;
    private static final int saltIndex = 1;
    private static final int pbkdf2Index = 2;

    // The following constants may be changed without breaking existing hashes.
    private static final int saltBytes = 32;
    private static final int hashBytes = 32;
    private static final int pbkdf2Iterations = 1000;

    /**
     * Compares two byte arrays in length-constant time. This comparison method
     * is used so that password hashes cannot be extracted from an on-line
     * system using a timing attack and then attacked off-line.
     *
     * @param a the first byte array
     * @param b the second byte array
     * @return true if both byte arrays are the same, false if not
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    /**
     * Computes the PBKDF2 hash of a password.
     *
     * @param password   the password to hash.
     * @param salt       the salt
     * @param iterations the iteration count (slowness factor)
     * @param bytes      the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    private static byte[] fromBase64(String src) {
        return Base64.getDecoder().decode(src);
    }

    private static String toBase64(byte[] src) {
        return Base64.getEncoder().encodeToString(src);
    }

    public String createHash(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[saltBytes];
            random.nextBytes(salt);

            // Hash the password
            byte[] hash = pbkdf2(password.toCharArray(), salt, pbkdf2Iterations, hashBytes);
            // format iterations:salt:hash
            return pbkdf2Iterations + ":" + toBase64(salt) + ":" + toBase64(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Can not create a hash for password: " + password);
        }
    }

    /**
     * Validates a password using a hash.
     *
     * @param password the password to check
     * @param goodHash the hash of the valid password
     * @return true if the password is correct, false if not
     */
    public boolean validatePassword(String password, String goodHash) {
        try {
            // Decode the hash into its parameters
            String[] params = goodHash.split(":");
            int iterations = Integer.parseInt(params[iterationIndex]);
            byte[] salt = fromBase64(params[saltIndex]);
            byte[] hash = fromBase64(params[pbkdf2Index]);
            // Compute the hash of the provided password, using the same salt,
            // iteration count, and hash length
            byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations, hash.length);
            // Compare the hashes in constant time. The password is correct if
            // both hashes match.
            return slowEquals(hash, testHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Can not validate password: " + password + " with hash: " + goodHash);
        }
    }
}
