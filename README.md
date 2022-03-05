# 定时输入工具

本工具是 [定时输入工具（Java 实现）](https://github.com/dhy2000/TimeInput) 的 Scala 版本。使用方法大致相同：

## 输入格式

每一行输入都匹配正则表达式 `^\[\d+(?:\.\d+)?\].*$`。

## 命令行中

```shell
$ java -jar Trivial-TimedInput.jar < input.txt | java -jar Program.jar
```

## 内嵌到 Java 项目中

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        InputStream input = new TimedInput(System.in).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String string;
        while ((string = reader.readLine()) != null) {
            System.out.println(string);
        }
    }
}
```
