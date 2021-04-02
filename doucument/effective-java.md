# Chapter 2 创建和销毁对象

## 第1条 考虑用静态工厂方法代替构造器

对于类而言, 最常用的获取实例的方法就是提供一个公有的构造器, 还有一种方法, 就是提供一个公有的静态工厂方法(static factory method), 返回类的实例.

(注意此处的静态工厂方法与设计模式中的工厂方法模式不同.)

提供静态工厂方法而不是公有构造, 这样做有几大优势:

- 静态工厂方法**有名称**. 可以更确切地描述正被返回的对象.
  当一个类需要多个带有相同签名的构造器时, 可以用静态工厂方法, 并且慎重地选择名称以便突出它们之间的区别.
- **不必在每次调用它们的时候都创建一个新对象.** 可以重复利用实例, 进行实例控制. 如果程序经常请求创建相同的对象, 并且创建对象的代价很高, 这项改动可以提升性能. (不可变类, 单例, 枚举).
- **可以返回原类型的子类型对象.** 适用于基于接口的框架, 可以隐藏实现类API, 也可以根据参数返回不同的子类型.
  由于在Java 8之前, 接口不能有    静态方法, 因此按照惯例, 接口Type的静态工厂方法被放在一个名为Types的不可实例化的类中.
  (Java的java.util.Collections). 
- 返回对象的类型可以根据输入的参数而变化. 比如`EnumSet`类的静态工厂, 根据元素的多少返回不同的子类型.
- 返回对象的类型不需要在写这个方法的时候就存在. 服务提供者框架(Service Provider Framework, 如JDBC)的基础, 让客户端与具体实现解耦. 
  Java 6开始提供了`java.util.ServiceLoader`.

静态工厂方法的缺点:

* 类如果不含public或者protected的构造器, 就不能被子类化. (鼓励程序员: 组合优于继承).
* 不容易被程序员发现, 因为静态工厂方法与其他的静态方法没有区别. 在API文档中没有像构造器一样明确标识出来. 可以使用一些惯用的名称来弥补这一劣势:
  - `from`: 类型转换方法.
  - `of`: 聚集方法, 参数为多个, 返回的当前类型的实例包含了它们.
  - `valueOf`: 类型转换方法, 返回的实例与参数具有相同的值.
  - `instance`或`getInstance`: 返回的实例通过参数来描述(并不是和参数有一样的值). 对于单例来说, 该方法没有参数, 返回唯一的实例.
  - `create`或`newInstance`: 像`getInstance`一样, 但`newInstance`能确保返回的每个实例都与其他实例不同.
  - `getType`: 和`getInstance`一样, Type表示返回的对象类型, 在工厂方法处于不同的类中的时候使用.
  - `newType`: 和`newInstance`一样, Type表示返回的对象类型, 在工厂方法处于不同的类中的时候使用.
  - `type`: `getType`和`newType`的简洁替代.


## 第2条 遇到多个构造器参数时要考虑用Builder

静态工厂和构造器有一个共同的局限性: 它们都不能很好地扩展到大量的可选参数.

重载多个构造器方法(telescoping constructor pattern)可行, 但是当有许多参数的时候, 代码会很难写难读.

第二种替代方法是JavaBeans模式, 即一个无参数构造来创建对象, 然后调用setter方法来设置每个参数. 
这种模式也有严重的缺点, 因为构造过程被分到了几个调用中, 在构造过程中JavaBean可能处于不一致的状态.
类无法通过检验构造器参数的有效性来保证一致性. 
另一点是这种模式阻止了把类做成不可变的可能.

第三种方法就是**Builder模式**. 不直接生成想要的对象, 而是利用必要参数调用构造器(或者静态工厂)得到一个builder对象, 然后在builder对象上调用类似setter的方法, 来设置可选参数, 最后调用无参的`build()`方法来生成不可变的对象.

这个Builder是它构建的类的静态成员类.
Builder的setter方法返回Builder本身, 可以链式操作.
Builder模式很适合在继承中使用. 子类`build()`方法返回自己的类型(covariant return typing).

Builder模式的优势: 可读性增强; 可以有多个可变参数; 易于做参数检查和构造约束检查; 比JavaBeans更加安全; 灵活性: 可以利用单个builder构建多个对象, 可以自动填充某些域, 比如自增序列号.

Builder模式的不足: 为了创建对象必须先创建Builder, 在某些十分注重性能的情况下, 可能就成了问题; Builder模式较冗长, 因此只有参数很多时才使用.

```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {
        // Required parameters
        private final int servingSize;
        private final int servings;

        // Optional parameters - initialized to default values
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            calories = val;
            return this;
        }

        public Builder fat(int val) {
            fat = val;
            return this;
        }

        public Builder sodium(int val) {
            sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }

    public static void main(String[] args) {
        NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
                .calories(100).sodium(35).carbohydrate(27).build();
        System.out.println(cocaCola.toString());
    }
```



## 第3条 用私有构造器或者枚举类型强化Singleton属性

Singleton(单例)指仅仅被实例化一次的类. 通常用来代表那些本质上唯一的系统组件. 

使类成为Singleton会使得它的客户端代码测试变得困难, 因为无法给它替换模拟实现, 除非它实现了一个充当其类型的接口.

单例的实现: 私有构造方法, 类中保留一个字段实例(static, final), 用public直接公开字段或者用一个public static的`getInstance()`方法返回该字段.

为了使单例实现序列化(`Serializable`), 仅仅在声明中加上`implements Serializable`是不够的, 为了维护并保证单例, 必须声明所有实例域都是`transient`的, 并提供一个`readResolve()`方法, 返回单例的实例. 
否则每次反序列化一个实例时, 都会创建一个新的实例.

从Java 1.5起, **可以使用枚举来实现单例**: 只需要编写一个包含单个元素的枚举类型.
这种方法无偿地提供了序列化机制, 绝对防止多次实例化.

## 第4条 通过私有构造器强化不可实例化的能力

只包含静态方法和静态域的类名声不太好, 因为有些人会滥用它们来编写过程化的程序. 尽管如此, 它们确实也有特有的用处, 比如:
`java.lang.Math`, `java.util.Arrays`把基本类型的值或数组类型上的相关方法组织起来; `java.util.Collections`把实现特定接口的对象上的静态方法组织起来; 还可以利用这种类把final类上的方法组织起来, 以取代扩展该类的做法.

这种工具类(utility class)不希望被实例化, 然而在缺少显式构造器的情况下, 系统会提供默认构造器, 可能会造成这些类被无意识地实例化.

通过做成抽象类来强制该类不可被实例化, 这是行不通的, 因为可能会造成"这个类是用来被继承的"的误解, 而继承它的子类又可以被实例化.

所以只要让这个类包含一个私有的构造器, 它就不能被实例化了. 进一步地, 可以在这个私有构造器中抛出异常. 

这种做法还会导致这个类不能被子类化, 因为子类构造器必须显式或隐式地调用super构造器. 在这种情况下, 子类就没有可访问的超类构造器可调用了.

## 第5条 优先使用依赖注入而不是直接绑定资源

对于其行为由底层资源参数化的类(比如SpellChecker, 底层资源是dictionary), 静态辅助类和单例都是不合适的实现方式.

一个简单的模式是在创建新实例的时候, 通过构造函数传入资源.

依赖注入(dependency injection): 依赖(dictionary)在spell checker被创建的时候注入(injected).
依赖注入适用于: 构造函数, 静态工厂, builder模式.
优点: 灵活, 复用, 易于测试.

一个有用的变种: 将资源工厂传入构造函数.

依赖注入的framework: Dagger, Guice, Spring.


## 第6条 避免创建不必要的对象

一般来说, 最好能重用对象而不是每次需要的时候创建一个相同功能的新对象. 
如果对象是**不可变的(immutable)**, 它就始终可以被重用.

比如应该用:

```
String s = "bikini";
```

而不是:

```
String s = new String("bikini"); // Don't do this
```

包含相同字符串的字面常量对象是会被重用的(同一个虚拟机).

对于同时提供了静态工厂方法和构造方法的**不可变类**, 通常可以使用静态工厂方法而不是构造器, 以避免创建不必要的对象.
比如`Boolean.valueOf()`. `Boolean(String)`在Java 9已经deprecated了.

用`string.matches()`做字符串正则匹配检查: 重复使用会有性能问题, 因为每次都会创建一个`Pattern`对象. -> 改进: 在类初始化的时候创建一个static final的`Pattern`对象, 然后方法重复利用.

除了重用不可变对象以外, 也可以重用那些已知不会被修改的可变对象. 比如把一个方法中需要用到的不变的数据保存成常量对象(static final), 只在初始化的时候创建一次(static 块), 这样就不用每次调用方法都重复创建.

如果该方法永远不会调用, 那也不需要初始化相关的字段, 可以通过延迟初始化(lazily initializing)把这些对象的初始化放到方法第一次被调用的时候. (但是不建议这样做, 没有性能的显著提高, 并且会使方法看起来复杂.)

如果对象是immutable的, 那么重用的安全性是很明显的. 
其他有些情形则并不总是这么明显了. (适配器(adapter)模式, Map的接口keySet()方法返回同样的Set实例).

Java 1.5中加入了自动装箱(autoboxing), 会创建对象. 所以程序中优先使用基本类型而不是装箱基本类型, 要当心无意识的自动装箱. 

小对象的构造器只做很少量的显式工作, 创建和回收都是很廉价的, 所以通过创建附加的对象提升程序的清晰简洁性也是好事.

通过维护自己的对象池(object pool)来避免创建对象并不是一种好的做法(代码, 内存), 除非池中的对象是非常重量级的. 正确使用的典型: 数据库连接池.

## 第7条 消除过期的对象引用

一个内存泄露的例子: 一个用数组实现的Stack, 依靠size标记来管理栈的深度, 但是这样从栈中弹出来的过期对象并没有被释放. 

称内存泄露为"无意识的对象保持(unintentional object retention)"更为恰当.

修复方法: 一旦对象引用已经过期, 只需清空这些引用即可.

清空对象引用应该是一种例外, 而不是一种规范行为. 
消除过期引用最好的方法是让包含该引用的变量结束其生命周期. 如果你是在最紧凑的作用域范围内定义变量, 这种情形就会自然发生.

一般而言, 只要类是自己管理内存, 程序员就应该警惕内存泄露问题. 一旦元素被释放掉, 则该元素中包含的任何对象引用都应该被清空.

内存泄露的另一个常见来源是缓存. 这个问题有这几种可能的解决方案: 

* 1.缓存项的生命周期由该键的外部引用决定 -> `WeakHashMap`; 
* 2.缓存项的生命周期是否有意义并不是很容易确定 -> 随着时间的推移或者新增项的时候删除没用的项.

内存泄露的第三个常见来源是监听器和其他回调. 如果你实现了一个API, 客户端注册了回调却没有注销, 就会积聚对象. 
API端可以只保存对象的弱引用来确保回调对象生命周期结束后会被垃圾回收. 

## 第8条 避免使用终结方法和清理器

终结方法(finalizer)通常是不可预测的, 也是很危险的, 一般情况下是不必要的. 使用终结方法会导致行为不稳定, 降低性能, 以及可移植性问题.
Java 9废弃了finalizers, 取而代之的是清理器 -> cleaners. cleaners虽然没有finalizers那么危险, 但还是不可预测, 慢, 并且通常是不必要的.

不要把finalizer当成是C++中的析构器(destructors)的对应物. 
在Java中, 当一个对象变得不可到达的时候, 垃圾回收器会回收与该对象相关联的存储空间.

C++的析构器也可以用来回收其他的非内存资源, 而在Java中, 一般用`try-finally`或`try-with-resources`块来完成类似的工作.

终结方法的缺点在于不能保证会被及时地执行. 从一个对象变得不可到达开始, 到它的终结方法被执行, 所花费的时间是任意长的. JVM会延迟执行终结方法. 

及时地执行终结方法正是垃圾回收算法的一个主要功能. 这种算法在不同的JVM上不同. 

Java语言规范不仅不保证终结方法会被及时地执行, 而且根本就不保证它们会被执行. 所以不应该依赖于终结方法来更新重要的持久状态. 

不要被`System.gc()`和`System.runFinalization()`这两个方法所迷惑, 它们确实增加了终结方法被执行的机会, 但是它们并不保证终结方法一定会被执行. 

如果未捕获的异常在终结过程中被抛出来, 那么这种异常可以被忽略, 而且该对象的终结过程也会终止. 

使用终结方法或清洁器有一个严重的性能损失. 

终结方法还有一个严重的安全问题: 使类暴露给了finalizer attacks. -> 抵御: 非final的类提供一个空的finalize方法.

如果类的对象中封装的资源(例如文件或线程)确实需要终止, 应该怎么做才能不用编写终结方法呢? 
只需提供一个显式的终止方法. 并要求该类的客户端在每个实例不再有用的时候调用这个方法. 
实现`AutoCloseable`, 提供一个显式的终止方法`close()`.
注意, 该实例必须记录下自己是否已经被终止了, 如果被终止之后再被调用, 要抛出异常. 
例子: `InputStream`, `OutputStream`和`java.sql.Connection`上的`close()`方法; `java.util.Timer`的`cancel()`方法.
`Image.flush()`会释放实例相关资源, 但该实例仍处于可用的状态, 如果有必要会重新分配资源. 

显式的终止方法通常与`try-with-resources`块结合使用, 以确保及时终止.

终结方法的好处, 它有两种合法用途:

- 当显式终止方法被忘记调用时, 终结方法可以充当安全网(safety net). **但是如果终结方法发现资源还未被终止, 应该记录日志警告, 这表示客户端代码中的bug.**
- 对象的本地对等体(native peer), 垃圾回收器不会知道它, 当它的Java对等体被回收的时候, 它不会被回收. 
  如果本地对等体拥有必须被及时终止的资源, 那么该类就应该有一个显式的终止方法, 如前面的`close()`; 如果本地对等体并不拥有关键资源, 终结方法是执行这项任务最合适的工具. 


## 第9条 优先使用`try-with-resources`而不是`try-finally`

曾经, `try-finally`是确保资源被关闭的最好方式, 即便是有Exception或者return也不怕.
但是要关闭多个资源, 嵌套使用的时候看起来很丑.

并且如果try和finally块中都有异常抛出, 通常第二个会掩盖了第一个.

所有的这些问题都被Java 7新添加的`try-with-resources`语句解决了. 
要使用的话, 资源类必须实现`AutoCloseable`接口.

当多个异常抛出的时候, 后续异常会被suppressed, 可以通过`getSuppressed()`方法获取(Java 7).
`try-with-resources`也可以加`catch`语句.

总之, 推荐使用`try-with-resources` -> 代码更短, 更简洁, `close()`被隐式调用, 异常信息更有意义.

# Chapter 3 对于所有对象都通用的方法

本章讲何时以及如何覆盖`Object`的非final的方法. 
`Comparable.compareTo`方法具有类似特征, 所以也放在本章讨论. 

## 第10条 覆盖`equals`时请遵守通用约定

如果不覆盖`equals`方法, 类的每个实例都只与它自身相等. 
如果满足以下任何一个条件, 就不需要覆盖`equals`方法: 

- 类的每个实例本质上都是唯一的. (代表活动实体的类如Thread.)
- 不关心类是否提供了逻辑相等的测试功能.
- 超类已经覆盖了`equals`, 从超类继承过来的行为对于子类也是合适的. (Set, List, Map).
- 类是私有或者包可见的, 可以确定它的`equals`方法永远不会被调用. 这种情况下, 可以覆盖`equals`方法, 抛出AssertionError. 以防意外调用.


什么时候应该覆盖`equals()`方法呢?
如果类具有逻辑相等的概念, 通常属于值类(value class)的情形. 
例外: 实例受控的值类: 枚举, 一个值对应一个实例, 所以不需要覆盖`equals`.

覆盖`equals`方法的时候, 必须要遵守通用约定:

* 自反性(reflexive): 对象必须等于其自身.
* 对称性(symmetric): 任何两个对象关于它们是否相等的结果保持一致.
* 传递性(transitive): 如果一个对象等于第二个对象, 第二个对象等于第三个对象, 则第一个对象一定等于第三个对象.
* 一致性(consistent): 如果两个对象相等, 它们就必须始终保持相等, 除非它们被修改了.
* 非空性(non-nullity): 所有的对象都必须不等于null.

实现高质量`equals`方法的诀窍:

