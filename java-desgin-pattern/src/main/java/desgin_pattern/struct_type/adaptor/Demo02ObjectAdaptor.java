package desgin_pattern.struct_type.adaptor;

/**
 * @author tobing
 * @date 2021/9/8 17:41
 * @description 对象适配器
 */
public class Demo02ObjectAdaptor {
    // 对象适配器基于组合实现
    interface ITarget {
        void f1();

        void f2();

        void fc();
    }

    class Adaptee {
        public void fa() {
        }

        public void fb() {
        }

        public void fc() {
        }
    }

    // // 适配器Adaptor将Adaptee适配为ITarget
    class Adaptor implements ITarget {
        private Adaptee adaptee;

        public Adaptor(Adaptee adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void f1() {
            // 委托给Adaptee
            adaptee.fa();
        }

        @Override
        public void f2() {
            // 重新实现
        }

        // 注意与类适配器的区别
        @Override
        public void fc() {
            adaptee.fc();
        }
    }
}


