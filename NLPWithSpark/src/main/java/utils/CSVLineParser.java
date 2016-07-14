package utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVLineParser {
    private Map<String, Integer> columnNamesMap = new HashMap<String, Integer>();
    private String[] tokens;

    static Pattern pattern = Pattern.compile("([^\",]*),|\"([^\"]*)\",");

    public CSVLineParser(String[] columnNames, int[] index) {
        int count = 0;
        for (String columnName : columnNames) {
            columnNamesMap.put(columnName, index[count]);
            count++;
        }
    }

    public CSVLineParser(String[] columnNames) {
        int count = 0;
        for (String columnName : columnNames) {
            columnNamesMap.put(columnName, new Integer(count));
            count++;
        }
    }
    
    public CSVLineParser() {}

    public String[] parse(String line) {
        List<String> tokensList = new ArrayList<String>();
        line = line + ",";
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String token;
            if (matcher.group(1) == null || matcher.group(1).equals("")) {
                token = matcher.group(2);
            } else {
                token = matcher.group(1);
            }
            tokensList.add(token);
        }
        tokens = tokensList.toArray(new String[0]);
        return tokens; 
        // char nextchar = ',';
        //
        // StringBuffer currentValue = new StringBuffer();
        // List<String> tokens = new ArrayList<String>();
        // boolean nextIsFirst = true;
        //
        // for(char c: line.toCharArray()){
        // if(c != nextchar){
        // currentValue.append(c);
        // if(nextIsFirst && c== '"'){
        // nextchar = '"';
        // }
        // nextIsFirst = false;
        // }else{
        // tokens.add(currentValue.toString());
        // currentValue = new StringBuffer();
        // nextIsFirst = true;
        // }
        // }
        //
        //
        //
        // //([^,]*)
        // tokens = line.split(",");
    }

    public String getValue(String key) {
        Integer index = columnNamesMap.get(key);
        if (index != null) {
            return tokens[index];
        } else {
            return null;
        }
    }

    public static enum States {
        TokenStart, CWait, QWait, TokenEnd
    };

    public static List<String> tokenizeCSV(String line) {
        List<String> tokens = new ArrayList<String>();
        StringBuffer buf = new StringBuffer();
        States s = States.TokenStart;

        for (char c : line.toCharArray()) {
            if (s == States.TokenStart) {
                if (c == '"') {
                    s = States.QWait;
                }else if (c == ',') {
                    tokens.add(buf.toString());
                    buf = new StringBuffer();
                } else {
                    s = States.CWait;
                    buf.append(c);
                }
            } else if (s == States.CWait) {
                if (c == ',') {
                    s = States.TokenStart;
                    tokens.add(buf.toString());
                    buf = new StringBuffer();
                } else {
                    buf.append(c);
                }
            } else if (s == States.QWait) {
                if (c == '"') {
                    s = States.TokenEnd;
                } else {
                    buf.append(c);
                }
            } else {
                if (c == ',') {
                    s = States.TokenStart;
                    tokens.add(buf.toString());
                    buf = new StringBuffer();
                } else {
                    throw new RuntimeException("Found the character " + c + " after \", and it should have been ,");
                }
            }
        }
        tokens.add(buf.toString());
        return tokens;
    }

    
    public static String printList(String[] list){
        StringBuffer buf = new StringBuffer(); 
        for(String str: list){
            buf.append(str).append("|");
        }
        return buf.toString();
    }
    
    public static void main(String[] args) throws Exception {
    	final CSVLineParser parser = new CSVLineParser();
    	
    	FileUtils.doForEachLine("data/us-election-aprial07.csv", new FileUtils.LineProcessor() {
			public boolean process(String line)  {
				System.out.println(Arrays.toString(parser.parse(line)));
				return true;
			}
		});
    }
}
