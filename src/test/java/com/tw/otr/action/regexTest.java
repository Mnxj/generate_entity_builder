package com.tw.otr.action;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class regexTest {
    @Test
    void should_get_params(){
        String str ="(DEWRWER),(\"),(sd),(new List(2323))";
        Matcher matcher = Pattern.compile("(?<=\\()[^\\)]+").matcher(str);
        List<String> values=new ArrayList<>();
        while(matcher.find()){
            values.add(matcher.group());
            System.out.println(matcher.group());
        }
        List<String> filterUpperCase = values.stream().filter(value -> Character.isUpperCase(value.charAt(0))).collect(Collectors.toList());
        assertEquals(1,filterUpperCase.size());
        assertEquals("DEWRWER",filterUpperCase.get(0));
    }

    @Test
    void should_get_imports(){
        String str ="import\n import123";
        Matcher matcher = Pattern.compile("imoprt").matcher(str);
        List<String> values=new ArrayList<>();
        while(matcher.find()){
            values.add(matcher.group());
        }
        assertEquals(2,values.size());
        assertEquals("import",values.get(0));
        assertEquals("import123",values.get(0));
    }

    @Test
    void should_get_(){
        String str ="(DEWRWER),(\"),(sd),(new Liet(2323)),(newNew())";
        System.out.println(str.replaceAll("[() ,](new )*", ""));
        System.out.println();
    }
}
