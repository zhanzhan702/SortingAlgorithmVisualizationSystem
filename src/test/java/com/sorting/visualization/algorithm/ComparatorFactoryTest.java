package com.sorting.visualization.algorithm;

import com.sorting.visualization.algorithm.impl.BubbleSort;
import com.sorting.visualization.model.Person;
import com.sorting.visualization.model.request.SortRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W6 单元测试：比较器工厂
 *
 * 覆盖点：
 * 1. 数值比较器的升序/降序/绝对值/反向四种比较方法
 * 2. comparatorInfo 为空时的默认策略
 * 3. 数据类型别名（INT → INTEGER）
 * 4. 字符串数值转换与非法字符串降级为 0
 * 5. Person 结构体按 age / id / name / score 字段排序
 */
@DisplayName("比较器工厂单元测试")
class ComparatorFactoryTest {

    private static SortRequest.ComparatorInfo info(String direction, String method) {
        SortRequest.ComparatorInfo comparatorInfo = new SortRequest.ComparatorInfo();
        comparatorInfo.setDirection(direction);
        comparatorInfo.setMethod(method);
        return comparatorInfo;
    }

    private static SortRequest.ComparatorInfo info(String direction, String method, String structField) {
        SortRequest.ComparatorInfo comparatorInfo = info(direction, method);
        comparatorInfo.setStructField(structField);
        return comparatorInfo;
    }

    private static Person person(int id, String name, int age, double score) {
        return new Person(id, name, age, score, name + "@mju.edu.cn");
    }

    @Nested
    @DisplayName("数值类型比较器")
    class NumericComparatorTest {

        @Test
        @DisplayName("升序：较小值在前")
        void ascending() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info("ascending", "numeric"));

