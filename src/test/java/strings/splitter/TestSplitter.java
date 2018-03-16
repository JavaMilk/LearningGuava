package strings.splitter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;


/**
 * @author mindawei
 * @date 2018/3/15
 *
 * https://github.com/google/guava/wiki/StringsExplained#splitter
 */
public class TestSplitter {

    @Test
    public void testSplitter(){

        Joiner joiner = Joiner.on(",");
        Iterable<String> result;

        // 按字符划分 Splitter.on(char)
        result = Splitter.on(',').split("foo,bar,qux");
        Assert.assertEquals(joiner.join(result),"foo,bar,qux");

        // 按字 CharMatcher 划分 Splitter.on(CharMatcher)
        result = Splitter.on(CharMatcher.anyOf(";,.")).split("foo;bar,qux");
        Assert.assertEquals(joiner.join(result),"foo,bar,qux");

        // 按字符串划分 Splitter.on(String)
        result = Splitter.on("，").split("foo,bar,qux");
        Assert.assertEquals(joiner.join(result),"foo,bar,qux");

        // 按正则表达式划分 Splitter.onPattern(String)  or Splitter.on(Pattern)
        result = Splitter.onPattern("\\s+").split("foo bar qux");
        Assert.assertEquals(joiner.join(result),"foo,bar,qux");

        // 按固定长度划分 Splitter.fixedLength(int)
        result = Splitter.fixedLength(3).split("foobarqux");
        Assert.assertEquals(joiner.join(result),"foo,bar,qux");

        // 去除结果中的空字符串
        result = Splitter.on(",").omitEmptyStrings().split("foo,,,bar,qux");
        Assert.assertEquals(joiner.join(result),"foo,bar,qux");

        // 结果前后去除空格
        result = Splitter.on(",").trimResults().split("foo  ,bar,qux");
        Assert.assertEquals(joiner.join(result),"foo,bar,qux");

        // 只是去除前后的，内部不去除
        result = Splitter.on(",").trimResults().split("foo  ,b a r,qux");
        Assert.assertEquals(joiner.join(result),"foo,b a r,qux");

        // 结果前后去除 "_"
        result =  Splitter.on(',').trimResults(CharMatcher.is('_')).split("_a ,_b_ ,c__");
        Assert.assertEquals(joiner.join(result),"a ,b_ ,c");

        // 达到指定个数后不再划分
        result = Splitter.on("_").limit(2).split("foo_bar_qux");
        Assert.assertEquals(joiner.join(result),"foo,bar_qux");
    }

    @Test
    public void testMapSplitter() {

        Joiner.MapJoiner mapJoiner = Joiner.on(",").withKeyValueSeparator("->");
        Map<String,String> map;

        // 使用 withKeyValueSeparator 转换成 Splitter.MapSplitter
        map = Splitter.on("#").withKeyValueSeparator(":").split("1:2#3:4");//{}
        Assert.assertEquals(mapJoiner.join(map),"1->2,3->4");

        // MapSplitter 对键值对个格式有着严格的校验，下面的例子拆分会抛出异常。
        boolean throwIllegalArgumentException = false;
        try{
            Splitter.on("#").withKeyValueSeparator(":").split("1:2#3:4:5");
        }catch (IllegalArgumentException e){
            throwIllegalArgumentException = true;
        }
        Assert.assertEquals(true,throwIllegalArgumentException);
    }

}
