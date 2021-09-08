package desgin_pattern.struct_type.adaptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tobing
 * @date 2021/9/8 17:45
 * @description 使用场景1：封装有缺陷的接口设计
 */
public class Demo04 {
    // 敏感词过滤系统A接口
    class SystemAFilter {
        // 将text中敏感词使用***替换
        public String filterSexWords(String text) {
            return null;
        }

        public String filterPoliticalWords(String text) {
            return null;
        }
    }

    // 敏感词过滤系统B接口
    class SystemBFilter {
        // 将text中敏感词使用***替换
        public String filter(String text) {
            return null;
        }
    }

    // 敏感词过滤系统C接口
    class SystemCFilter {
        // 将text中敏感词使用***替换
        public String filterWords(String text, String mask) {
            return null;
        }
    }

    // 使用适配器模式前：代码可测试性、扩展性不好
    class RiskManagementA {
        private SystemAFilter aFilter = new SystemAFilter();
        private SystemBFilter bFilter = new SystemBFilter();
        private SystemCFilter cFilter = new SystemCFilter();

        public String filterSensitiveWords(String text) {
            String maskedText = aFilter.filterSexWords(text);
            maskedText = aFilter.filterPoliticalWords(maskedText);
            maskedText = bFilter.filter(maskedText);
            maskedText = cFilter.filterWords(maskedText, "***");
            return maskedText;
        }
    }

    // 使用适配器模式后
    // 统一接口定义
    interface ISystemFilter {
        String filter(String text);
    }

    class SystemFilterAdaptor implements ISystemFilter {
        private SystemAFilter aFilter = new SystemAFilter();
        private SystemBFilter bFilter = new SystemBFilter();
        private SystemCFilter cFilter = new SystemCFilter();

        @Override
        public String filter(String text) {
            String maskedText = aFilter.filterSexWords(text);
            maskedText = aFilter.filterPoliticalWords(maskedText);
            maskedText = bFilter.filter(maskedText);
            maskedText = cFilter.filterWords(maskedText, "***");
            return maskedText;
        }
    }

    static class RiskManagementB {
        private List<ISystemFilter> filters = new ArrayList<>();

        public void addFilter(ISystemFilter filter) {
            filters.add(filter);
        }

        public String filterSensitiveWords(String text) {
            String maskedText = text;
            for (ISystemFilter filter : filters) {
                maskedText = filter.filter(maskedText);
            }
            return maskedText;
        }
    }
}