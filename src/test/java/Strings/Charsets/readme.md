[Charsets](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Charsets.html)
---
 
不要使用以下写法：
 ```
 try {
   bytes = string.getBytes("UTF-8");
 } catch (UnsupportedEncodingException e) {
   // how can this possibly happen?
   throw new AssertionError(e);
 }
 ```
 
 而是使用以下写法：
 ```
 bytes = string.getBytes(Charsets.UTF_8);
 ```
 
 [Charsets](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/Charsets.html) 提供了6种所有 Java 实现平台都能支持的字符集 `Charset `。具体如下：
 * `US_ASCII = Charset.forName("US-ASCII");`
 * `ISO_8859_1 = Charset.forName("ISO-8859-1");`
 * `UTF_8 = Charset.forName("UTF-8");`
 * `UTF_16BE = Charset.forName("UTF-16BE");`
 * `Charset UTF_16LE = Charset.forName("UTF-16LE");`
 * `Charset UTF_16 = Charset.forName("UTF-16");`
 
 可以看到，该类主要是提供一些 `java.nio.charset.Charset` 的静态实例。
 
 
 
 
 
 
 

 