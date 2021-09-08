package desgin_pattern.struct_type.adaptor;

/**
 * @author tobing
 * @date 2021/9/8 17:45
 * @description 使用场景1：封装有缺陷的接口设计
 */
public class Demo03 {
    static class SDK {
        public static void staticFunction1() {
        }

        public void uglyNamingFunction2() {
        }

        public void tooManyParamsFunction3(int paramA, int paramB) {
        }

        public void lowPerformanceFunction4() {
        }
    }

    interface ITarget {
        void function1();

        void function2();

        void fucntion3(ParamsWrapperDefinition paramsWrapper);

        void function4();
    }

    class SDKAdaptor extends SDK implements ITarget {
        @Override
        public void function1() {
            staticFunction1();
        }

        @Override
        public void function2() {
            super.uglyNamingFunction2();
        }

        @Override
        public void fucntion3(ParamsWrapperDefinition paramsWrapper) {
            super.tooManyParamsFunction3(paramsWrapper.getParams());
        }

        @Override
        public void function4() {
            // 重新实现
        }
    }
}