* 使用`==`操作符检查参数是否为这个对象的引用, 如果是, 则返回true.
* 使用`instanceof`操作符检查参数是否为正确的类型, 如果不是, 则返回false.
* 把参数转换成正确的类型.
* 对于该类中的每个关键域, 检查参数中的域是否与该对象中对应的域相匹配.
* 当你编写完成了`equals`方法之后, 应该问自己三个问题: 它是否是对称的, 传递的, 一致的? (其他两个特性通常会自动满足.)

注意覆写方法加上`@Override`, `equals`方法的参数类型是`Object`, 不要弄错.

## 第11条 覆盖`equals`时总要覆盖`hashCode`

在每个覆盖了`equals`方法的类中, 也必须覆盖`hashCode`方法. 
如果不这样做的话, 就会违反`Object.hashCode`的通用约定, 从而导致该类无法结合所有基于散列的集合一起正常运作, 这样的集合包括`HashMap`, `HashSet`和`Hashtable`.

通用约定:

* 程序执行期间, 只要对象的`equals`方法的比较操作所用到的信息没有被修改, 那么多次调用`hashCode`方法都必须始终如一地返回同一个整数. 
  (在应用程序多次执行的过程中, 每次执行所返回的整数可以不一致.)
* 如果两个对象根据`equals`比较相等, 那么`hashCode`结果应该相同.
* 如果两个对象根据`equals`比较不相等, 则`hashCode`**不一定**要产生不同的整数结果. 
  (但是不相等的对象产生不同的hashCode有可能提高散列表的性能. 一个好的散列函数通常倾向于为不相等的对象产生不相等的散列码.)


Hashcode的计算:

* 初始值result = 17 (非零常数值, 这样散列值为0的域就会影响到结果).
* 对于对象中`equals`涉及的每个域, 计算出散列值c.
* `result = 31 * result + c`. (乘法使得散列值依赖于域的顺序, 31奇素数, 可以用移位和减法来代替乘法.)

可以把冗余域排除在外, 即一个域的值可以根据其他域的值计算出来.
如果一个类是不可变的, 并且计算hashCode的开销也比较大, 就应该考虑把hashCode缓存在对象内部.


## 第12条 始终要覆盖`toString`

`Object`类的`toString`实现: `类名@散列码的无符号十六进制表示法`.

当对象被传递给`println`, `printf`, 字符串联操作符(+)以及`assert`或者被调试器打印出来时, `toString`方法会被自动调用.

提供好的`toString`方法可以使类使用起来更加舒适, 更利于调试.
实践上, `toString`方法应该返回对象中所有感兴趣的信息.

在实现`toString`的时候, 必须要做出一个很重要的决定: 是否在文档中指定返回值的格式. 

* 好处: 标准, 明确, 适合人阅读, 容易在对象和它的字符串表示法之间来回转换. 
* 不足: 一旦指定, 就必须坚持这种格式, 如果要改变就会破坏原来的代码和数据.
  无论是否指定了格式, 都应该在文档中说明意图.

无论是否指定格式, 都应该为`toString`返回值中包含的所有信息, 提供一种访问途径. 
如果不这么做, 如果想获取某个信息, 就得解析字符串, 降低性能, 解析过程也易出错, 会导致系统不稳定, 如果格式发生变化, 还会导致系统崩溃.


## 第13条 谨慎地覆盖`clone`

`Cloneable`接口没有包含任何方法. 
它决定了`Object`中受保护的`clone`方法实现的行为: 
如果一个类实现了`Cloneable`, `Object`的`clone`方法返回该对象的逐域拷贝, 否则就会抛出`CloneNotSupportedException`. (接口的一种极端非典型的用法.)

来自Object规范中的`clone`方法的通用约定:
创建和返回对象的一个拷贝. 这个拷贝的精确含义取决于该对象的类. 
通常要求:

* `x.clone() != x`
* `x.clone().getClass() == x.getClass()`
* `x.clone().equals(x)`
  通常要求这三个表达式都为true, 但不是绝对.

如果你覆盖了非final类中的`clone`方法, 则应该首先调用`super.clone`得到对象.

对于实现了`Cloneable`的类, 我们总是期望它也提供一个功能适当的公有的`clone`方法, 通常, 需要该类的所有超类都提供了行为良好的`clone`方法.

`clone`方法的返回值应该是当前类(而不是Object). **协变返回类型(covariant return type)**: 覆盖方法的返回类型可以是被覆盖方法的返回类型的子类.

immutable的类不应该提供`clone`方法.

引用对象的clone: `clone`方法不应该伤害到原始对象, 所以对引用对象应该递归地调用`clone`.

`Cloneable`和一般的指向mutable对象的final域使用不兼容(除非这些域可以在对象和它的克隆之间安全共享).
所以为了让一个类可克隆, 有时候需要移除一些域之前的final修饰符.

散列表数据的深度拷贝. (三种方法: 递归, 迭代, 后续put赋值.)

`clone`方法中不能调用非final非private的方法, 子类可能会覆盖, 修改行为.

`Object`的`clone`方法声明了throw `CloneNotSupportedException`, 覆写以后就不用声明了.
如果一个类只是为了继承而设计的, 那么它不应该实现`Cloneable`. 要么学`Object`类, 让子类自由决定是否实现; 要么实现一个抛出异常的`clone`方法, 阻止子类实现. 

另一个实现对象拷贝的方法(更好的方法)是提供一个拷贝构造器或者拷贝工厂.


## 第14条 考虑实现`Comparable`接口

`compareTo`方法是`Comparable`接口中唯一的方法, 允许进行等同性和顺序比较: 
将对象与指定的对象进行比较, 当该对象小于, 等于或大于指定对象的时候, 分别返回一个负整数, 零或正整数.

由`compareTo`施加的等同性测试, 也一定遵守相同于`equals`约定所施加的限制条件: 自反性, 对称性和传递性.

强烈建议`(x.compareTo(y) == 0) == (x.equals(y))`.

比较对象引用域可以是通过递归地调用`compareTo`方法来实现. 如果一个域并没有实现`Comparable`接口, 或者你需要一个非标准的排序关系, 可以使用一个显式的`Comparator`来代替.

本书之前的版本是这样建议的:

```
比较整数型基本类型的域, 可以用关系操作符`<`和`>`. 
浮点域用`Double.compare`或`Float.compare`. (浮点值没有遵守compareTo的通用约定.)
```

从Java 7开始, 所有的基本类型的装箱类型都提供了静态的`compare`方法, 所以不再建议使用`<`和`>`.

如果一个类有多个关键域, 必须从最关键的域开始, 逐步进行到所有的重要域, 如果某个关键域产生了非零的结果, 则整个比较结束, 并返回该结果, 否则则进一步比较下一个域.

Java 8提供了一些comparator构造的方法, 比如`comparingInt`, `thenComparingInt`, `comparing`等, 可以链式组合使用.

由于`compareTo`方法并没有指定返回值的大小, 而只是指定了符号, 所以可以利用这一点进行简化. 

反例: 不要用两个数相减的方法: 注意可能会溢出导致错误, 并且这样做并没有明显的性能改善. 
-> 推荐用静态的`Integer.compare`方法或者`comparingInt`来构造Comparator.

# Chapter 4 类和接口

## 第15条 使类和成员的可访问性最小化

尽可能地使每个类或者成员不被外界访问.

对于顶层(非嵌套)的类和接口, 只有两种可能的访问级别: 包级私有(package private)和公有(public).
如果一个包级私有的顶层类(或接口)只是在某一个类的内部被用到, 就应该考虑使它成为那个类的私有嵌套类.

对于成员(域, 方法, 嵌套类和嵌套接口), 有四种可能的访问级别(可访问性递增):

* 私有的(private).
* 包级私有的(package-private): 缺省(default)访问级别, 声明该成员的包内部的任何类都可以访问这个成员.
* 受保护的(protected): 声明该成员的类的子类和包内部的任何类可以访问这个成员.
* 公有的(public).

如果覆盖了超类中的一个方法, 子类中的访问级别就不允许低于超类中的访问级别. 这样可以确保任何使用超类的地方都可以使用子类的实例.

实例域很少是公有的.
包含公有可变域的类并不是线程安全的.

同样的建议也适用于静态域. 只有一种例外: **公有静态final域**来暴露常量(名称由大写字母单词下划线隔开). 这些域要么包含基本类型的值, 要么包含指向不可变对象的引用.

长度非零的数组总是可变的, 所以, 类具有**公有的静态final**数组域, 或者返回这种域的访问方法, 这几乎总是错误的. 
-> 改进: 让数组域private, 暴露一个immutable的list或者提供一个返回数组copy的公有方法.

Java 9的module在package之上的组. module可以在`module-info.java`中声明要`export`那些packages, 没有exported的packages在module外不可访问.


## 第16条 在公有类中使用访问方法而非公有域

一些退化类(degenerate classes)只是用来集中实例域, 因为其中的字段都是可直接访问的, 所以这些类没有提供封装的好处. 

对公有类, 应该用包含私有域和公有访问方法(getter)的类来代替, 对可变的类, 加上公有设值方法(setter).
-> 保留了改变内部表现的灵活性.

如果类是包级私有的, 或者是私有的嵌套类, 直接暴露它的数据域并没有本质的错误.

Java平台中有几个类违反了"公有类不应该直接暴露数据域"的告诫. 如`java.awt`包中的`Point`和`Dimension`.

让公有类直接暴露域虽然从来都不是种好办法, 但是如果域是不可变的, 这种做法的危害就比较小一些(但是仍然questionable).


## 第17条 使可变性最小化

不可变类: 其实例不能被修改的类. 每个实例中包含的所有信息都必须在创建该实例的时候就提供, 并在对象的整个生命周期内固定不变.

为了使类成为不可变, 要遵循下面五条规则:

* 不要提供任何会修改对象状态的方法.
* 保证类不会被扩展. (一般做法: 声明为final.)
* 使所有的域都是final的.
* 使所有的域都成为私有的.
* 确保对于任何可变组件的互斥访问.

不可变对象本质上是线程安全的, 它们不要求同步.
不可变对象可以被自由地共享.
不可变对象永远也不需要保护性拷贝.

不可变类唯一真正的缺点是, 对于每个不同的值都需要一个单独的对象. (特定情况下的性能问题.)

可以为类提供公有的可变配套类. Java类库中的`String`的可变配套类是`StringBuilder`和`StringBuffer`.

为了让类不能被继承, 除了使类成为final的外, 还有一种方法: 让类的所有构造器都变成私有的或者包级私有的, 并添加公有的静态工厂.
优点: 提供了缓存能力, 可以提供多个不同名字的静态方法, 使相同参数类型可以构造出不同的对象(用构造器就不行). 

**尽量缩小可变性**:

* 除非有很好的理由要让类成为可变的, 否则就应该是不可变的.
* 如果类不能被做成是不可变的, 仍然应该尽可能地限制它的可变性. (降低状态数, 尽量让域为`private final`的.)
* 构造器应该创建完全初始化的对象, 并建立起所有的约束关系. 不要在构造器或者静态工厂之外再提供公有的初始化方法, 也不应该提供重新初始化方法.


## 第18条 组合优先于继承

这里说的继承是类的继承, 不是接口的实现.

**继承打破了封装性.**
超类的实现有可能会随着发行版本的不同而有所变化, 如果真的发生了变化, 子类有可能会遭到破坏. 
因此, 子类必须要跟着其超类的更新而演变, 除非超类是专门为了扩展而设计的, 并且有很好的文档说明.
例子: 覆写了`HashSet`中的`add`和`addAll`方法, 但其实后者调用了前者.

组合(composition): 在新的类中增加一个私有域, 它引用现有类的一个实例.

新类中的方法可以转发被包含的现有实例中的方法. 这样得到的类将会非常稳固, 它不依赖于现有类的实现细节.

因为每一个新类的实例都把另一个现有类的实例包装起来了, 所以新的类被称为**包装类(wrapper class)**, 这也正是**Decorator模式**.

只有当子类真正是超类的子类型时, 才适合用继承. 即对于两个类, 只有当两者之间确实存在"is-a"关系的时候, 才应该继承.

Java类库中也有明显违反这条原则的地方, 比如`Stack`并不是`Vector`, `Properties`不是`Hashtable`, 它们却错误地使用了继承.

在决定使用继承而不是复合之前, 还应该问自己最后一组问题: 对于你正在试图扩展的类, 它的API中有没有缺陷呢? 继承机制会把超类API中的缺陷传播到子类中, 而复合则允许设计新的API来隐藏这些缺陷.


## 第19条 要么为继承而设计, 并提供文档说明, 要么就禁止继承

### 对于专门为了继承而设计的类, 需要具有良好的文档.

该类的文档必须精确地描述覆盖每个方法所带来的影响. 换句话说, **该类必须有文档说明它可覆盖的方法的自用性.** 
更一般地, 类必须在文档中说明, 在哪些情况下它会调用可覆盖的方法. 

如果方法调用到了可覆盖的方法, 在它的文档注释末尾应该包含关于这些调用的描述信息: Implementation Requirements, `@implSpec`(Added in Java 8).
这段描述信息要以这样的句子开头: "This implementation...".

关于程序文档有句格言; **好的API文档应该描述一个给定的方法做了什么工作, 而不是描述它是如何做到的.** 上面这种做法违背了这句格言, 这是继承破坏了封装性所带来的不幸后果.

### 类必须通过某种形式提供适当的钩子(hook).

类必须通过某种形式提供适当的钩子(hook), 以便能够进入到它的内部工作流程中, 这种形式可以是精心选择的protected方法, 也可以是protected的域, 后者比较少见.

例子: `java.util.AbstractList`中的`removeRange`方法. 使子类更易提供针对子列表的快速`clear`方法.

### 对于为了继承而设计的类, 唯一的测试方法就是编写子类.

在为了继承而设计的类有可能被广泛使用时, 必须要意识到, 对于文档中所说明的自用模式, 以及对于其受保护方法和域中所隐含的实现策略, 实际上已经做出了永久的承诺. 
因此必须在发布之前先编写子类对类进行测试.

### 为了允许继承, 类还必须遵守其他一些约束.

* 构造器决不能调用可被覆盖的方法. 无论是直接调用还是间接调用. 
  (因为超类的构造器在子类的构造器之前运行, 如果子类中覆盖版本的方法依赖于子类构造器所执行的任何初始化工作, 该方法将不会如预期般地执行.)
* 在为了继承而设计类的时候, `Cloneable`和`Serializable`接口出现了特殊的困难. 
  无论是`clone`还是`readObject`, 都和构造器类似, 都不可以调用可覆盖的方法, 不管是以直接还是间接的方式. 
  如果该类有`readResolve`或`writeReplace`方法, 就必须使它们成为受保护的方法.

### 对于那些并非为了安全地进行子类化而设计和编写文档的类, 要禁止子类化.

* 把类声明为final.
* 把所有的构造器都变成私有的, 或者包级私有的, 并增加一些公有的静态工厂来替代构造器.


## 第20条 接口优于抽象类

抽象类和接口的区别:

* 抽象类允许包含某些方法的实现, 接口则不允许. (从Java 8开始接口也可以包含默认方法了.)
* 抽象类需要继承(Java只允许单继承), 但是可以实现多个接口.

使用接口的好处:

* 现有的类可以很容易被更新, 以实现新的接口.
* 接口是定义混合类型(mixin)的理想选择.
* 接口允许我们构造非层次结构的类型框架.
* 接口可以更安全有力地增强功能. -> 组合优于继承.

通过对你导出的每个重要接口都提供一个抽象的骨架实现(skeletal implementation)类, 把接口和抽象类的优点结合起来.
按照惯例, 骨架实现被称为`AbstractInterface`, 这里`Interface`是指所实现的接口的名字. 
比如`AbstractCollection`, `AbstractSet`, `AbstractList`和`AbstractMap`.

骨架实现的美妙之处在于, 它们为抽象提供了实现上的帮助, 但又不强加抽象类被用作类型定义时所特有的严格限制. 
对于接口的大多数实现来讲, 扩展骨架实现类是个很显然的选择, 但并不是必需的. 如果类无法扩展骨架实现类, 这个类始终可以手工实现这个接口. 

此外, 骨架实现类仍然能够有助于接口的实现. 
实现了这个接口的类可以把对于接口方法的调用, 转发到一个内部私有类的实例上, 这个内部私有类扩展了骨架实现类. 这种方法被称作**模拟多重继承(simulated multiple inheritance)**.

编写骨架实现类, 必须认真研究接口, 并确定哪些方法是最为基本的, 其他的方法则可以根据它们来实现. 
这些基本方法将成为骨架实现类中的抽象方法, 然后, 必须为接口中的其他方法提供具体的实现.

