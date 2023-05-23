package io.github.speechchemistry;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.Test;

import io.github.speechchemistry.UniVectorPhone;


/**
 * Unit test for simple App.
 */
public class UniVectorPhoneTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        Map<String, Integer> fMap = new LinkedHashMap<String, Integer>();
        fMap.put("syllabic", -1);
        fMap.put("DORSAL", -1);
        fMap.put("LABIAL", 1);
        fMap.put("CORONAL", -1);
        fMap.put("nasal", -1);
        UniVectorPhone fPhone = new UniVectorPhone("f", fMap, 4);
        assertEquals(fPhone.toString(),"f");


        Map<String, Integer> wMap = new LinkedHashMap<String, Integer>();
        wMap.put("syllabic", -1);
        wMap.put("DORSAL", 1);
        wMap.put("LABIAL", 1);
        wMap.put("CORONAL", -1);
        wMap.put("nasal", -1);
        UniVectorPhone wPhone = new UniVectorPhone("w", wMap, 0);
        assertEquals(wPhone.toString(), "w");
        assertEquals(wPhone.size(),9);

        wMap = new LinkedHashMap<String, Integer>();
        wMap.put("syllabic", 1);
        wMap.put("DORSAL", 1);
        wMap.put("LABIAL", 1);
        wMap.put("CORONAL", -1);
        wMap.put("nasal", -1);
        wMap.put("high", 1);
        wMap.put("low", -1);
        wMap.put("front", -1);
        wMap.put("back", 1);
        wMap.put("round", 1);
        UniVectorPhone uph1 = new UniVectorPhone("u", wMap);

        wMap = new LinkedHashMap<String, Integer>();
        wMap.put("syllabic", 1);
        wMap.put("DORSAL", 1);
        wMap.put("LABIAL", -1);
        wMap.put("CORONAL", -1);
        wMap.put("nasal", -1);
        wMap.put("high", 1);
        wMap.put("low", -1);
        wMap.put("front", 1);
        wMap.put("back", -1);
        wMap.put("round", -1);
        UniVectorPhone uph2 = new UniVectorPhone("i", wMap);

        wMap = new LinkedHashMap<String, Integer>();
        wMap.put("syllabic", 1);
        wMap.put("DORSAL", 1);
        wMap.put("LABIAL", -1);
        wMap.put("CORONAL", -1);
        wMap.put("nasal", -1);
        wMap.put("high", 1);
        wMap.put("low", -1);
        wMap.put("front", -1);
        wMap.put("back", -1);
        wMap.put("round", -1);
        UniVectorPhone uph3 = new UniVectorPhone("barred i", wMap);
    }
}
