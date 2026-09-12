import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.jar.*;

/** Uses the same Java Properties semantics as Gradle. Never prints passwords or key material. */
class AndroidSigningCheck {
    public static void main(String[] args) {
        try {
            if (args.length != 2) throw new IllegalArgumentException("Expected signing <android-dir> or bundle <aab>");
            X509Certificate certificate;
            if (args[0].equals("signing")) {
                Path root = Path.of(args[1]);
                Properties properties = new Properties();
                Path file = root.resolve("keystore.properties");
                if (Files.exists(file)) try (var stream = Files.newInputStream(file)) { properties.load(stream); }
                String store = setting(properties, "storeFile", "ANDROID_UPLOAD_KEYSTORE_PATH");
                String password = setting(properties, "storePassword", "ANDROID_UPLOAD_STORE_PASSWORD");
                String alias = setting(properties, "keyAlias", "ANDROID_UPLOAD_KEY_ALIAS");
                String keyPassword = setting(properties, "keyPassword", "ANDROID_UPLOAD_KEY_PASSWORD");
                KeyStore keys = KeyStore.getInstance(root.resolve(store).toFile(), password.toCharArray());
                if (!(keys.getKey(alias, keyPassword.toCharArray()) instanceof PrivateKey))
                    throw new IllegalArgumentException("Upload alias does not contain a private key");
                certificate = (X509Certificate) keys.getCertificate(alias);
            } else if (args[0].equals("bundle")) {
                certificate = verifyBundle(Path.of(args[1]));
            } else throw new IllegalArgumentException("Unknown signing check");
            certificate.checkValidity();
            if (certificate.getSubjectX500Principal().getName().contains("CN=Android Debug"))
                throw new IllegalArgumentException("Android debug keys cannot be used for a Play release");
            String fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()));
            System.out.println("Upload certificate SHA-256: " + fingerprint);
        } catch (Exception error) {
            // Library errors can contain private paths. Only our configuration errors are printed.
            String detail = error instanceof IllegalArgumentException ? error.getMessage() : error.getClass().getSimpleName();
            System.err.println("Signing check failed: " + detail + ". Check the keystore, alias, passwords, and certificate validity.");
            System.exit(1);
        }
    }

    private static String setting(Properties properties, String key, String variable) {
        String value = System.getenv(variable);
        if (value == null) value = properties.getProperty(key);
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("Missing " + variable + " (or " + key + " in keystore.properties)");
        return value;
    }

    private static X509Certificate verifyBundle(Path path) throws Exception {
        X509Certificate signer = null;
        int entries = 0;
        try (JarFile jar = new JarFile(path.toFile(), true)) {
            var iterator = jar.entries();
            while (iterator.hasMoreElements()) {
                JarEntry entry = iterator.nextElement();
                if (entry.isDirectory() || entry.getName().startsWith("META-INF/")) continue;
                try (var stream = jar.getInputStream(entry)) { stream.transferTo(OutputStream.nullOutputStream()); }
                var certificates = entry.getCertificates();
                if (certificates == null || certificates.length == 0)
                    throw new IllegalArgumentException("Bundle contains unsigned content");
                X509Certificate current = (X509Certificate) certificates[0];
                if (signer != null && !signer.equals(current)) throw new IllegalArgumentException("Bundle contains mixed signing certificates");
                signer = current;
                entries++;
            }
        }
        if (entries == 0 || signer == null) throw new IllegalArgumentException("Bundle has no signed content");
        return signer;
    }
}
