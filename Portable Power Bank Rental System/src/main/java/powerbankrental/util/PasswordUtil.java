package powerbankrental.util;

/*这个完全不会了，抄AI的
https://www.bilibili.com/video/BV1f754zdEpR/
一个很好的学习视频*/
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.regex.Pattern;

//密码工具类，负责密码加密和验证
public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

    private static final int SALT_LENGTH = 8;//盐值长度（字节）
    private static final int HASH_LENGTH = 32;//哈希密钥长度（字节）
    private static final int ITERATION_COUNT = 10000;//迭代次数，增加计算成本

    //静态初始化代码块，注册Bouncy Castle提供者
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            logger.info("Bouncy Castle Provider not found");
        }
    }

    //生产随机盐值
    /*generateSalt() 方法是用来创建新的随机盐值，并以 Base64 字符串格式返回，
    通常在用户首次创建密码时使用*/
    public static String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    //密码哈希-使用PBKDF2算法和Bouncy Castle实现
    public static String hashPassword(String password, String salt){
        if (password == null || salt == null){
            logger.error("Password or salt is null");
            throw new IllegalArgumentException("Password or salt must not be null");
        }

        try{
            /*hashPassword() 方法中的 Base64.getDecoder().decode(salt)
            是将已有的 Base64 编码盐值转回字节数组，以便用于哈希计算
            如果每次都使用 generateSalt() 生成新盐值，将无法验证之前创建的密码哈希
            */
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            byte[] passwordBytes = password.getBytes();

            //使用PKCS5S2参数生成器（PBKDF2实现）
            PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator();
            generator.init(passwordBytes, saltBytes, ITERATION_COUNT);

            //生产密钥参数
            KeyParameter keyParameter = (KeyParameter)generator.generateDerivedParameters(HASH_LENGTH * 8);
            byte[] derivedKey = keyParameter.getKey();

            return Base64.getEncoder().encodeToString(derivedKey);
        }catch (Exception e){
            logger.error("Error while hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    //验证密码
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

    //生产安全的随机密码
    public static String generateRandomPassword(int length) {
        if (length < 8 ) {
            length = 8;//确保 最小长度为8
        }

        final String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        final String numericChars = "0123456789";
        final String specialChars = "!@#$%^&*()_-+=<>?";

        SecureRandom random = new SecureRandom();
        StringBuffer password = new StringBuffer();

        //确保密码至少包含大小写字母、数字、特殊字符各一个
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numericChars.charAt(random.nextInt(numericChars.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        //填充剩余长度的随机字符
        String allChars = upperChars + lowerChars + numericChars + specialChars;
        for (int i = 4; i < length; i++) {
            int randomIndex = random.nextInt(allChars.length());
            password.append(allChars.charAt(randomIndex));
        }

        //打乱字符顺序
        char[] passwordChars = password.toString().toCharArray();
        for (int i = 0; i < passwordChars.length; i++) {
            int randomIndex = random.nextInt(passwordChars.length);
            char tempChar = passwordChars[i];
            passwordChars[i] = passwordChars[randomIndex];
            passwordChars[randomIndex] = tempChar;
        }

        return new String(passwordChars);
    }

    //检查密码强度
    public static int checkPasswordStrength(String password){
        if (password == null || password.isEmpty()){
            return 0;
        }
        int score = 0;

        //长度检查
        if (password.length() >= 8) score++;
        if (password.length() >= 16) score++;

        //复杂度检查-0=非常弱，1=弱，2=中等，3=强，4=非常强
        if(Pattern.compile("[A-Z]]").matcher(password).find())score++;
        if(Pattern.compile("[a-z]]").matcher(password).find())score++;
        if(Pattern.compile("[0-9]]").matcher(password).find())score++;
        if(Pattern.compile("[A-Za-z0-9]]").matcher(password).find())score++;

        return Math.min(4,  score / 2);
    }

    //生产密码重置令牌
    public static String generatePasswordResetToken(String email) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] passwordBytes = new byte[32];
        secureRandom.nextBytes(passwordBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(passwordBytes);
    }
}