骨架类的简单实现: 最简单的可能的有效实现, 不是抽象的. 你可以原封不动地使用, 也可以将它子类化.

使用抽象类有一个优势: 抽象类的演变比接口的演变要容易得多. 
如果在后续的发行版本中, 你希望在抽象类中增加新的具体方法, 始终可以增加, 它包含合理的默认实现. 然后, 该抽象类的所有实现都将提供这个新的方法.

接口一旦被公开发行, 并且已被广泛实现, 再想改变这个接口几乎是不可能的.

## 第21条 为了后代设计接口

从Java 8开始, 可以给接口加上方法, 而不破坏现有的实现. (有风险).

声明包含默认实现的默认方法, 可以让之前实现这个接口的子类用这个默认实现.

Java 8开始, 有很多默认方法被加在了collection中, 主要是为了lambda. 
Java库的默认方法是高质量, 通用的实现, 大多数情况都能工作得很好.
但并不是永远都能写一个在任何情形下都适用的默认方法实现. -> 比如`removeIf`, Apache的`SynchronizedCollection`实现允许一个外部对象作为锁, 
如果使用`removeIf`可能会抛出异常或者其他未知行为.

在有默认方法出现的时候, 接口之前存在的实现可能可以通过编译, 但是可能在运行时失败.


## 第22条 接口只用于定义类型

常量接口(constant interface): 没有包含任何方法, 只包含静态的final域, 每个域都导出一个常量. 使用这些常量的类实现这个接口, 以避免用类名来修饰常量名.

### 常量接口模式是对接口的不良使用: 

* 暴露了实现细节到该类的导出API中; 
* 实现常量接口对于类的用户来说没有价值; 
* 如果以后的发行版本中不需要其中的常量了, 依然必须实现这个接口; 
* 所有子类的命名空间也会被接口中的常量污染.

```java
// Constant interface antipattern - do not use!
public interface PhysicalConstants {
    // Avogadro's number (1/mol)
    static final double AVOGADROS_NUMBER   = 6.022_140_857e23;

    // Boltzmann constant (J/K)
    static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;

    // Mass of the electron (kg)
    static final double ELECTRON_MASS      = 9.109_383_56e-31;
}

```

如果要导出常量, 可以有几种合理的选择方案:

* 如果这些常量与某个现有的类或者接口紧密相关, 就应该把这些常量添加到这个类或者接口中.
* 如果这些常量最好被看作枚举类型的成员, 就应该用枚举类型来导出这些常量.
* 使用不可实例化的工具类来导出这些常量.

```java
// Constant utility class (Page 93)
public class PhysicalConstants {
  private PhysicalConstants() { }  // Prevents instantiation

  // Avogadro's number (1/mol)
  public static final double AVOGADROS_NUMBER = 6.022_140_857e23;

  // Boltzmann constant (J/K)
  public static final double BOLTZMANN_CONST  = 1.380_648_52e-23;

  // Mass of the electron (kg)
  public static final double ELECTRON_MASS    = 9.109_383_56e-31;
}
```

总结: 接口应该只被用来定义类型, 它们不应该被用来导出常量.

## 第23条 类层次优于标签类

有时候, 可能会遇到带有两种甚至更多种风格的实例的类, 并包含表示实例风格的标签域. 例子: Figure类内含Shape枚举, 包含圆形和方形.

```java
// Class hierarchy replacement for a tagged class  (Page 110-11)
abstract class Figure {
    abstract double area();
}


class Circle extends Figure {
    final double radius;

    Circle(double radius) { this.radius = radius; }

    @Override double area() { return Math.PI * (radius * radius); }
}
// Class hierarchy replacement for a tagged class  (Page 110-11)
class Rectangle extends Figure {
    final double length;
    final double width;

    Rectangle(double length, double width) {
        this.length = length;
        this.width  = width;
    }
    @Override double area() { return length * width; }
}
// Class hierarchy replacement for a tagged class  (Page 110-11)
class Square extends Rectangle {
    Square(double side) {
        super(side, side);
    }
}


// Tagged class - vastly inferior to a class hierarchy! (Page 93)
class Figure {
    enum Shape { RECTANGLE, CIRCLE };

    // Tag field - the shape of this figure
    final Shape shape;

    // These fields are used only if shape is RECTANGLE
    double length;
    double width;

    // This field is used only if shape is CIRCLE
    double radius;

    // Constructor for circle
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }

    // Constructor for rectangle
    Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }

    double area() {
        switch(shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}

```

这种标签类有着许多缺点: 

* 它们中充斥着样板代码, 包括枚举声明, 标签域以及条件语句, 可读性不好.
* 内存占用也增加了, 因为实例承担着属于其他风格的不相关的域. 
* 域不能是final的, 构造器必须不借助编译器来设置标签域.
* 添加风格必须要修改源文件. 
* 不容易扩展, 添加风格必须记得给每个条件语句都添加条件. 
* 实例的数据类型没有提供任何关于其风格的线索.

**标签类过于冗长, 容易出错, 效率低下.**

用子类型修正:

* 定义抽象基类, 方法行为若依赖于标签值, 则定义为抽象方法. 方法行为若不依赖于标签值, 就把方法放在抽象类中.
* 所有方法都用到的数据域放在抽象类中, 特定于某个类型的数据域放在对应的子类中.

这个类层次纠正了前面所提到的标签类的所有缺点.

## 第24条 优先考虑静态成员类

嵌套类(nested class)是指被定义在另一个类的内部的类. 嵌套类存在的目的应该只是为它的外围类提供服务.

嵌套类有四种:

* 静态成员类(static member class).
* 非静态成员类(nonstatic member class).
* 匿名类(anonymous class).
* 局部类(local class).

除了第一种之外, 其他三种都被称为内部类(inner class).

### 静态成员类

外围类的一个静态成员, 与其他的静态成员遵守同样的可访问性规则.

如果声明成员类不要求访问外围实例, 就要始终把static修饰符放在它的声明中, 使它成为静态成员类.

常见用法: 作为公有的辅助类, 仅当与它的外部类一起使用时才有意义.

私有静态成员类的一种常见用法是用来代表外围类所代表的对象的组件.
例如: Map中的Entry.


### 非静态成员类

非静态成员类的每个实例都隐含着与外围类的一个实例相关联. 保存这份引用消耗时间和空间, 并且会导致外围实例在符合垃圾回收时却仍然得以保留.

如果嵌套类的实例可以在它外围类的实例之外独立存在, 这个嵌套类就必须是静态成员类; 在没有外围实例的情况下, 要想创建非静态成员类的实例是不可能的.

创建: 在外围类的某个实例方法的内部调用非静态成员类的构造器; 使用表达式`enclosingInstance.new MemberClass(args)`来手工创建(很少使用).

常见用法: 定义Adapter, 它允许外部类的实例被看作是另一个不相关的类的实例.
例如: Map的集合视图, Set和List的迭代器.

### 匿名类

匿名类没有名字, 它不是外围类的一个成员, 它是在使用的时候同时被声明和实例化. 可以出现在代码中任何允许存在表达式的地方.

当且仅当匿名类出现在非静态环境中时, 它才有外围实例. 但是即使它们出现在静态的环境中, 也不可能拥有任何静态成员.

常见用法: 

* 创建函数对象. 如匿名的Comparator实例.
* 创建过程对象. 如Runnable, Thread或者TimerTask实例.
* 在静态工厂方法的内部.

### 局部类

局部类不常用. 在任何可以声明局部变量的地方, 都可以声明局部类, 并且局部类也遵守同样的作用域规则.

局部类有名字, 可以被重复地使用. 只有当局部类在非静态环境中定义的时候, 才有外围实例. 它们也不能包含静态成员. 

与匿名类一样, 它们必须非常简短, 以保证可读性.

总结: 

* 如果一个嵌套类需要在单个方法之外仍然是可见的, 或者它太长了, 不适合于放在方法内部, 就应该使用成员类. 
* 如果成员类的每个实例都需要一个指向其外围实例的引用, 就要把成员类做成非静态的; 否则, 就做成静态的.
* 假设这个嵌套类属于一个方法的内部, 如果你只需要在一个地方创建实例, 并且已经有了一个预置的类型可以说明这个类的特征, 就要把它做成匿名类; 否则, 就做成局部类.

## 第25条 限制源文件为单个顶级类

虽然Java编译器允许你在一个文件中定义多个顶级类, 但是这样做没什么好处, 并且有风险.

风险缘由: 在一个源文件中定义多个顶级类, 将有机会通过多个源文件为一个类提供多个定义, 最终使用哪个定义和源文件被交给编译器的顺序有关.

永远不要把多个顶级类或接口放在同一个源文件中. 这样可以保证程序的运行结果和编译顺序无关.

# Chapter 5 泛型

## 第26条 不要使用原生态类型

类或接口的声明中如果有类型参数, 就是泛型类或泛型接口, 统称泛型.
比如List<E>接口.

每个泛型都定义一个原生态类型(raw type), 即不带任何实际类型参数的泛型名称. 
例如, 与List<E>相对应的原生态类型是List. 与Java平台没有泛型之前的接口类型List完全一样.

如果使用原生态类型, 就失掉了泛型在安全性和表达性方面的所有优势. 它的存在只是为了兼容泛型出现之前的旧版本的代码.

注意: 使用`List<Object>`仍然是可以的.
区别就是raw type逃避了泛型检查, 而`List<Object>`则明确地告诉编译器, 它能够有任意类型的对象. 

一个`List<String>`可以传给类型为`List`的参数, 但不能传给`List<Object>`.

```java
// Fails at runtime - unsafeAdd method uses a raw type (List)!  (Page 119)
public class Raw {
    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        unsafeAdd(strings, Integer.valueOf(42));
        String s = strings.get(0); // Has compiler-generated cast
    }

    private static void unsafeAdd(List list, Object o) {
        list.add(o);
    }
}
//报错
//Exception in thread "main" java.lang.ClassCastException: java.base/java.lang.Integer //cannot be cast to java.base/java.lang.String
//	at effectivejava.chapter5.item26.Raw.main(Raw.java:9)

```

如果要使用泛型, 但不确定或者不关心实际的类型参数, 可以使用一个问号(无限制的通配符类型)代替. 比如`Set<?>`

但是使用了这个通配符的缺点就是, 你无法将任何元素(除了null)插入到`Collection<?>`中, 而且根本无法猜测你会得到哪种类型的对象. 
要是无法接受这些限制, 可以使用泛型方法(见30条)或者有限制的通配符类型(见31条).

不要在新代码中使用原生态类型, 有两个小小的例外:

* 在类文字(class literal)中必须使用原生态类型. 比如List.class.
* 使用`instanceOf`的时候: 比如`o instanceOf Set`.


## 第27条 消除非受检警告

用泛型编程时, 有可能会收到很多编译器警告, 要尽可能地消除每一个非受检警告.

有一些根据提示即可消除, 另一些比较难消除.

如果无法消除警告, 但可以证明引起警告的代码是类型安全的, 可以用
`@SuppressWarnings("unchecked")`注解来禁止这条警告. 并加上注释解释为什么是安全的.

如果无法保证安全, 编译时禁止了警告, 运行时还是会抛出`ClassCastException`.

如果明知道安全却不做处理, 没有加Suppress注解, 那么当新出现一条可能有问题的警告时, 新的警告会淹没在所有的错误警告中.

`SuppressWarnings`可以用在任何粒度的级别中. 应该尽量在小范围内使用. 所以实践中常常会额外声明一个局部变量来加上这个注解, 以缩小注解范围.

## 第28条 列表(lists)优先于数组(arrays)

数组与泛型相比, 有两个重要的不同点:
首先:

* 数组是协变的(covariant). -> `Sub[]`是`Super[]`的子类型.
* 泛型是不可变的(invariant). -> `List<Type1>`和`List<Type2>`没有子类型关系.

所以有些类型错误的问题用数组可能要在运行时才能发现, 而用列表在编译时就发现了.

第二大区别:

*    数组是具体化的(reified), 在运行时才知道并检查元素类型约束.
* 泛型是通过擦除(erasure)来实现的. 在编译时强化类型信息, 并在运行时丢弃(或擦除)类型信息. 擦除就是使泛型可以与没有使用泛型的代码随意进行互用.

基于上述这些根本的区别, 因此数组和泛型不能很好地混合使用.

当你得到泛型数组创建错误时, 最好的解决办法通常是优先使用集合类型`List<E>`, 而不是数组类型`E[]`, 这样可能会损失一些简洁性, 但是换回的却是更高的类型安全性和互用性.

```java
// List-based Chooser - typesafe (Page 111)
public class Chooser<T> {
    private final List<T> choiceList;

    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    }

    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }

    public static void main(String[] args) {
        List<Integer> intList = List.of(1, 2, 3, 4, 5, 6);

        Chooser<Integer> chooser = new Chooser<>(intList);

        for (int i = 0; i < 10; i++) {
            Number choice = chooser.choose();
            System.out.println(choice);
        }
    }
}
```


## 第29条 优先考虑泛型

举了一个堆栈实现的例子, 开始是用Object类型.

将这个类泛型化:

* 给它的声明加类型参数.
* 用类型参数替换所有的Object类型.
* 解决不能创建泛型数组的问题: 1.创建Object的数组并强转为`E[]`; 2.将声明`E[]`改为`Object[]`, 在pop单个元素的时候强转为E. 因为这两种情况下都可以保证安全性, 所以在最小的范围加上`SuppressWarnings`.

改造后不需要客户端强转.

有一些泛型限制了可允许的类型参数值. 比如`<E extends Delayed>`要求实际的类型参数E必须是`Delayed`的一个子类型. 
此时E被称作有限制的类型参数(bounded type parameter). 

注意: 每个类型都是它自身的子类型.

```java
public class Stack<E> {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    // Appropriate suppression of unchecked warning
    public E pop() {
        if (size == 0)
            throw new EmptyStackException();

        // push requires elements to be of type E, so cast is correct
        @SuppressWarnings("unchecked") E result =
                (E) elements[--size];

        elements[size] = null; // Eliminate obsolete reference
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

    // Little program to exercise our generic Stack
    public static void main(String[] args) {
        Stack<String> stack = new Stack<>();
        for (String arg : args)
            stack.push(arg);
        while (!stack.isEmpty())
            System.out.println(stack.pop().toUpperCase());
    }
}
```

```java
// Generic stack using E[] (Pages 130-3)
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    // The elements array will contain only E instances from push(E).
    // This is sufficient to ensure type safety, but the runtime
    // type of the array won't be E[]; it will always be Object[]!
    @SuppressWarnings("unchecked")
    public Stack() {
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        E result = elements[--size];
        elements[size] = null; // Eliminate obsolete reference
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

    // Little program to exercise our generic Stack
    public static void main(String[] args) {
        Stack<String> stack = new Stack<>();
        for (String arg : args)
            stack.push(arg);
        while (!stack.isEmpty())
            System.out.println(stack.pop().toUpperCase());
    }
}
```

```java 
public class EmptyStackException extends RuntimeException {
}
```

## 第30条 优先考虑泛型方法

就如类可以从泛型中受益一般, 方法也一样.

静态工具方法尤其适合于泛型化.

声明类型参数的参数列表位于方法修饰符和返回值类型之间.

泛型方法的一个显著特性是, 无需明确指定类型参数的值, 不像调用泛型构造器的时候是必须指定的. 
编译器通过检查方法参数的类型来计算类型参数的值, 这个过程叫做类型推导(type inference).

利用这个特点, 可以利用静态工厂方法来简化泛型构造器的调用.

总而言之, 泛型方法优先于需要客户端来强转参数和返回值的方法.

```java
// Generic union method and program to exercise it  (Pages 135-6)
public class Union {

    // Generic method
    public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> result = new HashSet<>(s1);
        result.addAll(s2);
        return result;
    }

    // Simple program to exercise generic method
    public static void main(String[] args) {
        Set<String> guys = Set.of("Tom", "Dick", "Harry");
        Set<String> stooges = Set.of("Larry", "Moe", "Curly");
        Set<String> aflCio = union(guys, stooges);
        System.out.println(aflCio);
    }
}
```

```java
// Generic singleton factory pattern (Page 136-7)
public class GenericSingletonFactory {
    // Generic singleton factory pattern
    private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

    @SuppressWarnings("unchecked")
    public static <T> UnaryOperator<T> identityFunction() {
        return (UnaryOperator<T>) IDENTITY_FN;
    }

    // Sample program to exercise generic singleton
    public static void main(String[] args) {
        String[] strings = { "jute", "hemp", "nylon" };
        UnaryOperator<String> sameString = identityFunction();
        for (String s : strings)
            System.out.println(sameString.apply(s));

        Number[] numbers = { 1, 2.0, 3L };
        UnaryOperator<Number> sameNumber = identityFunction();
        for (Number n : numbers)
            System.out.println(sameNumber.apply(n));
    }
}
```

