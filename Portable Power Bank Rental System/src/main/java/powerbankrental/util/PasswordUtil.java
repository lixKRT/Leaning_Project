package powerbankrental.util;

/*�����ȫ�����ˣ���AI��
https://www.bilibili.com/video/BV1f754zdEpR/
һ���ܺõ�ѧϰ��Ƶ*/
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.regex.Pattern;

//���빤���࣬����������ܺ���֤
public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

    private static final int SALT_LENGTH = 8;//��ֵ���ȣ��ֽڣ�
    private static final int HASH_LENGTH = 32;//��ϣ��Կ���ȣ��ֽڣ�
    private static final int ITERATION_COUNT = 10000;//�������������Ӽ���ɱ�

    //��̬��ʼ������飬ע��Bouncy Castle�ṩ��
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            logger.info("Bouncy Castle Provider not found");
        }
    }

    //���������ֵ
    /*generateSalt() ���������������µ������ֵ������ Base64 �ַ�����ʽ���أ�
    ͨ�����û��״δ�������ʱʹ��*/
    public static String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    //�����ϣ-ʹ��PBKDF2�㷨��Bouncy Castleʵ��
    public static String hashPassword(String password, String salt){
        if (password == null || salt == null){
            logger.error("Password or salt is null");
            throw new IllegalArgumentException("Password or salt must not be null");
        }

        try{
            /*hashPassword() �����е� Base64.getDecoder().decode(salt)
            �ǽ����е� Base64 ������ֵת���ֽ����飬�Ա����ڹ�ϣ����
            ���ÿ�ζ�ʹ�� generateSalt() ��������ֵ�����޷���֤֮ǰ�����������ϣ
            */
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            byte[] passwordBytes = password.getBytes();

            //ʹ��PKCS5S2������������PBKDF2ʵ�֣�
            PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator();
            generator.init(passwordBytes, saltBytes, ITERATION_COUNT);

            //������Կ����
            KeyParameter keyParameter = (KeyParameter)generator.generateDerivedParameters(HASH_LENGTH * 8);
            byte[] derivedKey = keyParameter.getKey();

            return Base64.getEncoder().encodeToString(derivedKey);
        }catch (Exception e){
            logger.error("Error while hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    //��֤����
    public static boolean verifyPassword(String password, String storeHash, String storeSalt){
        if (password == null || storeHash == null ||  storeSalt == null){
            logger.error("Password or store hash or store salt is null");
            return false;
        }

        try{
            String hashedPassword = hashPassword(password, storeSalt);
            boolean isMatch = storeHash.equals(hashedPassword);

            if (!isMatch){
                logger.debug("Password or store hash does not match");
            }
            return false;
        }catch (Exception e){
            logger.error("Error while verifying password", e);
            return false;
        }
    }

    //������ȫ���������
    public static String generateRandomPassword(int length) {
        if (length < 8 ) {
            length = 8;//ȷ�� ��С����Ϊ8
        }

        final String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        final String numericChars = "0123456789";
        final String specialChars = "!@#$%^&*()_-+=<>?";

        SecureRandom random = new SecureRandom();
        StringBuffer password = new StringBuffer();

        //ȷ���������ٰ�����Сд��ĸ�����֡������ַ���һ��
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numericChars.charAt(random.nextInt(numericChars.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        //���ʣ�೤�ȵ�����ַ�
        String allChars = upperChars + lowerChars + numericChars + specialChars;
        for (int i = 4; i < length; i++) {
            int randomIndex = random.nextInt(allChars.length());
            password.append(allChars.charAt(randomIndex));
        }

        //�����ַ�˳��
        char[] passwordChars = password.toString().toCharArray();
        for (int i = 0; i < passwordChars.length; i++) {
            int randomIndex = random.nextInt(passwordChars.length);
            char tempChar = passwordChars[i];
            passwordChars[i] = passwordChars[randomIndex];
            passwordChars[randomIndex] = tempChar;
        }

        return new String(passwordChars);
    }

    //�������ǿ��
    public static int checkPasswordStrength(String password){
        if (password == null || password.isEmpty()){
            return 0;
        }
        int score = 0;

        //���ȼ��
        if (password.length() >= 8) score++;
        if (password.length() >= 16) score++;

        //���Ӷȼ��-0=�ǳ�����1=����2=�еȣ�3=ǿ��4=�ǳ�ǿ
        if(Pattern.compile("[A-Z]]").matcher(password).find())score++;
        if(Pattern.compile("[a-z]]").matcher(password).find())score++;
        if(Pattern.compile("[0-9]]").matcher(password).find())score++;
        if(Pattern.compile("[A-Za-z0-9]]").matcher(password).find())score++;

        return Math.min(4,  score / 2);
    }

    //����������������
    public static String generatePasswordResetToken(String email) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] passwordBytes = new byte[32];
        secureRandom.nextBytes(passwordBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(passwordBytes);
    }
}