            assertTrue(comparator.compare(1, 3) < 0);
            assertTrue(comparator.compare(3, 1) > 0);
            assertEquals(0, comparator.compare(2, 2));
        }

        @Test
        @DisplayName("降序：较大值在前")
        void descending() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info("descending", "numeric"));

            assertTrue(comparator.compare(3, 1) < 0);
            assertTrue(comparator.compare(1, 3) > 0);
        }

        @Test
        @DisplayName("绝对值法：按绝对值升序比较")
        void absolute() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info("ascending", "absolute"));

            assertTrue(comparator.compare(-5, 3) > 0, "abs(-5)=5 大于 3");
            assertTrue(comparator.compare(-2, 3) < 0, "abs(-2)=2 小于 3");
        }

        @Test
        @DisplayName("反向法：翻转比较结果")
        void reverse() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info("ascending", "reverse"));

            assertTrue(comparator.compare(1, 3) > 0, "反向法下 1 排在 3 之后");
        }

        @Test
        @DisplayName("千分位以外的浮点类型同样支持")
        void supportsDoubleAndLong() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("DOUBLE", info("ascending", "numeric"));

            assertTrue(comparator.compare(1.5, 2.5) < 0);
            assertTrue(((Comparator<Object>) comparator).compare(1L, 2L) < 0);
        }

        @Test
        @DisplayName("字符串数值按数值大小比较")
        void comparesNumericStrings() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info("ascending", "numeric"));

            assertTrue(comparator.compare("9", "10") < 0, "按数值比较 9 < 10");
        }

        @Test
        @DisplayName("Float 类型按数值大小比较")
        void supportsFloat() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("DOUBLE", info("ascending", "numeric"));

            assertTrue(comparator.compare(1.5f, 2.5f) < 0);
        }

        @Test
        @DisplayName("非数值字符串降级为 0")
        void nonNumericStringFallsBackToZero() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info("ascending", "numeric"));

            assertTrue(comparator.compare("abc", "5") < 0, "非法字符串按 0 处理");
        }
    }

    @Nested
    @DisplayName("默认策略与类型别名")
    class DefaultsTest {

        @Test
        @DisplayName("comparatorInfo 为 null 时默认升序数值比较")
        void defaultsToAscendingNumericWhenInfoIsNull() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", null);

            assertTrue(comparator.compare(1, 2) < 0);
        }

        @Test
        @DisplayName("dataType 为 INT 时等价于 INTEGER")
        void intAliasBehavesLikeInteger() {
            Comparator<Object> intComparator = ComparatorFactory.createComparator("INT", info("ascending", "numeric"));
            Comparator<Object> integerComparator = ComparatorFactory.createComparator("INTEGER", info("ascending", "numeric"));

            assertEquals(integerComparator.compare(4, 2), intComparator.compare(4, 2));
        }

        @Test
        @DisplayName("direction 为空时按升序处理")
        void blankDirectionFallsBackToAscending() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info(null, "numeric"));

            assertTrue(comparator.compare(1, 2) < 0);
        }

        @Test
        @DisplayName("direction 忽略大小写与首尾空格")
        void directionIsCaseAndSpaceInsensitive() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info(" DESCENDING ", "numeric"));

            assertTrue(comparator.compare(3, 1) < 0);
        }

        @Test
        @DisplayName("method 为空时按数值比较处理，不抛 NPE")
        void blankMethodFallsBackToNumeric() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("INTEGER", info("ascending", null));

            assertTrue(comparator.compare(1, 2) < 0);
        }

        @Test
        @DisplayName("无法识别的数据类型走数值比较，不抛异常")
        void unknownDataTypeFallsBackToNumeric() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("UNKNOWN", info("ascending", "numeric"));

            assertTrue(comparator.compare(1, 2) < 0);
        }
    }

    @Nested
    @DisplayName("Person 结构体比较器")
    class PersonComparatorTest {

        @Test
        @DisplayName("按 age 字段升序")
        void byAge() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", "age"));

            assertTrue(comparator.compare(person(1, "张三", 20, 88.5), person(2, "李四", 21, 95.0)) < 0);
        }

        @Test
        @DisplayName("按 id 字段降序")
        void byIdDescending() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("descending", "numeric", "id"));

            assertTrue(comparator.compare(person(1, "张三", 20, 88.5), person(2, "李四", 21, 95.0)) > 0);
        }

        @Test
        @DisplayName("按 name 字段升序（字典序）")
        void byName() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", "name"));

            assertTrue(comparator.compare(person(1, "Amy", 20, 88.5), person(2, "Bob", 21, 95.0)) < 0);
        }

        @Test
        @DisplayName("按 score 字段升序")
        void byScore() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", "score"));

            assertTrue(comparator.compare(person(1, "张三", 20, 70.0), person(2, "李四", 21, 95.0)) < 0);
        }

        @Test
        @DisplayName("未知字段按 0 处理，比较结果为相等")
        void unknownFieldTreatedAsZero() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", "unknownField"));

            assertEquals(0, comparator.compare(person(1, "张三", 20, 70.0), person(2, "李四", 21, 95.0)));
        }

        @Test
        @DisplayName("age/score 为 null 时按 0 处理")
        void nullAgeAndScoreTreatedAsZero() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", "age"));
            Person noAge = new Person();
            noAge.setId(1);
            noAge.setName("张三");
            Person withAge = person(2, "李四", 21, 95.0);

            assertTrue(comparator.compare(noAge, withAge) < 0);
        }

        @Test
        @DisplayName("按 email 字段排序：邮箱相同则视为相等")
        void byEmailSameAddressIsEqual() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", "email"));
            Person first = person(1, "张三", 20, 70.0);
            Person second = person(2, "李四", 21, 95.0);
            second.setEmail(first.getEmail());

            assertEquals(0, comparator.compare(first, second));
        }

        @Test
        @DisplayName("name 使用 reverse 方法时字典序翻转")
        void byNameWithReverseMethod() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "reverse", "name"));

            assertTrue(comparator.compare(person(1, "Amy", 20, 88.5), person(2, "Bob", 21, 95.0)) > 0);
        }

        @Test
        @DisplayName("未指定 structField 时默认按 score 排序")
        void defaultsToScoreWhenStructFieldMissing() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", new SortRequest.ComparatorInfo());

            assertTrue(comparator.compare(person(1, "张三", 20, 70.0), person(2, "李四", 21, 95.0)) < 0);
        }

        @Test
        @DisplayName("structField 忽略大小写")
        void structFieldIsCaseInsensitive() {
            Comparator<Object> comparator = ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", " AGE "));

            assertTrue(comparator.compare(person(1, "张三", 20, 70.0), person(2, "李四", 21, 95.0)) < 0);
        }

        @Test
        @DisplayName("与排序算法联动：Person 列表按分数升序排好")
        void worksWithBubbleSort() {            Comparator<Object> objectComparator =
                    ComparatorFactory.createComparator("PERSON", info("ascending", "numeric", "score"));
            Comparator<Person> personComparator = (a, b) -> objectComparator.compare(a, b);

            List<Person> people = new ArrayList<>(List.of(
                    person(1, "张三", 20, 88.5),
                    person(2, "李四", 21, 95.0),
                    person(3, "王五", 22, 70.0)
            ));

            List<Person> sorted = new BubbleSort<Person>().perform(people, personComparator).getSortedData();

            assertEquals(List.of(70.0, 88.5, 95.0), sorted.stream().map(Person::getScore).toList());
            assertEquals("王五", sorted.get(0).getName());
        }
    }
}
