package com.xdev.ooms.sharedkernel.qr;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.function.Function;

@Component
public class CodeGenerator {

    private static final int CODE_LENGTH = 6;
    private static final String HEX_CHARS = "0123456789ABCDEF";
    private final SecureRandom random = new SecureRandom();

    public String generateUnique(Function<String, Boolean> existsChecker) {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(HEX_CHARS.charAt(random.nextInt(HEX_CHARS.length())));
            }
            code = sb.toString();
        } while (existsChecker.apply(code));
        return code;
    }
}