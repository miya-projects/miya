package com.miya.system.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version 1.0
 * 敏感词过滤
 */
public class SensitivewordFilter {
    @SuppressWarnings("rawtypes")
    private final Map sensitiveWordMap;

    /**
     * 构造函数，初始化敏感词库
     */
    public SensitivewordFilter() {
        this(new ArrayList<>());
    }

    public SensitivewordFilter(List<String> keyWordList) {
        sensitiveWordMap = new SensitiveWordInit().initKeyWord(keyWordList);
    }

    /**
     * 判断文字是否包含敏感字符
     *
     * @param txt       文字
     * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
     */
    public boolean isContaintSensitiveWord(String txt, MatchType matchType) {
        boolean flag = false;
        for (int i = 0; i < txt.length(); i++) {
            int matchFlag = this.checkSensitiveWord(txt, i, matchType); //判断是否包含敏感字符
            if (matchFlag > 0) {    //大于0存在，返回true
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 获取文字中的敏感词
     *
     * @param txt       文字
     * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
     */
    public Set<String> getSensitiveWord(String txt, MatchType matchType) {
        Set<String> sensitiveWordList = new HashSet<String>();
        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i, matchType);    //判断是否包含敏感字符
            if (length > 0) {    //存在,加入list中
                sensitiveWordList.add(txt.substring(i, i + length));
                i = i + length - 1;    //减1的原因，是因为for会自增
            }
        }
        return sensitiveWordList;
    }

	/**
	 * 默认替换 ，最大规则匹配 。
	 * 默认将敏感字符串替换为相同长度的 *
	 * @param txt
	 */
	public String replaceSensitiveWord(String txt) {
		return replaceSensitiveWord(txt, MatchType.MAX_MATCH_TYPE, "*");
	}

	/**
	 * 替换敏感词
	 * @param txt
	 * @param matchType
	 * @param replaceChar
	 */
    public String replaceSensitiveWord(String txt, MatchType matchType, String replaceChar) {
        String resultTxt = txt;
        Set<String> set = getSensitiveWord(txt, matchType);     //获取所有的敏感词
        Iterator<String> iterator = set.iterator();
        String word = null;
        String replaceString = null;
        while (iterator.hasNext()) {
            word = iterator.next();
            replaceString = getReplaceChars(replaceChar, word.length());
            resultTxt = resultTxt.replaceAll(word, replaceString);
        }
        return resultTxt;
    }


    /**
     * 获取替换字符串  默认将敏感词替换为 相同长度*
     * @param replaceChar
     * @param length
     * @version 1.0
     */
    private String getReplaceChars(String replaceChar, int length) {
        String resultReplace = replaceChar;
        for (int i = 1; i < length; i++) {
            resultReplace += replaceChar;
        }

        return resultReplace;
    }

    /**
     * 检查文字中是否包含敏感字符，检查规则如下：<br>
     *
     * @param txt
     * @param beginIndex
     * @param matchType
     */
    @SuppressWarnings({"rawtypes"})
    public int checkSensitiveWord(String txt, int beginIndex, MatchType matchType) {
        boolean flag = false;    //敏感词结束标识位：用于敏感词只有1位的情况
        int matchFlag = 0;     //匹配标识数默认为0
        char word = 0;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            nowMap = (Map) nowMap.get(word);     //获取指定key
            if (nowMap != null) {     //存在，则判断是否为最后一个
                matchFlag++;     //找到相应key，匹配标识+1
                if ("1".equals(nowMap.get("isEnd"))) {       //如果为最后一个匹配规则,结束循环，返回匹配标识数
                    flag = true;       //结束标志位为true
                    if (MatchType.MIN_MATCH_TYPE.equals(matchType)) {    //最小规则，直接返回,最大规则还需继续查找
                        break;
                    }
                }
            } else {     //不存在，直接返回
                break;
            }
        }
        if (matchFlag < 2 || !flag) {        //长度必须大于等于1，为词
            matchFlag = 0;
        }
        return matchFlag;
    }

    public static void main(String[] args) {
        List<String> keyList = new ArrayList<String>();
        keyList.add("法轮功");
        SensitivewordFilter filter = new SensitivewordFilter(keyList);
        String string = "太多的伤感情怀也许只局限于饲养基地 荧幕中的情节，主人公尝试着去用某种方式渐渐的很潇洒地释自杀指南怀那些自己经历的伤感。"
                + "然后法轮功 我们的扮演的角色就是跟随着主人公的喜红客联盟 怒哀乐而过于牵强的把自己的情感也附加于银幕情节中，然后感动就流泪，"
                + "难过就躺在某一个人的怀里尽情的阐述心扉或者手机卡复制器一个人一杯红酒一部电影在夜三级片 深人静的晚上，关上电话静静的发呆着。";
        string = filter.replaceSensitiveWord(string);
        System.out.println(string);
    }
}

/**
 * 匹配规则
 */
enum MatchType {
    /**
     * 最小匹配规则
     */
    MIN_MATCH_TYPE,
    /**
     * 最大匹配规则
     */
    MAX_MATCH_TYPE
}

/**
 * 初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 */
class SensitiveWordInit {
    @SuppressWarnings("rawtypes")
    public HashMap sensitiveWordMap;

    public SensitiveWordInit() {
        super();
    }

    /**
     * 初始化敏感过滤词，
     * 组装DFA 算法
     * map格式为{key={isEnd=0,[map]key={isEnd=0,[map]key={.....}}}}
     *
     * @param keyWordList 敏感词list 列表
     */
    @SuppressWarnings("rawtypes")
    public Map initKeyWord(List<String> keyWordList) {
        //获取敏感词set集合
        Set<String> keyWordSet = new HashSet<>(keyWordList);
        //将敏感词库加入到HashMap中
        addSensitiveWordToHashMap(keyWordSet);
        return sensitiveWordMap;
    }

    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
     * 中 = {
     * isEnd = 0
     * 国 = {<br>
     * isEnd = 1
     * 人 = {isEnd = 0
     * 民 = {isEnd = 1}
     * }
     * 男  = {
     * isEnd = 0
     * 人 = {
     * isEnd = 1
     * }
     * }
     * }
     * }
     * 五 = {
     * isEnd = 0
     * 星 = {
     * isEnd = 0
     * 红 = {
     * isEnd = 0
     * 旗 = {
     * isEnd = 1
     * }
     * }
     * }
     * }
     *
     * @param keyWordSet 敏感词库
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addSensitiveWordToHashMap(Set<String> keyWordSet) {
        sensitiveWordMap = new HashMap(keyWordSet.size());     //初始化敏感词容器，减少扩容操作
        String key;
        Map nowMap;
        Map<String, String> newWorMap;
        //迭代keyWordSet
        Iterator<String> iterator = keyWordSet.iterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            if (null != key) {
                nowMap = sensitiveWordMap;
                for (int i = 0; i < key.length(); i++) {
                    char keyChar = key.charAt(i);
                    Object wordMap = nowMap.get(keyChar);
                    if (wordMap != null) {
                        nowMap = (Map) wordMap;
                    } else {
                        newWorMap = new HashMap<>();
                        newWorMap.put("isEnd", "0");
                        nowMap.put(keyChar, newWorMap);
                        nowMap = newWorMap;
                    }

                    if (i == key.length() - 1) {
                        nowMap.put("isEnd", "1");    //最后一个
                    }
                }
            }
        }
    }

}