```java
// Using a recursive type bound to express mutual comparability (Pages 137-8)
public class RecursiveTypeBound {
    // Returns max value in a collection - uses recursive type bound
    public static <E extends Comparable<E>> E max(Collection<E> c) {
        if (c.isEmpty())
            throw new IllegalArgumentException("Empty collection");

        E result = null;
        for (E e : c)
            if (result == null || e.compareTo(result) > 0)
                result = Objects.requireNonNull(e);

        return result;
    }

    public static void main(String[] args) {
        List<String> argList = Arrays.asList(args);
        System.out.println(max(argList));
    }
}
```

## 第31条 利用有限制通配符来提升API的灵活性

参数化类型是不可变的(invariant). 对于两个不同的类型Type1和Type2而言, `List<Type1>`和`List<Type2>`没有继承关系.
比如`List<String>`不是`List<Object>`的子类型.

但是有时候可能需要更灵活的应用场景, Java提供了有限制的通配符类型(bounded wildcard type).

举例: 堆栈实现中的两个方法:

```
public void pushAll(Iterable<? extends E> src)

public void popAll(Collection<? super E> dst)
```

为了获得最大限度的灵活性, 要在表示生产者或者消费者的输入参数上使用通配符类型.

在我们的Stack示例中，pushAll的src参数差生E示例供Stack使用，因此src相应的类型应为Interable<? extends E>;popAll的dst参数通过Stack消费E示例，因此dst相应的类型为Collection<? super E>

助记符:
**PECS**表示`producer-extends, consumer-super`.

注意不要把bounded wildcard types作为返回值.

**所有的comparable和comparator都是消费者.**

```
// Two possible declarations for the swap method
public static <E> void swap(List<E> list, int i, int j); // unbounded type parameter
public static void swap(List<?> list, int i, int j); // unbounded wildcard

```

哪种更好呢? 对于API来说, 第二种更好 -> 让API更简单, 灵活. 
如果一个参数类型在方法声明中只出现一次, 就用一个wildcard来替代它.
swapHelper -> 把复杂的泛型内化.

```java
// Generic stack with bulk methods using wildcard types (Pages 139-41)
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    // The elements array will contain only E instances from push(E).
    // This is sufficient to ensure type safety, but the runtime
    // type of the array won't be E[]; it will always be Object[]!
    @SuppressWarnings("unchecked")
    public Stack() {
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        E result = elements[--size];
        elements[size] = null; // Eliminate obsolete reference
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

//    // pushAll staticfactory without wildcard type - deficient!
//    public void pushAll(Iterable<E> src) {
//        for (E e : src)
//            push(e);
//    }

    // Wildcard type for parameter that serves as an E producer
    public void pushAll(Iterable<? extends E> src) {
        for (E e : src)
            push(e);
    }

//    // popAll staticfactory without wildcard type - deficient!
//    public void popAll(Collection<E> dst) {
//        while (!isEmpty())
//            dst.add(pop());
//    }

    // Wildcard type for parameter that serves as an E consumer
    public void popAll(Collection<? super E> dst) {
        while (!isEmpty())
            dst.add(pop());
    }

    // Little program to exercise our generic Stack
    public static void main(String[] args) {
        Stack<Number> numberStack = new Stack<>();
        Iterable<Integer> integers = Arrays.asList(3, 1, 4, 1, 5, 9);
        numberStack.pushAll(integers);

        Collection<Object> objects = new ArrayList<>();
        numberStack.popAll(objects);

        System.out.println(objects);
    }
}
```

```java
// Using a recursive type bound with wildcards (Page 143)
public class RecursiveTypeBound {
    public static <E extends Comparable<? super E>> E max(
        List<? extends E> list) {
        if (list.isEmpty())
            throw new IllegalArgumentException("Empty list");

        E result = null;
        for (E e : list)
            if (result == null || e.compareTo(result) > 0)
                result = e;

        return result;
    }

    public static void main(String[] args) {
        List<String> argList = Arrays.asList(args);
        System.out.println(max(argList));
    }
}
```

```java
// Private helper method for wildcard capture (Page 145)
public class Swap {
    public static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);
    }

    // Private helper method for wildcard capture
    private static <E> void swapHelper(List<E> list, int i, int j) {
        list.set(i, list.set(j, list.get(i)));
    }

    public static void main(String[] args) {
        // Swap the first and last argument and print the resulting list
        List<String> argList = Arrays.asList(args);
        swap(argList, 0, argList.size() - 1);
        System.out.println(argList);
    }
}
```

```java
// Generic union method with wildcard types for enhanced flexibility (Pages 142-3)
public class Union {
    public static <E> Set<E> union(Set<? extends E> s1,
                                   Set<? extends E> s2) {
        Set<E> result = new HashSet<E>(s1);
        result.addAll(s2);
        return result;
    }

    // Simple program to exercise flexible generic staticfactory
    public static void main(String[] args) {
        Set<Integer> integers = new HashSet<>();
        integers.add(1); 
        integers.add(3); 
        integers.add(5); 

        Set<Double> doubles =  new HashSet<>();
        doubles.add(2.0); 
        doubles.add(4.0); 
        doubles.add(6.0); 

        Set<Number> numbers = union(integers, doubles);

//      // Explicit type parameter - required prior to Java 8
//      Set<Number> numbers = Union.<Number>union(integers, doubles);

        System.out.println(numbers);
    }
}
```

## 第32条 谨慎地结合泛型和可变参数

泛型和可变参数都是Java 5的时候添加的, 但是它们却不能很好地一起用.

可变参数的实现实际上是创建了一个数组, 而这个数组实际上又是可见的, 所以当你使用的时候有泛型或参数化类型的可变参数的时候, 会得到令人困惑的编译警告.
这是因为几乎所有的泛型和参数化类型都是non-reifiable的(runtime信息比compile-time少),
所以编译器会在这种声明的时候警告heap pollution: 不能保证类型安全. 

**把一个值保存在泛型的可变参数数列中是不安全的.**

那么为什么声明泛型的数组是非法的, 而这种泛型可变参数声明是合法的呢? 实际上在实践中是有用的, 所以语言设计者保留了它.
Java类库中: `Arrays.asList(T...a)`, `Collections.addAll(Collection<? super T> c, T... elements)`, `EnumSet.of(E first, E... rest)`.
这些类库方法是类型安全的.

在Java 7之前, 对泛型可变参数的警告只能在客户端通过`@SuppressWarnings("unchecked")`来消除, 
Java 7加上了`SafeVarargs`注解, 方法的作者用来承诺安全性.

一个有泛型可变参数的方法, 满足了下面两个条件就是安全的:

* 不存储可变参数数组中的任何东西.
* 不会把这个数组暴露给不受信任的代码.
  如果违反了就应该修复, 然后标记`@SafeVarargs`, 这样方法的使用者就不会因为奇怪的编译警告而迷惑了.

还有一种选择是, 用List参数来代替.

```java
// It is unsafe to store a value in a generic varargs array parameter (Page 146)
public class Dangerous {
    // Mixing generics and varargs can violate type safety!
    static void dangerous(List<String>... stringLists) {
        List<Integer> intList = List.of(42);
        Object[] objects = stringLists;
        objects[0] = intList; // Heap pollution
        String s = stringLists[0].get(0); // ClassCastException
    }

    public static void main(String[] args) {
        dangerous(List.of("There be dragons!"));
    }
}
```

```java
// Safe method with a generic varargs parameter (page 149)
public class FlattenWithVarargs {
    @SafeVarargs
    static <T> List<T> flatten(List<? extends T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<? extends T> list : lists)
            result.addAll(list);
        return result;
    }

    public static void main(String[] args) {
        List<Integer> flatList = flatten(
                List.of(1, 2), List.of(3, 4, 5), List.of(6,7));
        System.out.println(flatList);
    }
}
```

```java
// Subtle heap pollution (Pages 147-8)
public class PickTwo {
    // UNSAFE - Exposes a reference to its generic parameter array!
    static <T> T[] toArray(T... args) {
        return args;
    }

    static <T> T[] pickTwo(T a, T b, T c) {
        switch(ThreadLocalRandom.current().nextInt(3)) {
            case 0: return toArray(a, b);
            case 1: return toArray(a, c);
            case 2: return toArray(b, c);
        }
        throw new AssertionError(); // Can't get here
    }

    public static void main(String[] args) {
        String[] attributes = pickTwo("Good", "Fast", "Cheap");
        System.out.println(Arrays.toString(attributes));
    }
}
```

```java
// Safe version of PickTwo using lists instead of arrays (Page 150)
public class SafePickTwo {
    static <T> List<T> pickTwo(T a, T b, T c) {
        switch(ThreadLocalRandom.current().nextInt(3)) {
            case 0: return List.of(a, b);
            case 1: return List.of(a, c);
            case 2: return List.of(b, c);
        }
        throw new AssertionError();
    }

    public static void main(String[] args) {
        List<String> attributes = pickTwo("Good", "Fast", "Cheap");
        System.out.println(attributes);
    }
}
```

## 第33条 优先考虑类型安全的异构容器

泛型最常用于集合, 限制每个容器只能有固定数目的类型参数.
一般来说, 这种情况正是你想要的, 比如一个Set只有一个类型参数, 表示元素类型; Map有两个类型参数, 表示建和值的类型.

但是有时候你会需要更多的灵活性, 有一种方法可以做到这一点: 
将键进行参数化而不是将容器进行参数化. 然后将参数化的键提交给容器, 来插入或获取值. 用泛型系统来确保值的类型与它的键相符.

举例:

```
public class Favorites {
public <T> void putFavorite(Class<T> type, T instance);
public <T> T getFavorite(Class<T> type);
}
```

Favorites实例是类型安全的, 同时也是异构的(heterogeneous): 它的所有键都是不同类型的. 所以Favorites被称作类型安全的异构容器.

Favorites的内部实现用了`HashMap<Class<?>, Object>`, `getFavorite()`方法的实现用了动态转换: `type.cast()`.

为了确保类型约束, 可以在`putFavorite()`方法中加入动态转换, 检验instance是否真的是type所表示的类型的实例. 

java.util.Collections中有一些集合包装类采用了同样的技巧. (checkedSet, checkedList, checkedMap).

```java
// Typesafe heterogeneous container pattern (Pages 151-4)
public class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNonNull(type), instance);
    }

    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }

//    // Achieving runtime type safety with a dynamic cast
//    public <T> void putFavorite(Class<T> type, T instance) {
//        favorites.put(Objects.requireNonNull(type), type.cast(instance));
//    }

    public static void main(String[] args) {
        Favorites f = new Favorites();
        f.putFavorite(String.class, "Java");
        f.putFavorite(Integer.class, 0xcafebabe);
        f.putFavorite(Class.class, Favorites.class);
        String favoriteString = f.getFavorite(String.class);
        int favoriteInteger = f.getFavorite(Integer.class);
        Class<?> favoriteClass = f.getFavorite(Class.class);
        System.out.printf("%s %x %s%n", favoriteString,
                favoriteInteger, favoriteClass.getName());
    }
}
```

```java
// Use of asSubclass to safely cast to a bounded type token (Page 155)
public class PrintAnnotation {
    static Annotation getAnnotation(AnnotatedElement element,
                                    String annotationTypeName) {
        Class<?> annotationType = null; // Unbounded type token
        try {
            annotationType = Class.forName(annotationTypeName);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
        return element.getAnnotation(
                annotationType.asSubclass(Annotation.class));
    }

    // Test program to print named annotation of named class
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println(
                "Usage: java PrintAnnotation <class> <annotation>");
            System.exit(1);
        }
        String className = args[0];
        String annotationTypeName = args[1]; 
        Class<?> klass = Class.forName(className);
        System.out.println(getAnnotation(klass, annotationTypeName));
    }
}
```

# Chapter 6 枚举和注解

## 第34条 用enum代替int常量

在编程语言中还没有引入枚举类型之前, 表示枚举类型的常用模式是声明一组具名的int常量. 
这种方法称作**int枚举模式**. 存在诸多不足, 在类型安全性和使用方便性方面没有任何帮助.

采用int枚举模式的程序是十分脆弱的, 因为int枚举是编译时常量, 被编译到使用它们的客户端中. 
如果与枚举常量关联的int发生了变化, 客户端就必须重新编译. 如果不重新编译, 程序还是可以运行, 但是行为是不确定的.

而且, 要打印int枚举, 所见到的只是一个数字. 要遍历一个组中所有的int枚举常量, 或者获得int枚举组的大小, 这些都没有很可靠的方法.

你还可能碰到这种模式的变体: 使用String常量, 被称作**String枚举模式**.
虽然提供了可打印的字符串, 但是会有性能问题, 因为它依赖于字符串的比较操作. 
更糟糕的是, 它会导致初级用户把字符串常量硬编码到客户端代码中, 而不是使用适当的field. 
如果这样的硬编码字符串常量中包含书写错误, 编译时不会检测到, 在运行时会报错.

Java1.5开始提供了枚举类型.
枚举类型: 实例受控, 是单例的泛型化, 本质上是单元素的枚举.

枚举类型提供的优点:

* 编译时的类型安全.
* 多个枚举类型可包含同名常量.
* 增加或重新排列枚举类型中的常量, 无需重新编译它的客户端代码.
* `toString()`方法将枚举转化成可打印的字符串.
* 允许添加任意的方法和域, 并实现任意的接口. 
* 提供了`Object`方法的实现, 实现了`Comparable`和`Serializable`.
* 静态的`values()`方法可以按照声明顺序返回它的值数组.

为了将数据与枚举常量关联, 要声明实例域, 并编写一个带有数据并将数据保存在域中的构造器. 
枚举天生不可变**, 因此所有的域都是final的.**

有时候需要将不同的行为与每个常量关联起来, 可以在枚举中定义抽象方法, 这样添加新的常量的时候就必须提供这个方法.

如果多个枚举常量同时共享相同的行为, 则考虑策略枚举.

枚举的性能与int常量相比是相当的, 有个微小的性能缺点, 即装载和初始化枚举时会有空间和时间的成本, 但是实践上通常是可忽略的.

```java
// Enum type with data and behavior  (159-160)
public enum Planet {
    MERCURY(3.302e+23, 2.439e6),
    VENUS  (4.869e+24, 6.052e6),
    EARTH  (5.975e+24, 6.378e6),
    MARS   (6.419e+23, 3.393e6),
    JUPITER(1.899e+27, 7.149e7),
    SATURN (5.685e+26, 6.027e7),
    URANUS (8.683e+25, 2.556e7),
    NEPTUNE(1.024e+26, 2.477e7);

    private final double mass;           // In kilograms
    private final double radius;         // In meters
    private final double surfaceGravity; // In m / s^2

    // Universal gravitational constant in m^3 / kg s^2
    private static final double G = 6.67300E-11;

    // Constructor
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        surfaceGravity = G * mass / (radius * radius);
    }

    public double mass()           { return mass; }
    public double radius()         { return radius; }
    public double surfaceGravity() { return surfaceGravity; }

    public double surfaceWeight(double mass) {
        return mass * surfaceGravity;  // F = ma
    }
}

```

```java
// Takes earth-weight and prints table of weights on all planets (Page 160)
public class WeightTable {
   public static void main(String[] args) {
      double earthWeight = Double.parseDouble(args[0]);
      double mass = earthWeight / Planet.EARTH.surfaceGravity();
      for (Planet p : Planet.values())
         System.out.printf("Weight on %s is %f%n",
                 p, p.surfaceWeight(mass));
   }
}
```

```java
// Enum type with constant-specific class bodies and data (Pages 163-4)
public enum Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;

    Operation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol; }
    // 上面的+-*/都必须对这个方法进行覆盖
    public abstract double apply(double x, double y);

    // Implementing a fromString method on an enum type (Page 164)
    private static final Map<String, Operation> stringToEnum =
            Stream.of(values()).collect(
                    toMap(Object::toString, e -> e));

    // Returns Operation for string, if any
    public static Optional<Operation> fromString(String symbol) {
        return Optional.ofNullable(stringToEnum.get(symbol));
    }

    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        for (Operation op : Operation.values())
            System.out.printf("%f %s %f = %f%n",
                    x, op, y, op.apply(x, y));
    }
}
```



枚举中的switch语句不是在枚举中实现特定于常量的行为的一种很好的选择

