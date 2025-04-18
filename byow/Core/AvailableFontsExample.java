package byow.Core;

import java.awt.GraphicsEnvironment;

/*
    检查电脑可用字体
 */
public class AvailableFontsExample {
    public static void main(String[] args) {
        // 获取系统中的所有字体名称
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        // 打印所有字体名称
        System.out.println("Available fonts on this system:");
        for (String fontName : fontNames) {
            System.out.println(fontName);
        }
    }
}