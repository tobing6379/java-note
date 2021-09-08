package desgin_pattern.struct_type.adaptor;

/**
 * @author tobing
 * @date 2021/9/8 17:54
 * @description
 */
public class Demo05 {
    // 外部系统A
    public interface IA {
        void fa();
    }
    public class A implements IA {
        @Override
        public void fa() {}
    }
    // 项目中使用外部系统A
    public class Demo {
        private IA a;
        public Demo(IA a) {
            this.a = a;
        }
    }
    Demo d = new Demo(new A());

    class B {
        public void fb() {
        }
    }

    // 将外部系统A替换为外部系统B
    public class BAdaptor implements IA {
        private B b;
        public BAdaptor (B b) {
            this.b = b;
        }
        @Override
        public void fa() {
            b.fb();
        }
    }
    // 借助BAdaptor,Demo的代码中调用IA接口的地方无需改动
// 只需要将BAdaptor如下注入即可
    Demo d = new Demo(new BAdaptor(new B));
}
