package Strings.CharMatcher;

import com.google.common.base.CharMatcher;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mindawei
 * @date 2018/3/17
 *
 * https://github.com/google/guava/wiki/StringsExplained#charmatcher
 */
public class TestCharMatcher {

    @Test
    public void testCharMatcher(){

        String result;

        // 将匹配 eko 中任何一个字符的各个序列替换为 -
        result = CharMatcher.anyOf("eko").collapseFrom("bookkeeper", '-');
        Assert.assertEquals("b-p-r",result);

        // 判断是否完全匹配
        boolean matchAll;
        matchAll= CharMatcher.is('a').matchesAllOf("aaaab");
        Assert.assertEquals(false,matchAll);

        matchAll= CharMatcher.is('a').matchesAllOf("aaaaa");
        Assert.assertEquals(true,matchAll);

        // 移除指定字符
        result = CharMatcher.is('a').removeFrom("bazaar");
        Assert.assertEquals("bzr",result);

        // 保留指定字符
        result = CharMatcher.is('a').retainFrom("bazaar");
        Assert.assertEquals("aaa",result);

        // 前后删除匹配 ab 中的任何一个字符
        result = CharMatcher.anyOf("ab").trimFrom("abacatbab");
        Assert.assertEquals("cat",result);

        // 将 a 替换为 oo
        result = CharMatcher.is('a').replaceFrom("yaha", "oo");
        Assert.assertEquals("yoohoo",result);

        // 统计 a 出现的个数
        int number = CharMatcher.is('a').countIn("aaa");
        Assert.assertEquals(3,number);

        // 找到 a 第一次出现的位置
        int index = CharMatcher.is('a').indexIn("java");
        Assert.assertEquals(1,index);


    }

}