枚举中的switch语句适合于给外部的枚举类型增加特定于常量的行为，假如，假设Operation枚举不受你的控制，

你希望他有一个实例方法来返回每个运算的反运算，你可以用下列静态方法来模拟这种效果：

```java
// Switch on an enum to simulate a missing method (Page 167)
//switch语句在枚举类中的使用
public class Inverse {
    public static Operation inverse(Operation op) {
        switch(op) {
            case PLUS:   return Operation.MINUS;
            case MINUS:  return Operation.PLUS;
            case TIMES:  return Operation.DIVIDE;
            case DIVIDE: return Operation.TIMES;

            default:  throw new AssertionError("Unknown op: " + op);
        }
    }

    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        for (Operation op : Operation.values()) {
            Operation invOp = inverse(op);
            System.out.printf("%f %s %f %s %f = %f%n",
                    x, op, y, invOp, y, invOp.apply(op.apply(x, y), y));
        }
    }
}
```

枚举使用场景：每当需要一组固定常量，并且在编译时就知道其成员的时候，就应该使用枚举类型。

## 第35条 用实例域代替序数

所有的枚举都有一个`ordinal()`方法, 返回每个枚举常量在类型中的数字位置.

永远不要根据枚举的序数导出与它关联的值, 而是要将它保存在一个实例域中.

```java
// Enum with integer data stored in an instance field (Page 168)
public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5),
    SEXTET(6), SEPTET(7), OCTET(8), DOUBLE_QUARTET(8),
    NONET(9), DECTET(10), TRIPLE_QUARTET(12);

    private final int numberOfMusicians;
    Ensemble(int size) { this.numberOfMusicians = size; }
    public int numberOfMusicians() { return numberOfMusicians; }
}
```

## 第36条 用EnumSet代替位域

如果一个枚举类型的元素主要用在集合中, 一般就用int枚举模式, 将2的不同倍数赋予每个常量.

这种表示法让你用OR运算将几个常量合并到一个集合中, 称作位域(bit field).

位域表示法也允许利用位操作有效地执行像联(union)合和交集(intersection)这样的集合操作.

但位域有着int枚举常量的所有缺点. (无法打印, 无法遍历.)

java.util提供了`EnumSet`类来有效地表示从单个枚举类型中提取的多个值的多个集合.

```java
// EnumSet - a modern replacement for bit fields (Page 170)
public class Text {
    public enum Style {BOLD, ITALIC, UNDERLINE, STRIKETHROUGH}

    // Any Set could be passed in, but EnumSet is clearly best
    public void applyStyles(Set<Style> styles) {
        System.out.printf("Applying styles %s to text%n",
                Objects.requireNonNull(styles));
    }

    // Sample use
    public static void main(String[] args) {
        Text text = new Text();
        text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
    }
}
```

## 第37条 用EnumMap代替序数索引

有时候, 你可能会见到用`ordinal`方法来索引数组的代码.

利用`EnumMap`可以更好地解决问题. (一维和多维的例子.)

```java
// Using an EnumMap to associate data with an enum (Pages 171-3)

// Simplistic class representing a plant (Page 171)
class Plant {
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL }

    final String name;
    final LifeCycle lifeCycle;

    Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }

    @Override public String toString() {
        return name;
    }

    public static void main(String[] args) {
        Plant[] garden = {
            new Plant("Basil",    LifeCycle.ANNUAL),
            new Plant("Carroway", LifeCycle.BIENNIAL),
            new Plant("Dill",     LifeCycle.ANNUAL),
            new Plant("Lavendar", LifeCycle.PERENNIAL),
            new Plant("Parsley",  LifeCycle.BIENNIAL),
            new Plant("Rosemary", LifeCycle.PERENNIAL)
        };

        // Using ordinal() to index into an array - DON'T DO THIS!  (Page 171)
        Set<Plant>[] plantsByLifeCycleArr =
                (Set<Plant>[]) new Set[LifeCycle.values().length];
        for (int i = 0; i < plantsByLifeCycleArr.length; i++)
            plantsByLifeCycleArr[i] = new HashSet<>();
        for (Plant p : garden)
            plantsByLifeCycleArr[p.lifeCycle.ordinal()].add(p);
        // Print the results
        for (int i = 0; i < plantsByLifeCycleArr.length; i++) {
            System.out.printf("%s: %s%n",
                    LifeCycle.values()[i], plantsByLifeCycleArr[i]);
        }

        // Using an EnumMap to associate data with an enum (Page 172)
        Map<LifeCycle, Set<Plant>> plantsByLifeCycle =
                new EnumMap<>(LifeCycle.class);
        for (LifeCycle lc : LifeCycle.values())
            plantsByLifeCycle.put(lc, new HashSet<>());
        for (Plant p : garden)
            plantsByLifeCycle.get(p.lifeCycle).add(p);
        System.out.println(plantsByLifeCycle);

        // Naive stream-based approach - unlikely to produce an EnumMap!  (Page 172)
        System.out.println(Arrays.stream(garden)
                .collect(groupingBy(p -> p.lifeCycle)));

        // Using a stream and an EnumMap to associate data with an enum (Page 173)
        System.out.println(Arrays.stream(garden)
                .collect(groupingBy(p -> p.lifeCycle,
                        () -> new EnumMap<>(LifeCycle.class), toSet())));
    }
}
```

## 第38条 用接口模拟可扩展的枚举

Java语言上是不支持枚举继承的, 这并不意外, 因为枚举的扩展最后证明都不是什么好点子. 
如果枚举可以继承, 那么怎么列举基本类型的所有元素及其扩展? 最终, 扩展会导致设计和实现的许多方面变得复杂起来.

但是有时候会有这种需求, 例子: 定义操作的枚举类型, 允许用户扩展自己的操作.

解决方法: 基本操作的枚举实现接口, 用户可以定义新的操作类型枚举, 只要实现这个接口就可以.

虽然无法编写可扩展的枚举类型, 却可以通过编写接口以及实现该接口的基础枚举类型, 对它进行模拟. 这样允许客户端编写自己的枚举来实现接口. 
如果API是根据接口编写的, 那么在可以使用基础枚举类型的任何地方, 也都可以使用这些枚举.

```java
public interface Operation {
    double apply(double x, double y);
}
```

```java
// Emulated extensible enum using an interface - Basic implementation (Page 176)
public enum BasicOperation implements Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;

    BasicOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override public String toString() {
        return symbol;
    }
}
```





```java
// Emulated extensible enum (Pages 176-9)
public enum ExtendedOperation implements Operation {
    EXP("^") {
        public double apply(double x, double y) {
            return Math.pow(x, y);
        }
    },
    REMAINDER("%") {
        public double apply(double x, double y) {
            return x % y;
        }
    };
    private final String symbol;
    ExtendedOperation(String symbol) {
        this.symbol = symbol;
    }
    @Override public String toString() {
        return symbol;
    }

//    // Using an enum class object to represent a collection of extended enums (page 178)
//    public static void main(String[] args) {
//        double x = Double.parseDouble(args[0]);
//        double y = Double.parseDouble(args[1]);
//        test(ExtendedOperation.class, x, y);
//    }
//    private static <T extends Enum<T> & Operation> void test(
//            Class<T> opEnumType, double x, double y) {
//        for (Operation op : opEnumType.getEnumConstants())
//            System.out.printf("%f %s %f = %f%n",
//                    x, op, y, op.apply(x, y));
//    }

    // Using a collection instance to represent a collection of extended enums (page 178)
    public static void main(String[] args) {
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        test(Arrays.asList(ExtendedOperation.values()), x, y);
    }
    private static void test(Collection<? extends Operation> opSet,
                             double x, double y) {
        for (Operation op : opSet)
            System.out.printf("%f %s %f = %f%n",
                    x, op, y, op.apply(x, y));
    }
}
```

## 第39条 注解优先于命名模式

Java 1.5之前, 一般使用命名模式(naming pattern)表明有些程序元素需要通过某种工具或者框架进行特殊处理. 
比如JUnit3要求测试方法名以test开头. 这样做有很多缺点, 注解很好地解决了这些问题.

代码例子:

* `@Test`注解.
* 有参数的`@ExceptionTest`注解.
* Java 8的`@Repeatable`注解.

## 第40条 坚持使用Override注解

举例: 覆写了`equals()`方法(还有`hashCode()`), 但是却没有得到期待的结果, Set中添加了好多重复的实例.

为什么呢? 因为`equals()`方法的参数写成了具体类型, 应该写`Object`. 没有加`@Override`注解的时候, 编译器不会报错, 而是把它当做一次方法重载.

加上`@Override`注解, 编译器会提示, 可以及时发现错误.

应该在你想要覆盖超类声明的每个方法声明中使用`@Override`注解.

如果不写IDE会有警告.

如果是实现抽象方法, 不写`@Override`注解IDE不会警告, 但是这样做没什么坏处.

## 第41条 用标记接口定义类型

标记接口(marker interface)是没有包含方法声明的接口, 而只是指明一个类实现了具有某种属性的接口. 例如`Serializable`接口.

标记接口和标记注解各有用处.

# Chapter 7 Lambda表达式和流

本章内容都是Java 8新增特性的使用相关.

## 第42条 优先使用lambdas而不是匿名类

以前, 只有一个抽象方法的接口(或抽象类)被当做function types使用. 它们的实例是函数对象(function objects), 表示功能或者行为.
从JDK1.1开始, 主要的创建函数对象的行为是匿名类(anonymous class).

匿名类很适合经典的需要函数对象的设计模式, 比如策略模式. 举例 -> `Comparator`提供了抽象策略, 匿名类实现具体策略.

在Java 8中, 认为这种只有一个抽象方法的接口值得被特殊对待, 它们现在被称为函数式接口(functional interfaces), 语言允许你用lambda表达式创建这些接口的实例.

Lambda和匿名类的功能类似, 但是更加简洁.

Lambda表达式可以省略参数和返回值的类型, 这是因为编译器会通过类型推断(type inference)推导出来. (不能推导出来的时候需要指明.)

举例: 之前各个枚举行为不同的例子, 可以用lambda简化, 构造的时候传入lambda, 用`DoubleBinaryOperator`保存.

注意: 由于lambda是没有名字和文档的, 如果一个计算不是自解释的, 或是行数较多(对于lambda来说, 一行最好, 三行最多), 就不要放在lambda中了.

通过enum构造传入的参数是在静态环境的, 所以从enum构造传入的lambda不能访问枚举的成员变量.

仍然有一些事情是匿名类可以做, 但是不能被lambda取代的: 

* lambda只被限制在函数式接口(functional interfaces), 所以抽象类, 多个方法的接口仍然要用匿名类.
* 在lambda中, 不能获取自身的引用, `this`关键字指的是enclosing instance. 匿名类中`this`指这个匿名类对象.

lambda和匿名类都不能可靠地序列化和反序列化, 所以, 你应该少做这件事. 如果真的有需要, 用一个私有静态内部类的实例.

## 第43条 优先使用方法引用而不是lambdas

用lambda取代匿名类的主要优势就是简洁, 其实Java还提供了更简洁的生成函数对象的方法: 方法引用(method references).

代码实例: 统计单词个数的方法, 用Map接口的`merge`方法(Java 8):

```
map.merge(key, 1, (count, incr) -> count + incr);
```

改进:

```
map.merge(key, 1, Integer::sum);
```

为了更加简洁, 可以把lambda中的代码提取到一个新方法中, 然后用这个方法引用取代原来的lambda.
但是偶然的情况下, lambda比方法引用更加简洁. 比如: 方法和lambda在同一个类中.

很多方法引用都是静态的, 但是有四种非静态的: 

* bound and unbound instance method references
* classes和arrays的constructor references, 作为工厂对象.

结论: 当方法引用比lambda更加简洁的时候, 就用方法引用, 否则就用lambda.

## 第44条 优先使用标准的函数式接口

有了lambda之后, 模板方法(Template Method)模式就没有吸引力了, 现代的方法是提供一个接收函数对象的静态工厂或者构造函数来达到相同的效果.
更一般地, 你需要写更多的以函数对象作为参数的构造器和方法. 要谨慎选择正确的函数参数类型.

`java.util.function`包中提供了一系列标准的函数式接口(一共43个).

六个基本的函数式接口:

* `UnaryOperator<T>`: 一个参数, 返回值类型和参数相同.
* `BinaryOperator<T>`: 两个参数, 返回值类型和参数相同.
* `Predicate<T>`: 一个参数, 返回一个boolean.
* `Function<T,R>`: 参数和返回值类型不同.
* `Supplier<T>`: 无参数, 有返回值.
* `Consumer<T>`: 有参数, 无返回值.

每个基本接口都有3种变型, 对应基本类型: int, long和double, 接口名字会加上类型的前缀. (6*3=18个)

`Function`接口还有9种变型, 对应结果的不同基本类型. 
如果源和结果都是基本类型, 加上前缀`SrcToResult`, 比如`LongToIntFunction`. (6种).
如果源是基本类型, 结果是对象引用, 加上前缀`SrcToObj`, 比如`DoubleToObjFunction`. (3种).

有三个基本的函数式接口都有双参数版本, 共有9种变型:

* `BiPredicate<T, U>` (1种).
* `BiFunction<T, U, R>`: 有三种返回不同基本类型的变种. (4种).
* `BiConsumer<T>`: 还有接受一个基本类型和一个对象引用的变种. (4种).

最后还有`BooleanSupplier`是一个返回布尔值的Supplier变型.

大多数的标准函数式接口仅是为了提供基本类型(primitive types)支持而存在. 
不要用基本函数式接口搭配装箱基本类型(basic functional interfaces with boxed primitives)来代替基本类型的函数式接口(primitive functional interfaces).

那什么时候应该写自己的函数式接口呢?

* 没有标准的函数式接口能够满足需求. -> 比如参数个数不同, 或者要抛出一个受检异常.
* 有结构相同的标准函数式接口, 有一些情况也应该写自己的函数式接口. 
  比如`Comparator<T>`和`ToIntBiFunction<T,T>`结构相同, 但是仍然用`Comparator`, 好处: 名字; 通用协议; 有用的默认方法.

如果你要自己写一个函数式接口而不是用标准的, 你要考虑它是不是和`Comparator`一样拥有(一个或多个)以下特性:

* 它通用, 会得益于有一个描述性的名字.
* 它与一个很强的协议相关.
* 它会得益于自定义的默认方法.

永远记住用注解`@FunctionalInterface`来标记你的函数式接口.

提供方法重载的时候要注意, 不要给同一个方法提供函数式接口在同一个参数位置的重载(有可能会引起二义性). 比如: `ExecutorService`的`submit`方法.
这需要客户端代码进行强转来指明正确的重载.

## 第45条 谨慎使用streams

### Stream API介绍

Java 8新增的streams API主要是为了更方便地进行批量操作, 串行的或者并行的.
两个关键的抽象: 

* stream: 有限或无限的数据元素序列. (支持对象引用或基本类型: int, long, double).
* stream pipeline: 对这些元素进行的多级运算. (0个或多个intermediate operations和一个terminal operation).

stream pipeline的运算是lazy的, 只有当terminal operation被触发的时候才会开始进行运算, 对最终操作不必要的数据将不会被运算(无限数据成为可能).

streams API是流式的.

### 为什么要谨慎使用Streams

适当地使用streams API可以让程序更简洁, 但是使用不当(过度使用)可能会降低可读性和可维护性.

举例: 寻找anagram(相同字母异序词).
Java 8新增方法: `computeIfAbsent`.

关于stream pipeline的可读性:

* 缺少明确的类型时, lambda参数的良好命名是必要的.
* 使用辅助方法, 提取逻辑并命名.

不要用stream来处理char values, 因为它们是int值, 需要强转.

### Stream和循环迭代的比较

stream pipeline使用函数对象(function objects), 通常是lambda和方法引用; 循环迭代(iterative code)使用的是代码块.

代码块可以做但是函数对象不能做的事情(循环可以做, 但stream不可以做):

* 代码块中可以读取和修改scope中的任何局部变量; lambda中只能读取final的, 不能修改任何局部变量.
* 循坏代码块中可以return, break或continue, 抛出方法声明的受检异常; lambda中不能做这些.

stream擅长的事情:

* 统一处理元素序列.
* 过滤.
* 联合元素的运算(加, 连接, 算最小值等).
* 将元素序列累积到集合中, 或分组.
* 查询.

用stream的时候比较难做到的一点是, 在pipeline的处理过程中, 很难找到对应的原来元素: 一旦map到新的值了, 前面的值就丢了.
比较lame的做法是用个pair, 还可以考虑在需要原始值的时候反向map.

