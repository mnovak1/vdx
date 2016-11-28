/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringRegexUtils {

    private static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]", Pattern.DOTALL);

    public static String escapeSpecialRegexChars(String str) {
        return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");
    }

    public static String surroundStringBy(String regex, String surroundingString) {
        return surroundingString + regex + surroundingString;
    }

    public static String escapeSpecialRegexCharsAndSurroundBy(String regex, String surroundingString) {
        return surroundStringBy(escapeSpecialRegexChars(regex), surroundingString);
    }

    public static List<String> addLinesToListAndEscapeRegexChars(String text) {
        String lines[] = text.split("\\r?\\n");
        List<String> regexs = new ArrayList<>();
        for (String regex : lines) {
            regexs.add(escapeSpecialRegexCharsAndSurroundBy(regex, ".*"));
        }
        return regexs;
    }

    /**
     * It will take multiline text and escape all characters which are used in Java Pattern, Then it will try to create
     *  pattern which matches the multiline text.
     * @param text multiline text which should be converted to regex/pattern which matches the text
     * @return
     */
    public static String convertStringLinesToOneRegex(String text) {
        // first make regex from each line
        List<String> regexs = addLinesToListAndEscapeRegexChars(text);
        // now join them all together creating one big regex
        StringBuilder bigRegex = new StringBuilder();
        for (String regex : regexs) {
            bigRegex.append(regex);
        }
        String bigString = bigRegex.toString();

        // remove multiple .* from it
        bigString = removeMultipleOccurencesOfMatchAnything(bigString);
        return bigString;
    }

    /**
     * This will remove line numbers from text. For example for input:
     *   3:     <extension modules="org.aaajboss.as.clustering.infinispan"/>
     *   it will return:
     *          <extension modules="org.aaajboss.as.clustering.infinispan"/>
     *
     * @param text
     * @return
     */
    public static String removeLineNumbersWithDoubleDotFromString(String text) {
        return text.replaceAll(".*[0-9]+:", "");
    }

    /**
     * Remove multiple .* from it, there is catch that "\..*" will be replaced as "\.*" which is wrong. Thus there is
     * one more replace of "\.*" to be ".*"
     */
    public static String removeMultipleOccurencesOfMatchAnything(String text) {
        return text.replaceAll("[\\.\\*]+", "\\.\\*").replaceAll("[\\\\.\\*]+", "\\.\\*");
    }

    // just for fast tries
    public static void main(String[] args) {
        String expectedErrorMessage = "OPVDX001: Validation error in standalone-full-ha-to-cripple.xml ================\n" +
                "\n" +
                "  1: <?xml version=\"1.0\" encoding=\"UTF-8\"?><server xmlns=\"urn:jboss:domain:5.0\">\n" +
                "  2:   <extensions>\n" +
                "  3:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "                    ^^^^ 'modules' isn't an allowed attribute for the 'extension' element\n" +
                "                         Attributes allowed here are: module\n" +
                "                         Did you mean 'module'?\n" +
                "\n" +
                "  4:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "  5:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>\n" +
                "  6:     <extension modules=\"org.aaajboss.as.clustering.infinispan\"/>.\n";
        System.out.println(convertStringLinesToOneRegex(expectedErrorMessage));
    }
}
