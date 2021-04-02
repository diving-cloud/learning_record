package effectivejava.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hzk
 * @version 1.0
 * @description
 * @date 2020-12-20 9:34
 */
public class C {
    public void addElement() {
        List<String> list = new ArrayList<>();
        list.add("d");
        list.add("e");
        list.add("f");
        A.currentStatement.add(list);
    }
    public static void main(String[] args) {
        A a = new A();
        a.test();
        for (List<String> list : A.currentStatement) {
            System.out.println(list);
        }

    }
}