例子: 打印20个梅森质数(Mersenne primes): 2的n次方仍然是个质数. 现在想找出对应的n -> 加一个反向运算.

有时候迭代循环和stream都能很好地解决问题(例: 遍历扑克牌组合), 具体选哪一种看情况(更喜欢哪个就用哪个, 同事是否接受你的偏好等).

## 第46条 优先使用streams中没有副作用的方法

Streams不仅是API, 它是基于函数式编程的饿一个范式.

Streams范式中最重要的一点: 把计算组织成一系列的转换, 每一个阶段都尽量是前一个阶段结果的单纯函数(pure function: 无状态 -> free of side-effects).
注意: pure function的结果只依赖于自己的输入, 不会依赖于任何mutable的状态, 也不会修改任何状态.

举例: 统计单词频次: `forEach` -> bad; `collect` -> good.

Stream中的`forEach`有着浓烈的迭代气息, 应该只被用来报告结果, 不应该用来进行计算, 通常是bad smell.

```
freq.keySet().stream() 
.sorted(comparing(freq::get).reversed()) 
.limit(10)
.collect(toList()); // 静态引入了Collectors
```

Collectors API主要是封装了降维操作(reduction strategy), 它的结果通常是一个集合.

注: Scanner的`stream`方法是Java 9加的, 之前的版本可以用`streamOf(Iterable<E>)`.

Collectors API中的方法(一共有39个):

* `toList()`, `toSet()`, `toCollection(collectionFactory)`: 把stream中的元素放在一个集合中.
* `toMap(keyMapper, valueMapper)`: 参数的两个方法, 一个把元素转换到key, 一个转换到value. -> 冲突的时候会抛异常.
* `toMap(keyMapper, valueMapper, mergeFunction)`: 同样key的value会merge. 应用1: 选择value, 例: 选择歌手最畅销的唱片; 应用2: 最后的value胜出.
* `toMap(keyMapper, valueMapper, mergeFunction, mapSupplier)`: mapSupplier是一个map工厂, 用来指定具体的map实现: EnumMap或TreeMap.
* 三个`toMap`方法都有相应的`toConcurrentMap`方法, 创建`ConcurrentHashMap`实例.
* `groupingBy(classifier)`方法用来返回按照类别分组的元素, 分类器(classifier function)返回元素应属于的类别(category), 作为map的key.
* `groupingBy(classifier, downstream)`: downstream将对分类中的所有元素进一步处理, 可以`toSet()`, `toCollection()`或者`counting()`.
* `groupingBy(classifier, mapFactory, downstream)`: 可以同时控制map和集合的实现类型, 比如一个`TreeMap`, 包含`TreeSet`.
* 类似的, 三个`groupingBy`方法的重载都提供`groupingByConcurrent`方法.
* `partitioningBy(predicate)`返回key为`Boolean`类型的map, 还有一种重载形式加了downstream参数.
* 仅用作downstream的方法. 比如`counting`, 以`summing`, `averaging`, `summarizing`开头的方法, `Stream`中都有相应的方法. 还有`reducing`, `filtering`, `mapping`, `flatMapping`, `collectingAndThen`等.
* `minBy`和`maxBy`是`BinaryOperator`中相应方法的类似物.
* `joining`: 有三种重载, 以特定分隔符和前后缀组合字符串.


注: 文档: [Java 8 Collectors](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html).

## 第47条 优先使用Collection而不是Stream作为返回值

`Stream`虽然有一个符合`Iterable`接口的规定的用于遍历的方法, 但是`Stream`却没有继承`Interable`接口. 
所以想要遍历Stream, 需要提供一个适配方法:

```
// Adapter from  Stream<E> to Iterable<E>
public static <E> Iterable<E> iterableOf(Stream<E> stream) {
       return stream::iterator;
}
```

如果想要用stream pipeline处理一个Iterable的接口, 也需要写个适配方法:

```
// Adapter from Iterable<E> to Stream<E>
public static <E> Stream<E> streamOf(Iterable<E> iterable) {
       return StreamSupport.stream(iterable.spliterator(), false);
}
```

如果你在写一个方法, 返回一系列的对象, 你应该为使用者要用stream pipeline和for-each的两种可能性都做好准备.

`Collection`接口是`Iterable`的子类型, 还有一个`stream`方法, 所以`Collection`或其一个合适的子类型, 通常是返回序列的公有方法返回值的最好选择.

数组有`Arrays.asList`和`Stream.of`方法.

但是不要把很大的序列放在内存中, 仅仅是为了作为集合返回. -> 举例: PowerSet. -> 自定义collection.
自定义集合可以限制元素大小, 但如果数据太大或者数量不确定, 可以考虑用stream或者iterable.

有时候也会根据实现的方法更直观而选择返回类型, 例子: 用stream处理生成字符串子集集合.

PS: 如果以后`Stream`继承了`Iterable`, 就可以自由返回stream了, 因为这样就两种可能性(流式, 遍历)都提供了.

## 第48条 把流变为并行时要多加小心

如果一个pipeline的source是从Stream.iterate来的, 或者`limit`作为方法在其中被使用了, 那么把这个pipeline变为并行是不太可能提高性能的.
举例: 寻找20个梅森素数的例子, 加上`parallel()`方法之后程序不输出了, 只能强制停止.

不要不加选择地把stream并行化, 性能影响可能是灾难性的.

### 数据结构

并行的性能增益在这些流上是最好的: `ArrayList`, `HashMap`, `HashSet`, `ConcurrentHashMap`实例; 数组; int ranges; long ranges.
这些数据结构的的共同特点:

* 它们都可以准确且廉价地分成任何所需大小的子集, 这使得在并行线程之间划分工作变得容易.
* 它们都提供了优秀的引用地址(locality of reference): 顺序元素引用在内存中存储在一起. 
  引用地址(Locality-of-reference)对于并行化批量操作至关重要. 没有它, 线程将会大部分时间空闲, 等待数据从内存传输到处理器缓存中.

### stream pipeline的最终操作也会影响并行的执行.

如果比起整个pipeline的工作来说, 最终操作承担了大量的工作, 而且这个操作还是串行的, 那么把pipeline并行化的作用很有限.
对并行来说, 最好的最终操作是降维操作(reductions), 比如`reduce`, `max`, `min`, `count`, `sum`.
短路操作比如`anyMatch`, `allMatch`, `nonMatch`对于并行来说也适用.
但是Stream的`collect`方法(mutable reductions), 就不是很适合并行了, 因为合并集合开销大.


如果你自己实现`Stream`, `Iterable`或者`Collection`, 想要比较好的并行性能, 你必须覆写`spliterator`方法, 并且广泛地测试流的并行性能.

并行化流不仅会导致性能不佳，包括活动失败(liveness failures); 它可能导致不正确的结果和不可预测的行为(safety failures).

在正确的情形下, 在stream pipeline上加上`parallel`调用是有可能实现处理器内核数量的近线性加速的.

如果要平行的流是随机数字的, 应该使用`SplittableRandom`, 而不是`ThreadLocalRandom`(单线程用), 或`Random`(每个操作都同步).

# Chapter 8 方法

## 第49条 检查参数的有效性

方法的参数限制, 应该在文档中指明, 并且在方法体的开头处检查参数, 以强制施加这些限制.

对于公有的方法, 要用Javadoc的`@throws`标签在文档中说明违反参数值限制时会抛出的异常.

Java 7新增了方法`Objects.requireNonNull`, 很好用.
Java 9新增了`checkFromIndexSize`, `checkFromToIndex`, 和`checkIndex`.

非公有的方法通常应该使用断言(assertion)来检查它们的参数. 如果断言失败会抛出AssertionError, 如果它们没有起到作用, 本质上也不会有成本开销. 
(除非通过将-ea或者-enableassertions标记传递给Java解释器来启用它们.)

## 第50条 必要时进行保护性拷贝

你的类是否能够容忍对象进入数据结构之后发生变化? 如果答案是否定的, 就必须对该对象进行保护性拷贝, 并且让拷贝之后的对象而不是原始对象进入到数据结构中.

在内部组件被返回给客户端之前, 对它们进行保护性拷贝也是同样的道理.

如果参数的类型是可以被不被信任的人子类化的, 那么对参数进行保护性拷贝的时候, 不要使用`clone`方法.

只要有可能, 都应该使用不可变的对象作为对象内部的组件, 这样就不必再为保护性拷贝操心.

如果拷贝的成本受到限制, 并且类信任它的客户端不会不恰当地修改组件, 就可以在文档中指明客户端的职责是不得修改受到影响的组件, 以此来代替保护性拷贝.

## 第51条 谨慎设计方法签名

API设计技巧:

* 谨慎选择方法名.
* 不要过于追求提供便利的方法. 
* 避免过长的参数列表. -> 1.分解成多个方法; 2.创建辅助类, 用来保存参数的分组; 3.从对象构建到方法调用都采用Builder模式.
* 参数类型优先使用接口而不是类.
* 对于boolean参数, 要优先使用两个元素的枚举类型.

## 第52条 慎用重载

对于重载(overload)方法的选择是静态的, 而对于被覆盖(override)方法的选择则是动态的.

选择被覆盖方法的正确版本是在运行时进行的, 选择的依据是被调用方法所在对象的运行时类型.
所以子类方法与基类签名相同, 则覆盖基类, 尽管对象声明为基类, 但是调用时用的是子类的实现.

但重载的选择工作是在编译时进行的, 完全基于参数的编译时类型. 这样的代码很容易使人感到困惑.

安全而保守的策略是: 永远不要导出两个具有相同参数数目的重载方法. 如果方法使用可变参数(varargs), 保守的策略是不要重载它.

这项限制并不麻烦, 因为你始终可以给方法起不同的名称而不使用重载机制.

对于构造器, 没有选择不同名称的机会, 在许多情况下, 可以选择导出静态工厂.

当然如果对于每一种重载方法, 至少有一个对应的参数在两个重载方法中具有根本不同的类型, 就不会产生迷惑. 在这种情况下主要的混淆根源就消除了.

注意泛型和自动装箱会引起的歧义性, 举例 -> `List<E>`的`remove(E)`和`remove(int)`.

Java 8引入的lambda和方法引用更增加了可能会引起重载解析歧义的可能性. -> 重载方法中, 不要在同样的参数位置接受不同的函数式接口.

## 第53条 慎用可变参数

可变参数机制通过先创建一个数组, 数组的大小为在调用位置所传递的参数数量, 然后将参数传到数组中, 最后将数组传递给方法.

在重视性能的情况下, 使用可变参数机制要特别小心.

在定义参数数目不定的方法时, 可变参数是一种很方便的方式, 但是它们不应该被过度滥用.

```java
// Sample uses of varargs (Pages 245-6)
public class Varargs {
    // Simple use of varargs (Page 245)
    static int sum(int... args) {
        int sum = 0;
        for (int arg : args)
            sum += arg;
        return sum;
    }

//    // The WRONG way to use varargs to pass one or more arguments! (Page 245)
//    static int min(int... args) {
//        if (args.length == 0)
//            throw new IllegalArgumentException("Too few arguments");
//        int min = args[0];
//        for (int i = 1; i < args.length; i++)
//            if (args[i] < min)
//                min = args[i];
//        return min;
//    }

    // The right way to use varargs to pass one or more arguments (Page 246)
    static int min(int firstArg, int... remainingArgs) {
        int min = firstArg;
        for (int arg : remainingArgs)
            if (arg < min)
                min = arg;
        return min;
    }

    public static void main(String[] args) {
        System.out.println(sum(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        System.out.println(min(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }
}

```



## 第54条 返回零长度的数组或集合, 而不是null

返回类型为数组或集合的方法, 应该返回一个零长度的数组或者集合, 没理由返回null. -> 不好用, 容易出错, 没有性能优势.

开销考虑:

* 在这个级别上担心性能问题是不明智的, 除非分析表明这个方法是造成性能问题的真正源头.
* 对于不返回任何元素的调用, 每次都返回同一个零长度数组是有可能的. (例如: `Collections.emtpySet`).

## 第55条 明智地返回optionals

在Java 8之前, 当一个方法无法返回值的时候有两种选择: 返回null或者抛出异常.
Java 8推出了一个新的解决方案: `Optional<T>`: 不可变容器, 含有一个或0个值.

Optional的精神和checked exception一样, 强迫用户意识到返回值有可能是为空的.
例子:

```
// Using an optional to provide a chosen default value 
String lastWordInLexicon = max(words).orElse("No words...");

// Using an optional to throw a chosen exception
Toy myToy = max(toys).orElseThrow(TemperTantrumException::new);

// Using optional when you know there’s a return value 
Element lastNobleGas = max(Elements.NOBLE_GASES).get();

```

如果默认值的计算比较重, 可以用`orElseGet`方法.
还有很多方法供各种用途: `filter`, `map`, `flatMap`, `ifPresent`等.

如果各种方法都不能满足你的需要, `isPresent`方法是一个很方便的方法, 它有值返回true, 无值返回false, 可以用来实现各种需求. 
(不过通常可以用上面的各种方法更加优雅地解决问题.)

也不是所有的类型都可以从Optional受益, 容器类型(collections, maps, streams, arrays)和optionals不应该再用Optional包装.

Optional也不适用于性能关键的情形.

不要用Optional包装基本类型的装箱类型.
对基本类型的装箱类型返回Optional是有性能开销的, 还不如直接返回基本类型的默认值0.
库提供了`OptionalInt`, `OptionalLong`和`OptionalDouble`.

通常, 用optional作为key, value或者集合中的元素都是不合适的, 会造成不必要的复杂性.
把optional保存在字段中也通常是一个bad smell. 但是也有例外, 比如想要合理地表达absence.

```java
// Using Optional<T> as a return type (Pages 249-251)
public class Max {
//    // Returns maximum value in collection - throws exception if empty (Page 249)
//    public static <E extends Comparable<E>> E max(Collection<E> c) {
//        if (c.isEmpty())
//            throw new IllegalArgumentException("Empty collection");
//
//        E result = null;
//        for (E e : c)
//            if (result == null || e.compareTo(result) > 0)
//                result = Objects.requireNonNull(e);
//
//        return result;
//    }

//    // Returns maximum value in collection as an Optional<E> (Page 250)
//    public static <E extends Comparable<E>>
//    Optional<E> max(Collection<E> c) {
//        if (c.isEmpty())
//            return Optional.empty();
//
//        E result = null;
//        for (E e : c)
//            if (result == null || e.compareTo(result) > 0)
//                result = Objects.requireNonNull(e);
//
//        return Optional.of(result);
//    }

    // Returns max val in collection as Optional<E> - uses stream (Page 250)
    public static <E extends Comparable<E>>
    Optional<E> max(Collection<E> c) {
        return c.stream().max(Comparator.naturalOrder());
    }

    public static void main(String[] args) {
        List<String> words = Arrays.asList(args);

        System.out.println(max(words));

        // Using an optional to provide a chosen default value (Page 251)
        String lastWordInLexicon = max(words).orElse("No words...");
        System.out.println(lastWordInLexicon);
    }
}

```



## 第56条 为所有导出的API元素编写文档注释

Javadoc可以根据源代码自动生成API文档.

要正确地为API建立文档, 就必须在每个导出的类, 接口, 构造函数, 方法和字段声明之前加上doc注释.

方法的文档注释应该简洁地描述出它和客户端之间的约定. **这个约定应该说明这个方法做了什么, 而不是如何完成这项工作的.**

方法的文档注释还应该列举出:

* 所有前提条件. 一般可以利用`@throws`, `@param`.
* 后置条件.
* 副作用.
* 线程安全性.
* 每个参数: `@param` 名词短语.
* 返回值: `@return` 名词短语. (除非和方法描述一致时, 可根据所遵循的规定省略.)
* 每个异常: `@throws` 含if的名词短语. 


按惯例, `@param`, `@return`, `@throws`后面的短语或句子都不用句点来结束.

`{@code}`用来标记代码, 多行代码要加上`<pre>`标签, 变成: `<pre>{@code xxx}</pre>`.
注意代码中的注解符号@需要被省略.

按照惯例, 方法的文档注释中的"this"指代的是当前的对象.

Java 8新增`@implSpec`: 描述方法和子类的关系.
Java 9中Javadoc utility会忽略`@implSpec`, 除非你在命令行加上"implSpec:a:Implementation Requirements:"

如果文档中要用HTML中的元素, 比如`<`, `>`和`&`, 需要加上`{@literal}`标签.

文档注释必须在代码和生成文档中都保证可读性, 如果不能两者都保证, 生成文档的可读性优先.

