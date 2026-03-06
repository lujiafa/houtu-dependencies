package io.github.lujiafa.houtu.util.constant;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2017年4月19日
 * @Description 通用分隔符常量类
 */
public interface CharConstant {

    /** 英文空''字符 **/
    char EMPTY_CHAR = 0x00;
    /** 英文空""字符串 **/
    String EMPTY = Character.toString(EMPTY_CHAR);

    /** 英文空格" "字符 **/
    char SPACE_CHAR = 0x20;
    /** 英文空格" "字符串 **/
    String SPACE = Character.toString(SPACE_CHAR);

    /** 英文点"."字符 **/
    char POINT_CHAR = 0x2E;
    /** 英文点"."字符串 **/
    String POINT = Character.toString(POINT_CHAR);
    /** 英文点"."正则表达式分割字符串 **/
    String POINT_REGEX = "\\" + POINT;

    /** 英文逗号","字符 **/
    char COMMA_CHAR = 0x2C;
    /** 英文逗号","字符串 **/
    String COMMA = Character.toString(COMMA_CHAR);

    /** 英文分号";"字符 **/
    char SEMICOLON_CHAR = 0x3B;
    /** 英文分号";"字符串 **/
    String SEMICOLON = Character.toString(SEMICOLON_CHAR);

    /** 英文冒号":"字符 **/
    char COLON_CHAR = 0x3A;
    /** 英文冒号":"字符串 **/
    String COLON = Character.toString(COLON_CHAR);

    /** 英文连字符"-"字符 **/
    char HYPHEN_CHAR = 0x2D;
    /** 英文连字符"-"字符串 **/
    String HYPHEN = Character.toString(HYPHEN_CHAR);

    /** 英文斜杠"/"字符 **/
    char SLASH_CHAR = 0x2F;
    /** 英文斜杠"/"字符串 **/
    String SLASH = Character.toString(SLASH_CHAR);

    /** 英文下划线"_"字符 **/
    char UNDERLINE_CHAR = 0x5F;
    /** 英文下划线"_"字符串 **/
    String UNDERLINE = Character.toString(UNDERLINE_CHAR);

    /** 英文等号"="字符 **/
    char EQUAL_CHAR = 0x3D;
    /** 英文等号"="字符串 **/
    String EQUAL = Character.toString(EQUAL_CHAR);

    /** 英文问号"?"字符 **/
    char QUESTION_CHAR = 0x3F;
    /** 英文问号"?"字符串 **/
    String QUESTION = Character.toString(QUESTION_CHAR);

    /** 英文和号"&"字符 **/
    char AMPERSAND_CHAR = 0x26;
    /** 英文和号"&"字符串 **/
    String AMPERSAND = Character.toString(AMPERSAND_CHAR);

    /** 英文井号"#"字符 **/
    char HASH_CHAR = 0x23;
    /** 英文井号"#"字符串 **/
    String HASH = Character.toString(HASH_CHAR);

    /** 英文竖线"|"字符 **/
    char VERTICAL_BAR_CHAR = 0x7C;
    /** 英文竖线"|"字符串 **/
    String VERTICAL_BAR = Character.toString(VERTICAL_BAR_CHAR);
    /** 英文竖线"|"正则表达式分割字符串 **/
    String VERTICAL_BAR_REGEX = "\\" + VERTICAL_BAR;

}