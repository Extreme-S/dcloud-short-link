package org.example.biz;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.example.util.CommonUtil;
import org.junit.Test;


@Slf4j
public class ShortLinkTest {


    @Test
    public void testMurmurHash() {
        for (int i = 0; i < 5; i++) {
            String originalUrl =
                "https://xdclass.net?id=" + CommonUtil.generateUUID() + "pwd=" + CommonUtil.getStringNumRandom(7);

            long murmur3_32 = Hashing.murmur3_32().hashUnencodedChars(originalUrl).padToLong();
            log.info("murmur3_32={}", murmur3_32);
        }
    }
}
