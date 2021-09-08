package desgin_pattern.struct_type.adaptor;

/**
 * @author tobing
 * @date 2021/9/8 17:41
 * @description 类适配器
 */
public class Demo01ClassAdaptor {
    // 类适配器基于继承实现
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


    // 适配器Adaptor将Adaptee适配为ITarget
    class Adaptor extends desgin_pattern.struct_type.adaptor.Adaptee implements desgin_pattern.struct_type.adaptor.ITarget {
        @Override
        public void f1() {
            super.fa();
        }

        @Override
        public void f2() {
            // 重新实现
        }
        // 由于fc函数兼容，无需实现，这是与对象适配器最大的不同
    }
}

