package com.atlassian.sourcemap;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceMapperParser {

    Map<String, SourceMap> map = new HashMap<>();

    /**
     * 慢操作
     */
    @SneakyThrows(FileNotFoundException.class)
    public void init() {
        String dir = "D:\\project\\miya-ui\\dist\\sm";
        for (File file : FileUtil.loopFiles(dir)) {
            String str = IoUtil.read(new FileReader(file));
            initSourceMapFile(file.getName(), str);
        }
    }

    public void initSourceMapFile(String sourceMapFile, String sourceMapContent) {
        SourceMap sm = new SourceMapImpl(sourceMapContent);
        map.put(sourceMapFile, sm);
    }

    /**
     * 将压缩后的JS错误堆栈转换成源码错误堆栈
     * @param errorStack
     * @return
     */
    public String parse(String errorStack) {
        Pattern pattern = Pattern.compile(" \\(?(\\S+):(\\d*):(\\d*)\\)?");
        StringBuilder sb = new StringBuilder();

        String[] lines = errorStack.split("\n");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String name = matcher.group(1);
                int row = Integer.parseInt(matcher.group(2));
                int col = Integer.parseInt(matcher.group(3));
                Mapping mapping = map.get(name + ".map").getMapping(row - 1, col - 1);
                String decodeLine = StrUtil.format("{}:{}:{}", mapping.getSourceFileName(), mapping.getSourceLine() + 1, mapping.getSourceColumn() + 1);
                sb.append(" ").append(matcher.replaceAll(decodeLine)).append("\n");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String errorStack = "error handler Error\n" +
                "    at _.submit (main.81b9f13543e1ed0b.js:1:213449)\n" +
                "    at main.81b9f13543e1ed0b.js:1:217312\n" +
                "    at Xu (main.81b9f13543e1ed0b.js:13:410137)\n" +
                "    at Object.S [as next] (main.81b9f13543e1ed0b.js:13:410276)\n" +
                "    at A.next (main.81b9f13543e1ed0b.js:13:133748)\n" +
                "    at T._next (main.81b9f13543e1ed0b.js:13:133432)\n" +
                "    at T.next (main.81b9f13543e1ed0b.js:13:133127)\n" +
                "    at main.81b9f13543e1ed0b.js:13:131097\n" +
                "    at t (main.81b9f13543e1ed0b.js:13:157978)\n" +
                "    at c7.next (main.81b9f13543e1ed0b.js:13:130936)";

        SourceMapperParser sourceMapperParser = new SourceMapperParser();
        sourceMapperParser.init();
        String parseErrorStack = sourceMapperParser.parse(errorStack);
        System.out.println(parseErrorStack);
    }
}
