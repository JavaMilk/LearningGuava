package strings.joiner;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

/**
 * @author mindawei
 * @date 2018/3/15
 *
 * https://github.com/google/guava/wiki/StringsExplained#joiner
 */
public class TestJoiner {

    @Test
    public void testJoin() {
        String result;

        // 跳过 null 值
        result = Joiner.on("; ").skipNulls().join("Harry", null, "Ron", "Hermione");
        Assert.assertEquals(result, "Harry; Ron; Hermione");

        // 替换 null 值
        result = Joiner.on("; ").useForNull("null").join("Harry", null, "Ron", "Hermione");
        Assert.assertEquals(result, "Harry; null; Ron; Hermione");

        // 使用在对象上，会调用对象的 toString() 函数
        result = Joiner.on(",").join(Arrays.asList(1, 5, 7));
        Assert.assertEquals(result, "1,5,7");

        // MapJoiner 的使用，将 map 转换为字符串
        Map map = ImmutableMap.of("k1", "v1", "k2", "v2");
        result = Joiner.on("; ").withKeyValueSeparator("=").join(map);
        Assert.assertEquals(result, "k1=v1; k2=v2");
    }

}
