package debrepo.teamcity.web.tags;

import org.apache.commons.lang3.StringEscapeUtils;

public class Utilities {
    public static String escapeJS(String value) {
        return StringEscapeUtils.escapeEcmaScript(value);
    }
}