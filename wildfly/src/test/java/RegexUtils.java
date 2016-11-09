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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class RegexUtils {

    private static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]", Pattern.DOTALL);

    public static String escapeSpecialRegexChars(String str) {
        return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");
    }

    public static String surroundRegexBy(String regex, String surroundingString) {
        return surroundingString + regex + surroundingString;
    }

    public static String escapeSpecialRegexCharsAndSurroundBy(String regex, String surroundingString) {
        return surroundRegexBy(escapeSpecialRegexChars(regex), surroundingString);
    }

    public static List<String> convertStringLinesToRegexs(String text) {
        String lines[] = text.split("\\r?\\n");
        List<String> regexs = new ArrayList<>();
        for (String regex : lines) {
            regexs.add(escapeSpecialRegexCharsAndSurroundBy(regex, ".*"));
        }
        return regexs;
    }
}
