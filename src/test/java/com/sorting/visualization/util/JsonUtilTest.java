package com.sorting.visualization.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sorting.visualization.model.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W6 单元测试：JSON 工具类
 *
 * 覆盖点：序列化、反序列化、异常降级、泛型反序列化、Map 转对象、美化输出。
 */
@DisplayName("JSON 工具类单元测试")
class JsonUtilTest {

    private static Person samplePerson() {
        return new Person(1, "张三", 20, 88.5, "zhangsan@mju.edu.cn");
    }

    @Test
    @DisplayName("toJson：对象序列化为 JSON 字符串")
    void serializesObjectToJson() {
        String json = JsonUtil.toJson(samplePerson());

        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("张三"), json);
        assertTrue(json.contains("88.5"), json);
    }

    @Test
    @DisplayName("fromJson：JSON 反序列化为对象")
    void deserializesJsonToObject() {
        String json = JsonUtil.toJson(samplePerson());

        Person person = JsonUtil.fromJson(json, Person.class);

        assertNotNull(person);
        assertEquals(1, person.getId());
        assertEquals("张三", person.getName());
        assertEquals(20, person.getAge());
        assertEquals(88.5, person.getScore());
    }

    @Test
    @DisplayName("fromJson：非法 JSON 返回 null 而不抛异常")
    void returnsNullForMalformedJson() {
        assertNull(JsonUtil.fromJson("{不是合法JSON}", Person.class));
    }

    @Test
    @DisplayName("fromJsonOrThrow：非法 JSON 抛出 JsonProcessingException")
    void throwsForMalformedJsonInStrictVariant() {
        assertThrows(JsonProcessingException.class,
                () -> JsonUtil.fromJsonOrThrow("{不是合法JSON}", Person.class));
    }

    @Test
    @DisplayName("泛型反序列化：JSON 数组转 List")
    void deserializesGenericList() {
        String json = "[{\"id\":1,\"name\":\"张三\"},{\"id\":2,\"name\":\"李四\"}]";

        List<Person> people = JsonUtil.fromJson(json, new TypeReference<List<Person>>() {
        });

        assertNotNull(people);
        assertEquals(2, people.size());
        assertEquals("李四", people.get(1).getName());
    }

    @Test
    @DisplayName("泛型反序列化（抛出异常版本）：非法 JSON 抛异常")
    void throwsForMalformedGenericJson() {
        assertThrows(JsonProcessingException.class,
                () -> JsonUtil.fromJsonOrThrow("{bad}", new TypeReference<List<Person>>() {
                }));
    }

    @Test
    @DisplayName("convertValue：Map 转 Person 对象")
    void convertsMapToObject() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 3);
        map.put("name", "王五");
        map.put("age", 22);
        map.put("score", 70.0);

        Person person = JsonUtil.convertValue(map, Person.class);

        assertNotNull(person);
        assertEquals("王五", person.getName());
        assertEquals(70.0, person.getScore());
    }

    @Test
    @DisplayName("convertValue：无法转换时返回 null")
    void returnsNullWhenConversionFails() {
        assertNull(JsonUtil.convertValue("not-a-person", Person.class));
    }

    @Test
    @DisplayName("prettyPrint：输出带缩进格式")
    void prettyPrintAddsIndentation() {
        String pretty = JsonUtil.prettyPrint(samplePerson());

        assertTrue(pretty.contains(System.lineSeparator()) || pretty.contains("\n"), pretty);
    }

    @Test
    @DisplayName("fromJson：泛型版本遇到非法 JSON 返回 null")
    void genericFromJsonReturnsNullForMalformedJson() {
        assertNull(JsonUtil.fromJson("{bad}", new TypeReference<List<Person>>() {
        }));
    }

    @Test
    @DisplayName("fromJsonOrThrow：合法 JSON 正常解析")
    void strictVariantParsesValidJson() throws Exception {
        Person person = JsonUtil.fromJsonOrThrow("{\"id\":9,\"name\":\"赵六\"}", Person.class);

        assertEquals(9, person.getId());
        assertEquals("赵六", person.getName());
    }

    @Test
    @DisplayName("fromJsonOrThrow：合法泛型 JSON 正常解析")
    void strictGenericVariantParsesValidJson() throws Exception {
        List<Person> people = JsonUtil.fromJsonOrThrow("[{\"id\":1},{\"id\":2}]",
                new TypeReference<List<Person>>() {
                });

        assertEquals(2, people.size());
    }
}
