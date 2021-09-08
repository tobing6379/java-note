# Spring

Spring学习笔记，仓库地址：https://gitee.com/tobing/spring-learn.git

## 一、组件注册

![image-20210531195341655](https://gitee.com/tobing/imagebed/raw/master/image-20210531195341655.png)

从上面可以看出，Spring Framework的核心依赖于Core Container，本章重点关注如何将组件注册IOC容器中。

将组件注册到容器中主要有以下四种方式（注解方式）：

+ ComponentScan+组件注解（包含@Controller、@Service、@Repository、@Component）：适用于自定义组件
+ @Bean方式：适用于第三方组件
+ @Import方式：适用于第三方组件
  + @Import(组件)：容器自动注册其组件，其创建的bean的id默认是全类名
  + ImportSelector：
  + ImportBeanDefinitionRegistrar：
+ 使用Spring提供FactoryBean：适用于第三方组件，常用以第三方框架的集成

#### ComponentScan

自定义的组件往往通过ComponentScan方式注册到IOC容器中，下面介绍使用方法：

```java
@Configuration
@ComponentScan(value = "top.tobing")
public class MainConfig {
}

@Controller
public class UserController {
}

@Service
public class UserService {
}

@Service
public class UserService {
}

@Data
public class User {
    private String username;
    private Integer age;
}
```

Test

```java
@Test
public void testComponentScanType() {
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MainConfig.class);
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    for (String beanName : beanNames) {
        System.out.println(beanName);
    }
}
```

输出

```bash
...
mainConfig
userController
userDao
userService
user
```

上面演示了ComponentScan+自定义组件的常规用法，也是我们日常开发中最常见的方式。但是ComponentScan的功能远不止这些，下面介绍ComponentScan进阶使用。

#### ComponentScan进阶

**1、先分析一下ComponentScan的属性**

```java
@Repeatable(ComponentScans.class) // 表明ComponentScan注解可以重复标注使用
public @interface ComponentScan {
    // 等于value，用来指示需要将那个包以及子包中的组件添加到IOC容器中
	@AliasFor("basePackages")
	String[] value() default {};
	@AliasFor("value")
	String[] basePackages() default {};
    // 是否使用默认的Filter，即basePackages包下的所有组件
	boolean useDefaultFilters() default true;
	// 符合Filter将会被包含到IOC容器
    Filter[] includeFilters() default {};
    // 符合Filter将会被排除在IOC容器之外
	Filter[] excludeFilters() default {};
    // 是否采用懒加载的方式初始化
	boolean lazyInit() default false;
}
```

从ComponentScan的定义可以发现，除了平常使用的value，还有一些我们日常没使用到的，如：includeFilters/excludeFilters等

**2、使用Filter实现定制化的Bean创建**

通过配置ComponentScan注解的includeFilters和excludeFilters属性可以分别实现包含和排除自定的组件到IOC容器。

```java
@ComponentScan(value = "top.tobing", excludeFilters = {
         // 根据注解类型排除：排除标记了Controller注解的Bean，标注了Controller的注解将会被排除
         @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class),
         // 根据分配类型排除：排除UserDao的所有Bean，UserDao类将会被排除
         @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UserDao.class),
         // 根据自定义规则排除：排除bean名称包含er的Bean，自定义过滤规则
         @ComponentScan.Filter(type = FilterType.CUSTOM, value = AppFilter.class)
})
```

AppFilter.java

```java
public class AppFilter implements TypeFilter {
    /**
     * 所有ComponentScan扫描的类都会执行match进行匹配。
     * 返回true表示匹配通过，即通过Filter
     * 返回false表示匹配失败，即不通过Filter
     *
     * @param metadataReader        可以简单获取到类的元数据信息
     * @param metadataReaderFactory 允许为每个原始资源缓存一个 MetadataReader
     */
    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        // 获取类的元数据信息
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        // 获取正在扫描的类注解信息
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        // 获取当前的类路径信息
        Resource resource = metadataReader.getResource();
        System.out.println("----> " + resource);
        // 允许类名包含er的Bean通过Filter
        if (classMetadata.getClassName().contains("entity") || classMetadata.getClassName().contains("filter")) {
            return true;
        }
        return false;
    }
}
```

#### Bean

在使用第三方组件时，我们无法在其类标注类似Component的组件，此时我们往往采用@Bean的方式将其注入到IOC容器中。

```java
@Configuration
public class PlatformConfig {
    // 将分布式Id生成器组件添加到IOC容器，以便其他组件使用
    @Scope("singleton")
    @Lazy
    @Bean
    public IdWorker idWorker() {
        return idWorker();
    }
}
```

+ @Scope：指定Bean的作用范围，主要有一下四种：
  + singleton：单例，容器只会创建一个对象
  + prototype：原型，每次获取都会创建新的对象
  + request：同一个请求创建一个实例
  + session：同一个session创建一个实例
+ @Lazy：指定Bean创建时间，标注表示采用懒加载的方式，即使用才创建。

#### Conditional

和ComponentScan类似，Bean也可以通过Condition来限制Bean的是否注入到IOC容器中。

```java
@Configuration
public class MainConfig2 {
    @Conditional(value = LinuxCondition.class)
    @Bean("tobing")
    public User tobing() {
        System.out.println("init.....");
        return new User("tobing", 21);
    }
    @Conditional(value = WindowsCondition.class)
    @Bean("zenyet")
    public User zenyet() {
        System.out.println("init.....");
        return new User("zenyet", 22);
    }
}

// 根据运行的变量，确定是否将Bean加入IOC中。此处表示在Linux平台下降tobing的bean添加到容器中
public class LinuxCondition implements Condition {
    // @param context 接口条件上下文对象，可以获取丰富的信息
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取Bean工厂
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        // 获取运行时环境
        Environment environment = context.getEnvironment();
        // 如果当前运行环境为Linux，将tobing添加进来
        String os = environment.getProperty("os.name");
        if (os.contains("Linux")) {
            return true;
        }
        return false;
    }
}

// 类似上面，不赘述
public class WindowsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        Environment environment = context.getEnvironment();
        String os = environment.getProperty("os.name");
        if (os.contains("Windows")) {
            return true;
        }
        return false;
    }
}
```

除了修饰方法，Conditional还可以修饰类。

#### Import

Import方式也可以将第三方的组件添加到IOC容器中。

```java
@Configuration
@Import({Red.class, Green.class, Blue.class})
public class MainConfig3 {
}
```

上面代码中把Red、Green、Blue添加到了容器中。

除此之外，Import还提供了ImportSelector、ImportBeanDefinitionRegistrar两种方式实现定制化的注入，首先看Selector。

```java
public class MySelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // ... 自定义逻辑
        return new String[]{"top.tobing.entity.JSONParser"};
        // 返回需要注入容器中的Bean对应的全限定类名
    }
}
```

接下来看ImportBeanDefinitionRegistrar方式

```java
public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * Import
     *
     * @param importingClassMetadata Import注解标志类的注解元信息
     * @param registry               持有Bean的定义，可以用来注册一个Bean
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        boolean isGreen = registry.containsBeanDefinition("top.tobing.entity.Green");
        boolean isRed = registry.containsBeanDefinition("top.tobing.entity.Red");
        boolean isBlue = registry.containsBeanDefinition("top.tobing.entity.Blue");
        // 如果容器中存在上述三个Bean将Rainbow注册容器中，并设置其beanName以及Scope等信息
        if (isBlue && isGreen && isRed) {
            RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(Rainbow.class);
            // 设置Scope
            rootBeanDefinition.setScope("prototype");
            // 设置懒加载
            rootBeanDefinition.setLazyInit(true);
            registry.registerBeanDefinition("tobing's Rainbow", rootBeanDefinition);
        }
    }
}
```

具体看注释吧。

#### FactoryBean

FactoryBean大量使用再第三方框架整合到Spring中，直接看代码

```java
public class ColorFactoryBean implements FactoryBean<Color> {
    @Override
    public Color getObject() throws Exception {
        System.out.println("Init Color...");
        return new Color();
    }

    @Override
    public Class<?> getObjectType() {
        return Color.class;
    }

    /**
     * 是否单例
     * false:prototype
     * true:singleton
     */
    @Override
    public boolean isSingleton() {
        return false;
    }
}
```

将ColorFactoryBean通过Bean注入到容器

```java
@Configuration
public class MainConfig3 {
    @Bean
    public ColorFactoryBean colorFactoryBean() {
        return new ColorFactoryBean();
    }
}
```

## 二、生命周期

备注：这里理解不够，因此会感觉很乱。

#### 概述

上面通过不同的方式，可以将自定义的组件或第三方组件添加到IOC容器，接下来我们关注这些组件在IOC容器的生命周期。

Bean的生命周期主要有一下三段：**创建===>初始化==>销毁**

容器会帮我们管理Bean的生命周期，我们可以自定义初始化和销毁方法，这样容器就会在Bean推进到特定生命周期的时候调用我们指定的方法。

#### 对象创建

对于不同作用域（Scope）的对象，其创建的时机并不一样：

+ 对于单实例，在容器创建的时候创建对象
+ 对于多实例，在每次获取的时候创建对象

#### 对象初始化

在对象创建好之后，要对对象进行初始化，这时会调用初始化方法。

#### 对象销毁

对于不同作用域（Scope）的对象，其销毁的时机并不一样：

+ 对于单实例：在容器关闭时销毁对象
+ 对于多实例：有用户释放，容器不管理，即不调用其销毁方法

#### 回调函数

可以为一个对象的不同生命周期指定不同的回调函数，如在初始时指定初始化方法，在销毁时指定销毁方法。

这些回调方法可以通过以下几种方式进行指定：

1. 通过@Bean注解属性指定
2. 通过实现InitializingBean和DisposableBean
3. 通过使用JSR250注解
4. 通过实现BeanPostProcessor

#### @Bean注解属性

@Bean注解有两个属性可以指定初始化回调函数以及销毁回调函数。

```java
@Configuration
public class MainConfig4 {
    @Bean(initMethod = "initMethod", destroyMethod = "destroyMethod")
    public Car car() {
        return new Car();
    }
}
```

+ initMethod属性指定了初始化回调函数为initMethod
+ destroyMethod属性指定了销毁回调函数为destroyMethod

```java
public class Car {
    public Car() {
        System.out.println("Car construct create...");
    }

    public void initMethod() {
        System.out.println("Car initMethod execute...");
    }

    public void destroyMethod() {
        System.out.println("Car destroyMethod execute...");
    }
}
```

#### InitializingBean与DisposableBean

实现InitializingBean接口重写其afterPropertiesSet()方法，在该方法指定初始化逻辑，IOC容器在对象初始化过程中将遍历容器中所有InitializingBean实例，并调用其afterPropertiesSet方法。

实现DisposableBean接口重写其destroy()方法，在改方法指定其销毁逻辑，IOC容器在对象销毁过程中将遍历容器中所有的DisposableBean实例，并调用其afterPropertiesSet方法。

```java
public class Cat implements InitializingBean, DisposableBean {

    public Cat() {
        System.out.println("Cat construct create...");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Cat DisposableBean destroy...");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Cat InitializingBean afterPropertiesSet...");
    }
}
```



#### JSR250注解

使用@PreDestroy注解标志一个方法为销毁回调方法。 

使用@PostConstruct注解标志一个方法为销毁回调方法。

在IOC初始化Bean的过程中，会执行`org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#postProcessBeforeInitialization`方法，在该方法中会遍历所有Bean判断是否具有LifecycleMetadata（生命周期函数），如果有就会调用执行对应的方法。

![image-20210601163110838](https://gitee.com/tobing/imagebed/raw/master/image-20210601163110838.png)

#### BeanPostProcessor

这是一个杀器，在Spring源码中大量使用了各种BeanPostProcessor，如：

+ 帮助我们实现@Autowired的AutowiredAnnotationBeanPostProcessor
+ 帮助我们实现@Valid参数校验的BeanValidationPostProcessor
+ 帮助我们实现@Async异步任务的AsyncAnnotationBeanPostProcessor

## 三、属性赋值

#### 概述

项目中常常需要使用到第三方的服务，如需要集成Tencent的短信SDK。在使用这些第三方SDK的时候，往往需要配置一些参数，如secret-key、access-key等。此时我们往往不是直接在类的内部硬编码赋值，而是采用属性赋值的方式，将这些值从配置文件等注入到属性中，这样如果需要改变参数就无需重新编译，而只需修改配置文件即可。

Spring中提供了@Value注解用于属性赋值，可以有以下几种方式：

+ 在注解内部编写基本值
+ 在注解内部编写SpEL表达式
+ 使用${}取出配置文件中的值

#### 注解内部编写基本值

这种比较直观，直接看代码

```java
public class User {
    @Value("tobing")
    private String username;
    @Value("12")
    private Integer age;
}
```

#### 注解内部编写SpEl表达式

使用SpEL时，需要使用#{}包含表达式

```java
public class User {
    @Value("tobing")
    private String username;
    @Value("#{20-12}")
    private Integer age;
}
```

需要注意，age的值是SpEL运算的值，为8

#### 注解内部获取配置文件值

使用配置文件的方式时，需要在Spring中为配置文件制定位置。

**1、指定配置文件user.properties**

```java
@Configuration
@PropertySource(value = "classpath:/user.properties")
public class MainConfig5 {
    @Bean
    public User user() {
        return new User();
    }
}
```

user.properties位于resource文件夹下，内容如下：

```properties
user.username=proper_tobing
user.age=21
```

**2、在属性中注入配置文件中的值**

```java
public class User {
    @Value("${user.username}")
    private String username;
    @Value("${user.age}")
    private Integer age;
}
```

创建的对象如下：

```bash
User(username=proper_tobing, age=21)
```

**补充：properties配置的值也可以通过Environment获取**

```java
@Test
public void testValueInject() {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MainConfig5.class);
    ConfigurableEnvironment environment = applicationContext.getEnvironment();
    String age = environment.getProperty("user.age");
    String username = environment.getProperty("user.username");
    System.out.println(age);
    System.out.println(username);
}
```

因此，${}本质上是获取环境变量（ConfigurableEnvironment）中的值，而IOC容器初始化时会把指定配置文件中的key/vlaue放到环境变量中。

## 四、自动装配

#### 概述

在Spring中，在属性中使用类似于@Autowired的注解，Spring创建这个bean时会自动从IOC容器中根据类型属性搜索，如果找到指定bean，会为我们自动装配。

在使用@Autowired时，需要注意一下几点：

1. 默认情况下先按照类型去容器找对应的组件。
2. 如果找到多个类型相同的，在将属性名称作为组件的id去容器中查找。
3. 可以通过@Qualifer("id")，指定装配特定id的组件。
4. 如果容器中没有指定的组件，启动时会报错。

除此之外还需要注意，以下几点：

+ @Autowired中可以指定属性required，required=false表示如果容器中没有则不自动装配。
+ @Primary注解可以在存在多个同类型属性是，优先使用标注的bean，但是在使用Qualifier指定id时不会生效。
+ 除了@Autowired可以自动装配，@Resource直接与@Inject也可实现自动装配

#### required属性

```java
@Service
public class UserService {
    // false表示如果容器中存在类型为UserDao的bean就自动装配，如果不存在则不装配
    @Autowired(required = false)
    private UserDao userDao;
}
```

#### Primary注解

按类型注入是，如果IOC容器中存在两个或以上的类型相同的Bean会优先标注@Primary的Bean

```java
// 第一处注入
@Repository
public class UserDao {
    private String version = "1";
    ...
}

// 第二处注入
@Configuration
@ComponentScan({"top.tobing.controller", "top.tobing.service", "top.tobing.dao"})
public class MainConfig6 {
    @Primary
    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setVersion("2");
        return userDao;
    }
}
```

如果采用按照类型注入，由于第二处标注了@Primary会优先使用第二处的Bean。

#### 小总结

+ 使用@Autowired自动装配是分为**按类型注入**和**按id注入**；

+ 默认采用按类型注入，这也是平常最常见的方式；
+ 使用按类型注入的时候，如果IOC容器中存在多个类型相同的Bean会按照以下方式装配：
  + 有Primary标注的Bean则直接使用这个Bean；
  + 如果Bean的id和@Autowired标注的属性id相同则直接使用该Bean；
+ 如果按照id注入，直接根据id查找，如果存在相同id，报错。

#### Resource、Inject与Autowired

@Resource、@Inject与@Autowired都可以用于实现自动装配，但是这三者又有一些区别，如下：

+ @Resource，JSR250规范定义，默认按照组名装配，不支持Primary（4.X）且不支持require
+ @Inject，JSR330规范定义，需要导入javax.inject包，无require
+ @Autowired，Spring定义，功能灵活

#### 灵活使用Autowired

除了将@Autowired标注在属性上使用，还可以将其标注：构造器、方法、参数等：

+ 标注在方法上，@Bean+参数
+ 标注在构造器上，如果组件一个有参构造器，有参构造器的@Autowired可以省略
+ 标注在参数上

总上，我们只需要知道@Autowired使用起来很灵活。

#### 底层组件自动装配

上面都是将容器中第三方引入或我们自定义的组件自动注入，如果需要使用Spring容器底层的组件，如ApplicationContext、BeanFactory实现自定装配，需要怎么办呢？

Spring也为我们考虑到这一点，提供了大量的Aware子接口，通过实现这些子接口我们可以拿到这些需要的底层组件。

![image-20210601183737718](https://gitee.com/tobing/imagebed/raw/master/image-20210601183737718.png)

这些Aware的都是通过XXXAwareProcessor完成装配。

**1、获取环境变量**

```java
public class MyEnv implements EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void testEnv() {
        String property = this.environment.getProperty("os.name");
        System.out.println(property);
    }
}
```

**2、获取Value解析器**

```java
public class MyConfig implements EmbeddedValueResolverAware {

    private StringValueResolver stringValueResolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }

    public void resolverPath() {
        String str = this.stringValueResolver.resolveStringValue("你好${os.name}，我是${user.username}，我今年${user.age}");
        System.out.println(str);
    }
}
```

输出：

```bash
你好Windows 10，我是proper_tobing，我今年21
```

#### Profile注解

有可能在开发的时候需要使用开发环境，部署的时候使用生成环境。Spring提供了@Profile注解，方便我们来指定那些组件在那些情况下才被注册到容器中。

废话少说，直接上示例代码

```java
@PropertySource(value = "classpath:/user.properties")
@Configuration
public class MainConfig8 implements EmbeddedValueResolverAware {

    private StringValueResolver resolver;


    @Bean("productDataSource")
    public DataSource dataSourceProduct() {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        String username = resolver.resolveStringValue("${db.username}");
        String password = resolver.resolveStringValue("${db.password}");
        comboPooledDataSource.setUser(username);
        comboPooledDataSource.setPassword(password);
        comboPooledDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/shopping");
        return comboPooledDataSource;
    }

    @Profile("default")
    @Bean("devDataSource")
    public DataSource dataSourceDev() {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        String username = resolver.resolveStringValue("${db.username}");
        String password = resolver.resolveStringValue("${db.password}");
        comboPooledDataSource.setUser(username);
        comboPooledDataSource.setPassword(password);
        comboPooledDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/shopping");
        return comboPooledDataSource;
    }

    @Profile("test")
    @Bean("testDataSource")
    public DataSource dataSourceTest() {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        String username = resolver.resolveStringValue("${db.username}");
        String password = resolver.resolveStringValue("${db.password}");
        comboPooledDataSource.setUser(username);
        comboPooledDataSource.setPassword(password);
        comboPooledDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/shopping");
        return comboPooledDataSource;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }
}
```

+ 通过实现EmbeddedValueResolverAware获取StringValueResolver将其获取到配置文件中的配置信息
+ 程序中配置了三个DataSource，分别配置了不同的id，通过Profile来标志不同的使用场景
+ 如testDataSource在test环境下生效，devDataSource在default生效等等
+ 通过@Profile可以限定不同的组件在不同环境下添加到容器中，实现环境的隔离

## 五、AOP回顾

#### 概述

AOP，Aspect Oriented Programming，面向切面编程。它允许在程序运行期间动态地将某段代码切入到指定的位置进行运行。

在Spring中提供了Spring AOP来简化我们的开发，主要步骤如下：

1. 导入AOP模块：spring-aspects
2. 定义一个业务逻辑类，在业务逻辑运行的时进行日志打印（方法执行前、方法执行后、方法执行返回、方法执行出错）
3. 定义一个日志切面类，在切面类需要动态感知业务逻辑类运行到哪一步，进而调用对应的通知方法：
   + 前置通知
   + 后置通知
   + 返回通知
   + 异常通知
   + 环绕通知
4. 给切面类的目标方法标注何时何地运行
5. 将切面类和业务逻辑类添加到容器中
6. 给配置类添加@EnableAspectJAutoProxy，开启基于注解的AOP模式

#### 导入AOP模块依赖

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>4.3.25.RELEASE</version>
</dependency>
```

#### 业务类

```java
public class MathCal {
    public int div(int i, int j) {
        System.out.println("MathCal div() execute...");
        return i / j;
    }
}
```

#### 切面类

切面类中可以定义统一的切入点（需要插入代码片段的地方），可以编写各种通知方法（前置、后置、环绕、异常、返回等）并用各种注解标识。

```java
@Aspect
@Component
public class LogAspects {

    /**
     * 抽取功能切入点表达式，用于本类复用获取其他类引用
     */
    @Pointcut(value = "execution(public int top.tobing.entity.MathCal.*(..))")
    public void pointcut() {
    }


    /**
     * 前置通知，在目标方法执行前切入
     */
    @Before(value = "pointcut()")
    public void logStart(JoinPoint joinPoint) {
        System.out.println("logStart execute..." + joinPoint.getSignature().getName());
    }

    /**
     * 后置通知，在目标方法执行后执行
     */
    @After(value = "pointcut()")
    public void logEnd(JoinPoint joinPoint) {
        System.out.println("logEnd execute..." + joinPoint.getSignature().getName());
    }

    /**
     * 返回通知，在目标方法自行返回执行
     */
    @AfterReturning(value = "pointcut()", returning = "res")
    public void logReturn(JoinPoint joinPoint, Object res) {
        System.out.println("logReturn execute..." + joinPoint.getSignature().getName() + " 返回结果：" + res);
    }

    /**
     * 异常通知，在目标方法执行发生异常执行
     */
    @AfterThrowing(value = "pointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) {
        System.out.println("logException execute..." + joinPoint.getSignature().getName() + " 异常：" + exception);
    }
}
```

#### 配置类

配置类中需要注意将切面类（LogAspects）和业务类（MathCal）注入到容器中。

```java
@Configuration
@ComponentScan({"top.tobing.aop"})
@EnableAspectJAutoProxy
public class MainConfig9 {
    @Bean
    public MathCal mathCal() {
        return new MathCal();
    }
}
```

#### 测试

在测试时需要注意，要通过IOC容器获取业务注解，不能用直接new的方式。只有通过IOC容器创建的组件才会经过AOP增强。

```java
@Test
public void testAopUse() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MainConfig9.class);
    MathCal mathCal = context.getBean(MathCal.class);
    int res = mathCal.div(10, 1);
}
```



