package hawkeye.service.util;

import hawkeye.graph.model.GraphComparison;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class LineMatchChecker {
    public boolean hasDifferences(GraphComparison<String> graphComparison) {
        if (!graphComparison.isIsomorphicComparison()) {
            return true;
        }

        List<String> firstValue = graphComparison.getValues().get(0);
        for (List<String> value : graphComparison.getValues().subList(1, graphComparison.getValues().size())) {
            if (hasDifferences(firstValue, value)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasDifferences(List<String> a, List<String> b) {
        if (a.size() != b.size()) {
            return true;
        }

        Iterator<String> ai = a.iterator();
        Iterator<String> bi = b.iterator();
        while (ai.hasNext()) {
            String as = removeWhitespace(ai.next());
            String bs = removeWhitespace(bi.next());

            if (!isEqualIgnoreWhitespace(as, bs)) {
                return true;
            }
        }

        return false;
    }

    public boolean areLinesSame(List<String> lines) {
        String firstLine = lines.get(0);
        for (String line : lines.subList(1, lines.size())) {
            if (!isEqualIgnoreWhitespace(firstLine, line)) {
                return false;
            }
        }

        return true;
    }

    private boolean isEqualIgnoreWhitespace(String a, String b) {
        if (Objects.equals(a, b)) {
            return true;
        } else if ((a == null) || (b == null)) {
            return false;
        }

        String as = removeWhitespace(a);
        String bs = removeWhitespace(b);

        return as.equals(bs);
    }

    private String removeWhitespace(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("　", "").replaceAll("\\s", "");
    }
}
