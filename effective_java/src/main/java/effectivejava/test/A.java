package effectivejava.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hzk
 * @version 1.0
 * @description
 * @date 2020-12-20 9:32
 */
public class A {
    public static List<List<String>> currentStatement =null;

    public void test() {
        currentStatement = new ArrayList<>();
        B b = new B();
        C c = new C();
        //这个方法有返回值，并求会修改currentStatment这个变量值
        c.addElement();
        b.addElement();
        for (int i = 0; i < currentStatement.size(); i++) {
            System.out.println(currentStatement.get(i));
        }
    }

}
