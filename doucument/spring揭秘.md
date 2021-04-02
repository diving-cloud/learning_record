## 依赖注入

参考：https://www.zhihu.com/question/23277575

总结下：依赖的对象不再自己new ，而是直接让容器提供，这样就不需要自己知道要依赖的对象的构造方法，因为你拿到的是现成的对象不需要自己new。

**控制反转（Inversion of Control）** 就是依赖倒置原则的一种代码设计的思路。具体采用的方法就是所谓的**依赖注入（Dependency Injection）**。其实这些概念初次接触都会感到云里雾里的。说穿了，这几种概念的关系大概如下：

![preview](spring揭秘.assets/v2-ee924f8693cff51785ad6637ac5b21c1_r.jpg)

例子：

![preview](spring揭秘.assets/v2-8ec294de7d0f9013788e3fb5c76069ef_r.jpg)

![preview](spring揭秘.assets/v2-82e0c12a1b26f7979ed9241e169affda_r.jpg)

![preview](spring揭秘.assets/v2-99ad2cd809fcb86dd791ff7f65fb1779_r.jpg)