每个文档注释的第一句话成了该注释所属元素的概要描述(summary description).
为了避免混乱, 在类或者接口中不应该有两个成员或者构造方法有相同的概要描述. 尤其要注意方法重载.

**对于方法和构造器而言, 概要描述应该是个完整的动词短语, 它描述了该方法所执行的动作.** 
对于类, 接口和域, 概要描述应该是一个名词短语.

Java 9引入了index, 方面文档查询. 偶尔你需要用`{@index}`加入额外的index.

泛型, 枚举, 注解都需要额外的注意: 

* 当为泛型方法写文档时, 需要为每个泛型参数写文档注释.
* 枚举需要为每个常量写注释.
* 注解需要注释每个成员. (注解的概要描述是个动词短语.)

包级文档注释: `package-info.java`.
模块级文档注释: `module-info.java`.

在文档中还应该标明:

* 线程安全性 -> 不论是否线程安全.
* 如果可序列化, 需标明序列化形式.

Javadoc可以继承方法注释.
你可以用`{@inheritDoc}`标签来继承部分文档注释. (tricky and has some limitations).

```java

// Documentation comment examples (Pages 255-9)
public class DocExamples<E> {
    // Method comment (Page 255)
    /**
     * Returns the element at the specified position in this list.
     *
     * <p>This method is <i>not</i> guaranteed to run in constant
     * time. In some implementations it may run in time proportional
     * to the element position.
     *
     * @param  index index of element to return; must be
     *         non-negative and less than the size of this list
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= this.size()})
     */
    E get(int index) {
        return null;
    }

    // Use of @implSpec to describe self-use patterns & other visible implementation details. (Page 256)
    /**
     * Returns true if this collection is empty.
     *
     * @implSpec This implementation returns {@code this.size() == 0}.
     *
     * @return true if this collection is empty
     */
    public boolean isEmpty() {
        return false;
    }

    // Use of the @literal tag to include HTML and javadoc metacharacters in javadoc comments. (Page 256)
    /**
     * A geometric series converges if {@literal |r| < 1}.
     */
    public void fragment() {
    }

    // Controlling summary description when there is a period in the first "sentence" of doc comment. (Page 257)
    /**
     * A suspect, such as Colonel Mustard or {@literal Mrs. Peacock}.
     */
    public enum FixedSuspect {
        MISS_SCARLETT, PROFESSOR_PLUM, MRS_PEACOCK, MR_GREEN, COLONEL_MUSTARD, MRS_WHITE
    }


    // Generating a javadoc index entry in Java 9 and later releases. (Page 258)
    /**
     * This method complies with the {@index IEEE 754} standard.
     */
    public void fragment2() {
    }

    // Documenting enum constants (Page 258)
    /**
     * An instrument section of a symphony orchestra.
     */
    public enum OrchestraSection {
        /** Woodwinds, such as flute, clarinet, and oboe. */
        WOODWIND,

        /** Brass instruments, such as french horn and trumpet. */
        BRASS,

        /** Percussion instruments, such as timpani and cymbals. */
        PERCUSSION,

        /** Stringed instruments, such as violin and cello. */
        STRING;
    }

    // Documenting an annotation type (Page 259)
    /**
     * Indicates that the annotated method is a test method that
     * must throw the designated exception to pass.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ExceptionTest {
        /**
         * The exception that the annotated test method must throw
         * in order to pass. (The test is permitted to throw any
         * subtype of the type described by this class object.)
         */
        Class<? extends Throwable> value();
    }
}

```



# Chapter 9 通用程序设计

## 第57条 将局部变量的作用域最小化

要使局部变量的作用域最小化, **最有力的方法就是在第一次使用它的地方声明.**

几乎每个局部变量的声明都应该包含一个初始化表达式. (例外: try-catch).

for循环允许声明循环变量, 其作用域被限定在正好需要的范围之内. -> 优于while循环.

方法应该小而集中.

## 第58条 for-each循环优先于传统的`for`循环

for-each循环(增强型for循环)在简洁性和预防Bug方面有着传统for循环无法比拟的优势, 并且没有性能损失, 应该尽可能地使用for-each循环.

增强型for循环中的`:`读作`in`.

但是有三种情况无法使用for-each循环:

* 过滤删除.
* 转换更新. 
* 平行迭代. 多个集合的同步位移.

for-each循环可以用在任何实现了`Iterable`接口的对象上.

## 第59条 了解和使用类库

举例: 随机数的例子 -> 了解和使用类库. (Java 7不使用`Random`, 而用`ThreadLocalRandom`. 另, 还有`SplittableRandom`.)

不要重新发明轮子.

好处:

* 充分利用专家知识和前人经验.
* 时间.
* 类库的性能会不断提高.
* 类库功能会扩展.
* 使代码融入主流, 易读易维护.

关注类库更新新加入的功能. 尤其是`java.lang`, `java.util`和`java.io`包下的.
还有collections, streams, concurrent等.

## 第60条 如果需要精确的答案, 请避免使用`float`和`double`

货币计算不能用`float`或`double`, 应该用`BigDecimal`, `int`或者`long`.

`BigDecimal`没有原生类型使用起来方便, 而且会有性能影响. 优点是可以自己选择舍入模式.
`int`(9位)或`long`(18位)需要自己处理小数点移位.

```java
public class BigDecimalChange {
    public static void main(String[] args) {
        final BigDecimal TEN_CENTS = new BigDecimal(".10");

        int itemsBought = 0;
        BigDecimal funds = new BigDecimal("1.00");
        for (BigDecimal price = TEN_CENTS;
             funds.compareTo(price) >= 0;
             price = price.add(TEN_CENTS)) {
            funds = funds.subtract(price);
            itemsBought++;
        }
        System.out.println(itemsBought + " items bought.");
        System.out.println("Money left over: $" + funds);
    }
}

```

```java
public class IntChange {
    public static void main(String[] args) {
        int itemsBought = 0;
        int funds = 100;
        for (int price = 10; funds >= price; price += 10) {
            funds -= price;
            itemsBought++;
        }
        System.out.println(itemsBought + " items bought.");
        System.out.println("Cash left over: " + funds + " cents");
    }
}

```



## 第61条 基本类型优先于装箱基本类型

基本类型和装箱基本类型的三个主要区别:

* 基本类型只有值, 而装箱基本类型则具有与它们的值不同的同一性.
* 基本类型只有功能完备的值, 而装箱基本类型还有非功能值null.
* 基本类型通常比装箱基本类型更节省时间和空间.

有问题的情形:

* 对装箱基本类型运用`==`操作符进行比较, 几乎总是错误的.
* 当一项操作中混合装箱基本类型和基本类型时, 会自动拆箱, 如果null被自动拆箱会抛出`NullPointerException`.
* 变量被反复自动装箱和拆箱, 会有性能问题.

装箱基本类型的合理用处:

* 作为集合中的元素, 键和值.
* 在参数化类型中必须使用装箱基本类型.
* 在进行反射的方法调用时必须使用装箱基本类型.


## 第62条 如果其他类型更适合, 则尽量避免使用字符串

字符串不适合代替其他的值类型. -> `int`, `float`, `BigInteger`, `boolean`等.

字符串不适合代替枚举类型, 聚集类型, 也不适合代替能力表(capabilities).

总而言之, 如果可以使用更加合适的数据类型, 或者可以编写更加适当的数据类型, 就应该避免用字符串来表示对象. 
若使用不当, 字符串会比其他的类型更加笨拙, 更不灵活, 速度更慢, 也更容易出错.

## 第63条 当心字符串连接的性能

为连接n个字符串而重复地使用字符串连接操作符(`+`), 需要n的平方级的时间. 
这是由于字符串的不可变而导致的. 当两个字符串被连接在一起时, 它们的内容都需要被拷贝.

连接多个项目, 为了性能, 请使用`StringBuilder`的`append()`.

## 第64条 通过接口引用对象

如果有合适的接口类型存在, 那么对于参数, 返回值, 变量和域来说, 就都应该使用接口类型进行声明.

这样做程序将会更加灵活 -> 当你决定更换实现的时候, 值需要改变调用构造器的那句.

如果没有适当的接口, 则使用类层次结构中提供了必要功能的最基础的类.

## 第65条 接口优先于反射机制

反射机制提供了"通过程序来访问关于已装载的类的信息"的能力.

这种能力的代价:

* 丧失了编译时类型检查的好处.
* 执行反射访问所需要的代码非常笨拙和冗长.
* 性能损失.

也有一些情形, 通过以非常有限的形式利用, 你可以获得反射的好处, 而不被它的cost影响:
如果你编写的程序必须要与编译时未知的类一起工作, 如有可能, 就应该仅仅使用反射机制来实例化对象, 而访问对象时则使用编译时已知的某个接口或者超类.

```java
// Reflective instantiaion demo (Page 283)
public class ReflectiveInstantiation {
    // Reflective instantiation with interface access
    public static void main(String[] args) {
        // Translate the class name into a Class object
        Class<? extends Set<String>> cl = null;
        try {
            cl = (Class<? extends Set<String>>)  // Unchecked cast!
                    Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            fatalError("Class not found.");
        }

        // Get the constructor
        Constructor<? extends Set<String>> cons = null;
        try {
            cons = cl.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fatalError("No parameterless constructor");
        }

        // Instantiate the set
        Set<String> s = null;
        try {
            s = cons.newInstance();
        } catch (IllegalAccessException e) {
            fatalError("Constructor not accessible");
        } catch (InstantiationException e) {
            fatalError("Class not instantiable.");
        } catch (InvocationTargetException e) {
            fatalError("Constructor threw " + e.getCause());
        } catch (ClassCastException e) {
            fatalError("Class doesn't implement Set");
        }

        // Exercise the set
        s.addAll(Arrays.asList(args).subList(1, args.length));
        System.out.println(s);
    }

    private static void fatalError(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}

```



## 第66条 谨慎地使用本地方法

Java Native Interface (JNI)允许Java应用程序可以调用本地方法(native method), 即本地程序设计语言(C或者C++)来编写的特殊方法.

使用本地方法来提高性能的做法不值得提倡, 因为JVM实现变得越来越快了.

使用本地方法有一些缺点: 不安全; 与平台相关, 不可自由移植; 更难调试; 进入和退出本地代码时需要相关的固定开销; 需要胶合代码的本地方法编写起来单调乏味, 且难以阅读.

## 第67条 谨慎地进行优化

不要因为性能而牺牲合理的结构.

努力避免那些限制性能的设计决策.

要考虑API设计决策的性能后果.
为了性能而包装API -> bad idea.

在每次试图做优化之前和之后, 要对性能进行测量.

总而言之, 不要费力去编写快速的程序, 应该努力编写好的程序, 速度自然会随之而来.

在设计系统的时候, 特别是在设计API, 线路层协议和永久数据格式的时候, 一定要考虑性能的因素. 

当构建完成之后, 要测量它的性能. 如果不够快, 可以在性能剖析器的帮助下, 找到问题的根源, 然后设法优化系统中相关的部分. 
第一个步骤是检查所选择的算法: 再多的底层优化也无法弥补算法的选择不当. 
必要时重复这个过程, 在每次改变之后都要测量性能, 直到满意为止.

## 第68条 遵守普遍接受的命名惯例

Java平台建立了一整套很好的命名惯例(naming convention).

* 包/模块名: 层次状, 小写字母或数字(很少使用数字), `.`分隔.
* 类, 接口: 一个或多个单词, 首字母大写.
* 方法和域, 局部变量: 首字母小写.
* 常量域: 一个或多个大写的单词, 下划线分隔.
* 类型参数: 单个字母: T表示任意的类型, E表示集合元素类型, K和V表示映射的键和值, X表示异常. 任何类型的序列可以是T, U, V或者T1, T2, T3.

一些语法惯例:

* 可实例化的类通常用单数名词, 不可实例化的辅助类通常用复数名词, 如`Collections`.
* 方法名通常是动词或动词短语.
* 返回布尔值的方法通常以`is`或`has`开头.
* 方法返回非布尔值时, 有时用名词命名, 如`size`, 有时加`get`.
* 转换类型的方法通常用`toType`.
* 返回不同视图的方法用`asType`.
* 还有`typeValue`如intValue和静态工厂方法from,of,valueOf、instance等.

# Chapter 10 异常

## 第69条 只针对异常的情况才使用异常

异常应该只用于异常的情况下, 永远不应该用于正常的控制流. 
缺点: 代码难看, 性能降低, 隐藏真正的错误, 有bug, 难以维护.

良好设计的API不应该强迫它的客户端为了正常的控制流而使用异常.

如果类具有状态相关(state-dependent)的方法, 往往也应该有个状态测试(state-testing)方法.
举例: `Iterator`接口的`next()`方法状态相关, 相应的测试方法是`hasNext()`.
状态测试方法不适用的情形: 并发未同步可能会产生状态不一致; 检车工作重复执行了动作操作会有性能问题.

另一种状态测试的做法: 让状态相关的方法返回一个可识别的值, 比如null或者是空的optional的值.
(对`next()`不适用, 因为null是`next()`方法的合法返回值.)

## 第70条 对可恢复的情况使用受检异常, 对编程错误使用运行时异常

Java提供三种可抛出结构(throwable):

* 受检异常(checked exception).
* 运行时异常(run-time exception).
* 错误(error).

决定使用受检异常或是非受检异常时, 主要的原则: 如果期望调用者能够适当地恢复, 对于这种情况就应该使用受检的异常.

通过抛出受检的异常, 强迫调用者在一个catch子句中处理该异常, 或者将它传播出去. 
每个受检异常都是对API用户的一个潜在指示: 与异常相关联的条件是这个方法的一种可能的结果.

用运行时异常(runtime exception)来表明编程错误. 大多数运行时异常都表示前提违例, 例如数组越界访问.

如果无法决定到底应该用受检(checked exception)还是非受检异常(runtime exception), (无法确定是否可能恢复), 就用runtime exception.

虽然Java语言规范没有要求, 但是按照惯例, 错误(error)往往被JVM保留用于表示资源不足, 约束失败或者其他使程序无法继续执行的条件.
因此, 基于这个惯例, 最好不要实现Error的新的子类, 你实现的所有未受检的抛出结构都应该是`RuntimeException`的子类(直接或间接).

不要定义任何既不是checked exception又不是runtime exception的异常.

## 第71条 避免不必要地使用受检的异常

受检的异常强迫程序员处理异常的情况, 大大增强了可靠性.

但是过分使用受检的异常会使API使用起来非常不方便.
如果方法抛出一个或多个受检的异常, 调用该方法的代码就必须在一个或多个catch块中处理这些异常, 或者它必须声明抛出这些异常.
这两种方式都会对API的使用者造成负担. Java 8开始, 这种负担加重, 因为抛出受检异常的方法不能直接在流中使用.

如果正确地使用API并不能阻止这种异常条件的产生, 并且一旦产生异常, 使用API的程序员可以立即采取有用的动作, 这种负担就被认为是正当的. 
-> 这两个条件都满足受检异常才是正当的.

消除受检异常最简单的方法就是返回一个空的optional(异常情况下的缺省值). 这种方法的缺点就是不能提供更多的信息.

"把受检的异常变成未受检的异常"的一种方法是, 把这个抛出异常的方法分成两个方法, 其中第一个方法返回一个boolean, 表明是否该抛出异常. -> 状态测试方法.

## 第72条 优先使用标准的异常

使用标准异常的好处: API好用; 可读性; 更少的异常类节省了类加载的时间和空间.

常用的异常:

* `IllegalArgumentException`
* `IllegalStateException`
* `NullPointerException`
* `IndexOutOfBoundsException`
* `ConcurrentModificationException`
* `UnsupportedOperationException`

不要直接使用`Exception`, `RuntimeException`, `Throwable`, `Error`, 把这些类看作抽象的.

如果一个异常符合你的需求就使用它, 抛出这个异常时的条件要和异常文档中标注的一致, 而不仅仅是名字.

如果想要继承一个标准异常, 增加更多细节, feel free.

异常都是可序列化的, 没有什么好的理由不要写自己的异常.

一些情况下的异常选择: 如果没有任何参数值可以work, 抛`IllegalStateException`, 否则抛`IllegalArgumentException`.

## 第73条 抛出与抽象相对应的异常

异常转译(exception translation): 更高层的实现应该捕获底层的异常, 同时抛出可以按照高层抽象进行解释的异常.

异常链(exception chaining): 如果低层的异常对于调试导致高层异常的问题非常有帮助, 使用异常链将低层的异常(原因cause)传到高层异常.

