package effectivejava.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hzk
 * @version 1.0
 * @description
 * @date 2020-12-20 9:33
 */
public class B {
    public static void main(String[] args) {

    }

    public void addElement() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        A.currentStatement.add(list);
    }
}