大多数标准的异常都有支持链的构造器, 对于没有的, 可以用`Throwable.initCause()`设置原因.
异常链不仅方法提供访问cause, 还集成了cause的stack trace到高层异常中.

虽然异常转换优于无脑的异常传播, 但是也不应该被过度使用.

* 可能的情形下, 最好的方法是能避免底层的异常, 确保底层方法成功. (有时候可以通过检查高层传入底层的参数实现.)
* 如果底层异常的确不可避免, 让高层默默解决这些异常, 从而使高级别方法的调用者与低层问题隔离. 加log供之后研究.

## 第74条 每个方法抛出的异常都要有文档

始终要单独地声明受检的异常, 并且利用Javadoc的`@throws`标记, 准确地记录下抛出每个异常的条件.

虽然Java并不要求方法声明它可能会抛出的未受检异常, 但是仔细地为未受检异常建立文档是非常明智的, 因为它们有效描述了方法的前提条件.

使用Javadoc的`@throws`标签来标记方法抛出的每个异常, 但是不要对非受检异常使用`throws`关键字.
这样对于方法的使用者来说可以很好地区分两类异常: 在文档中有`@throws`, 在方法声明中没有throws子句的就是非受检异常.

但是要标记所有的非受检异常只是一种理想情况, 现实生活中很难达到.

如果一个异常被一个类中的很多方法基于同样的理由抛出, 可以在类的文档注释中说明这个异常.

## 第75条 在细节消息中包含能捕获失败的信息

程序由于未被捕获的异常失败的时候, 会打印该异常的堆栈轨迹, 包含该异常的`toString()`结果: 通常包含类名和细节消息(detail message).

异常的细节信息应该包含对该异常有贡献的参数和域的值.
但是要注意不要包含敏感信息, 如密码, 加密秘钥等.

为了确保在异常的细节消息中包含足够的信息, 一种办法是在异常的构造器中引入这些信息, 然后只要把它们放到消息描述中, 就可以自动产生细节信息.

## 第76条 努力使失败保持原子性

失败原子性(failure atomic): 失败的方法调用应该使对象保持在被调用之前的状态.

实现这种效果的途径:

* 设计一个不可变的对象.
* 在执行操作之前检查参数的有效性, 在对象的状态被修改之前抛出适当的异常. -> 让可能会失败的计算部分都在对象状态被修改之前发生.
* 在对象的一份临时拷贝上执行操作, 当操作完成后再用临时拷贝中的结果代替对象的内容.
* 编写一段恢复代码, 使对象回滚.

## 第77条 不要忽略异常

空的`catch`块会使异常达不到应有的目的. -> 至少应该有个说明吧.

如果你选择忽视一个异常, `catch`块应该包含一个注释, 解释为什么这么做是合理的, 而且`catch`括号中的异常变量应该被命名为`ignored`.

# Chapter 11 并发

## 第78条 同步访问共享的可变数据

关键字`synchronized`可以保证同一时刻只有一个线程可以执行某一个方法或者某一个代码块.

如果把同步的概念仅仅理解为一种互斥的方式, 虽然正确, 但并没有说明同步的全部意义.

如果没有同步, 一个线程的变化就不能被其他线程看到. 
同步不仅可以阻止一个线程看到对象处于不一致的状态中, 它还可以保证进入同步方法或者同步代码块的每个线程, 都看到由同一个锁保护的之前所有的修改效果.

虽然语言规范保证了线程在读写数据的时候, 不会看到任意的数值(读写变量是原子的). (例外: `long`和`double`.)
但是它并不保证一个线程写入的值对于另一个线程将是可见的. 
为了在线程之间进行可靠的通信, 也为了互斥访问, 同步是必要的. -> 归因于内存模型, 规定线程所做的变化何时以及如何对其他线程可见.

如果读和写操作没有都被同步, 同步就不会起作用.

`volatile`修饰符不执行互斥访问, 但它可以保证任何一个线程在读取该域的时候都将看到最近刚刚被写入的值. -> 用在只需要通信而不需要互斥的场合.

增量操作符`++`不是原子的: 先读, 后写.
多个线程可能会看到同一个值.

如果没有同步共享的可变数据, 可能会引起liveness和safety failures.
避免本条目中所讨论到的问题的最佳办法是: 不共享可变的数据. -> 要么不可变, 要么不共享. -> 将可变数据限制在单个线程中.

让一个线程在短时间内修改一个数据对象, 然后与其他线程共享, 这是可以接受的, 只同步共享对象引用的动作. 
这种对象称为**事实上不可变的(effectively immutable)**. 将这种对象引用从一个线程传递到其他线程被称作**安全发布(safe publication)**.

## 第79条 避免过度同步

不要在同步代码块中调用外来方法, 可能会造成并发修改异常或者死锁.

将外来的方法调用移出同步的代码块:

* 拷贝数据结构.
* 使用concurrent collection, `CopyOnWriteArrayList`.

通常, 应该在同步区域内做尽可能少的工作.

## 第80条 executor, task和streams优先于线程

Java 1.5中加入了`java.util.concurrent`, 很好用:

```
ExecutorService executorService = Executors.newSingleThreadExecutor();

// 提交runnable
executorService.execute(new Runnable() {
    @Override
    public void run() {

    }
});

// 终止
executorService.shutdown();
```

很多有用的方法, 比如等待优雅地完成终止:

```
executorService.awaitTermination(30, TimeUnit.SECONDS);
```

还可以创建固定或可变数目的线程池.

```
Executors.newFixedThreadPool(3);
```

小程序或者轻载的服务器, `Executors.newCachedThreadPool()`是个不错的选择, 但是对于大负载的服务器来说, 最好使用固定数目的线程池.

还可以直接使用`ThreadPoolExecutor`类, 控制线程池操作.

`ScheduledThreadPoolExecutor`可以代替`java.util.Timer`, 优势: 时间更准确, 可以从异常恢复.


### Fork Join

Java 7开始, Executor Framework支持fork-join tasks, 被一种特殊的executor service(fork-join pool)来运行.
`ForkJoinTask`的实例, 可以被拆分成子任务, `ForkJoinPool`中的线程不仅负责处理任务, 还会互相偷取任务, 来确保每个线程都忙碌, 提高了CPU的利用率.

并行的streams就是在fork join pools之上写的, 允许你很容易就能利用其性能提升.

## 第81条 并发工具优先于`wait`和`notify`

concurrent中工具分为三类:

* Executor Framework.
* 并发集合. -> 为标准集合接口提供了高性能的并发实现.
* 同步器(synchronizers).

### 并发集合

应该优先使用并发集合, 而不是使用外部同步的集合.
例: `ConcurrentHashMap`优于`Collections.synchronizedMap`.

大多数`ExecutorService`实现都使用了`BlockingQueue`.

### 同步器

同步器: 常用: `CountDownLatch`, `Semaphore`; 不太常用: `CyclicBarrier`, `Exchanger`; 最强大: `Phaser`.

线程饥饿死锁(thread starvation deadlock).

`System.nanoTime()`比`System.currentTimeMillis()`更加准确和精确.

### `wait`和`notify`

没有理由在新代码中使用`wait`和`notify`, 即使有也是极少的. 
如果你在维护使用`wait`和`notify`的代码, 务必确保始终是利用标准的模式从while循环内部调用`wait`.

使用`wait`的标准模式:

```
// The standard idiom for using the wait method
synchronized (obj) {
while (<condition does not hold>) 
obj.wait();  // (Releases lock, and reacquires on wakeup) 

... // Perform action appropriate to condition
}
```

一般情况下, 优先使用`notifyAll`而不是`notify`, 如果使用`notify`请一定要小心, 以确保程序的活性.

## 第82条 线程安全性的文档化

方法声明中的`synchronized`关键字是一个实现细节, 并不是API的一部分. 它并不能可靠地说明这个方法是线程安全的.

为了安全的并发使用, 一个类必须在文档中说明它支持的线程安全级别.

线程安全性的常见级别:

* 不可变的(immutable). -> `String`, `Long`, `BigInteger`.
* 无条件的线程安全(unconditionally thread-safe). -> 这个类的实例是可变的, 但是这个类有着足够的内部同步. 所以它的实例可以被并发使用, 无需任何外部同步. -> `Random`, `ConcurrentHashMap`.
* 有条件的线程安全(conditionally thread-safe). -> 有些方法需要外部同步. -> `Collections.synchronized`包装返回的集合.
* 非线程安全(not thread-safe). -> 并发使用需要客户自己在外部处理同步. -> 通用的集合比如`ArrayList`, `HashMap`.
* 线程对立的(thread-hostile). -> 这个类不能安全地被多个线程并发使用, 即使所有的方法调用都被外部同步包围.

每个类都应该在文档中说明它的线程安全属性. 
有条件的线程安全必须在文档中指明"哪个方法调用序列需要外部同步, 以及在执行这些序列的时候要获得哪把锁".

无条件的线程安全类, 应该考虑使用私有锁对象来代替同步的方法 -> 防止客户端程序和子类的不同步干扰.

注意: Lock字段应该永远被声明为final的.

## 第83条 慎用延迟初始化

延迟初始化(lazy initialization): 需要域的值时才将它初始化.

延迟初始化降低了初始化类或者创建实例的开销, 却增加了访问被延迟初始化的域的开销.

当有多个线程共享一个延迟初始化的域, 采用某种形式的同步是很重要的.

大多数的域应该正常地进行初始化, 而不是延迟初始化. 
如果为了达到性能目标, 或者为了破坏有害的初始化循环, 而必须延迟初始化一个域, 就可以使用相应的延迟初始化方法:

* 对于实例域, 使用双重检查模式.
* 对于静态域, 使用lazy initialization holder class模式.
* 对于可以接受重复初始化的实例域, 也可以考虑使用单重检查模式.


## 第84条 不要依赖于线程调度器

线程调度器(thread scheduler)决定哪些线程将会运行, 以及运行多长时间. 编写良好的程序不应该依赖于这种策略的细节.

要编写健壮的, 响应良好的, 可移植的多线程应用程序, 最好的办法是确保可运行线程的平均数量不明显多于处理器的数量.

降低线程数量: 如果线程没有做有用的工作, 那么线程不应该run.

不要依赖`Thread.yield`或者线程优先级, 这些设施仅仅对调度器作些暗示. 

线程优先级可以用来提高一个已经能够正常工作的程序的服务质量, 但永远不应该用来"修正"一个原本不能工作的程序.

# Chapter 12 序列化

## 第85条 优先考虑非Java序列化的其他选择

Java的序列化容易被黑客利用, 引发安全问题.

deserialization bombs: 反序列化它将花费很长时间, 或永远无法完成.

避免序列化漏洞被利用的最佳方法是永远不要反序列化任何东西。

有很多机制可以替代序列化, 并且提供更多好处, 比如跨平台, 高性能, 社区支持等.
本书把这些替代方式称作cross-platform structured-data representations.
比较流行的有:

* JSON
* Protocol Buffers (protobuf).

如果你不能完全地避免Java序列化, 你可以: 

* 永远不要反序列化不受信任的数据.
* 如果对数据安全性不能完全确定, 使用Java 9的`java.io.ObjectInputFilter`在反序列化前过滤数据(优先使用白名单策略).

## 第86条 谨慎地实现`Serializable`接口

实现`Serializable`不是一个轻率的决定.

实现`Serializable`接口而付出的最大代价是, 一旦一个类被发布, 就大大降低了"改变这个类的实现"的灵活性.

如果你接受了默认的序列化形式, 这个类中私有的和包级私有的实例域将都变成导出的API的一部分, 这不符合"最低限度地访问域"的实践原则, 从而它就失去了作为信息隐藏工具的有效性.

序列化会使类的演受到限制, 这种限制的一个例子与流的唯一标识符有关, 通常它也被称为序列版本UID(serial version UID). 如果没有显式声明, 系统会自动生成.

实现`Serializable`的第二个代价是, 它增加了出现Bug和安全漏洞的可能性. -> 反序列化是一个隐藏的构造器.

实现`Serializable`的第三个代价是, 随着类发行新的版本, 相关的测试负担也增加了.

为了继承而设计的类应该尽可能少地去实现`Serializable`接口, 用户的接口也应该尽可能少地继承`Serializable`接口.

内部类不应该实现`Serializable`接口, 除非是静态内部类.

## 第87条 考虑使用自定义的序列化形式

如果没有先认真考虑默认的序列化形式是否合适, 则不要贸然接受.

如果一个对象的物理表示法等同于它的逻辑内容, 可能就适合于使用默认的序列化形式.

即使你确定了默认的序列化形式是合适的, 通常还必须提供一个`readObject`方法以保证约束关系和安全性.

当一个对象的物理表示法与它的逻辑数据内容有实质性的区别时, 使用默认序列化形式会有以下4个缺点:

* 它使这个类的导出API永远地束缚在该类的内部表示法上.
* 消耗过多的空间.
* 消耗过多的时间.
* 会引起栈溢出.

`transient`修饰符: 从序列化形式中省略掉实例域. 反序列化时这些域将被初始化为默认值.

当你决定把一个字段标记为非`transient`之前, 首先需要说服自己, 这个字段是这个对象逻辑状态的一部分.

无论你是否使用默认的序列化形式, 如果在读取整个对象状态的任何其他方法上强制任何同步, 则也必须在对象序列化上强制这种同步.

不论你选择了哪种序列化形式, 都要为自己编写的每个可序列化的类声明一个显式的序列版本UID(serial version UID).
除非你要破坏和所有已经存在的实例的兼容性, 否则就不要改序列版本UID.

## 第88条 保护性地编写`readObject`方法

`readObject`方法实际上相当于一个公有的构造器, 如同其他的构造器一样, 它也要求注意同样的所有注意事项. 
构造器必须检查其参数的有效性, 并且在必要的时候对参数进行保护性拷贝.

编写更加健壮的`readObject()`方法的指导方针:

* 对于对象引用域必须保持为私有的类, 要保护性地拷贝这些域中的每个对象. 不可变类的可变组件就属于这一类别.
* 对于任何约束条件, 如果检查失败, 则抛出一个`InvalidObjectException`异常. 这些检查动作应该跟在所有的保护性拷贝之后.
* 如果整个对象图在被反序列化之后必须进行验证, 就应该使用`ObjectInputValidation`接口.
* 无论是直接还是间接方式, 都不要调用类中任何可被覆盖的方法.


## 第89条 对于实例控制, 枚举类型优先于`readResolve`

如果单例模式的类加上了`implements Serializable`, 就多了一种创建实例的途径.

`readResolve`特性允许你用`readObject`创建的实例代替另一个实例. 
对于一个正在被反序列化的对象, 如果它的类定义了一个`readResolve`方法, 并且具备正确的声明, 那么在反序列化之后, 新建对象上的`readResolve`方法就会被调用. 
然后该方法返回的对象引用将被返回, 取代新建的对象. 在这个特性的绝大多数用法中, 指向新建对象的引用不需要再被保留, 因此立即成为垃圾回收的对象.

可以利用`readResolve`方法保证单例模式. -> 方法忽略被反序列化的对象, 只返回该类初始化时创建好的那个实例.

如果依赖`readResolve`进行实例控制, 带有对象引用类型的所有实例域都必须声明为`transient`的.

从历史上来看, `readResolve`方法被用于所有可序列化的实例受控(instance-controlled)的类. 自从Java1.5以来, 它就不再是在可序列化的类中维持实例控制的最佳方法了.

应该尽可能地使用枚举类型来实施实例控制的约束条件.
但是如果这不可能做到, 或者你需要一个实现了序列化的实例受控的类, 那么你就必须提供一个`readResolve`方法, 然后确保所有的字段都是primitive或transient的.

## 第90条 考虑用序列化代理代替序列化实例

序列化代理模式(serialization proxy pattern): 

* 为可序列化的类设计一个私有的静态嵌套类(序列化代理), 它应该有一个单独的构造器, 其参数类型就是那个外围类. 
* 在外围类中添加writeReplace方法. -> 产生代理类实例.
* 外围类中添加readObject方法. -> 防止伪造.
* 代理类中提供readResolve方法, 返回一个逻辑上相当的外围类的实例. -> 序列化代理转变回外围类的实例.

序列化代理模式的局限性:

* 不能与可以被客户端扩展的类兼容.
* 不能与对象图中包含循环的某些类兼容.
* 序列化代理模式的功能和安全性有性能开销的代价.

总而言之, 每当你发现自己必须在一个不能被客户端扩展的类上编写`readObject`或者`writeObject`方法的时候, 就应该考虑使用序列化代理模式.