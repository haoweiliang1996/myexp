/**
 * Created by haowei on 16-11-23.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.io.FileWriter;
public class EXP {
    public static HashMap<String,String> patternMap = new HashMap<>();
    public static HashMap<String,Integer> count2=new HashMap<String,Integer>();
    public static HashMap<String,Integer> count3=new HashMap<String,Integer>();
    public static String NoPattern="";

    public static void loadPattern(String strFile)
            throws Exception {
        File file = new File(strFile);

        if (file.isFile() && file.exists()) {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    file), "GBK");
            BufferedReader br = new BufferedReader(read);
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()){
                    if (!line.contains("\t")) {
                        System.out.println("读入NoPattern");
                        if (NoPattern != "")
                            System.out.print("error" + "输入文件中有多个空项,即所谓其它消费");
                        NoPattern=line;
                        continue;
                    }

                    int index = line.indexOf("\t");
                    String key = line.substring(0, index);
                    String value = line.substring(index+1).trim();
                    value=value.toUpperCase();
                    value = value.replaceAll("\\+", "");
                    value = value.replaceAll("\\*", ".*");

                    //添加以处理 W1|W2*W3|W4的输入
                    String[] vauleArray=value.split("\\s+");
                    //System.out.println("debug"+vauleArray.length);
                    StringBuffer sb=new StringBuffer();
                    for (int i=0;i<vauleArray.length;i++){
                        //System.out.print("debug"+vauleArray[i]);
                        String[] phaseArrary=vauleArray[i].split("\\.\\*");
                        //System.out.println("debug"+phaseArrary.length);
                        for (int j=0;j<phaseArrary.length;j++) {
                            String s = phaseArrary[j];
                            if (s.contains("|"))
                                s = "(" + s + ")";
                            if (j != phaseArrary.length - 1)
                                s += ".*";
                            //System.out.println("debug"+s);
                            sb.append(s);
                        }
                        if(i!=vauleArray.length-1)
                            //不同的模式间的间隔符是@
                            sb.append("@");
                    }
                    if (sb.length()>0) {
                        value = sb.toString();
                        //System.out.println("debug"+value);
                    }
                    //添加以处理 W1|W2*W3|W4的输入

                    if(patternMap.containsKey(key))
                        value = patternMap.get(key)+ "@" + value;
                    patternMap.put(key, value);
                }
            }
            br.close();
            System.out.println(patternMap);
        }
        else {
            System.out.println("找不到指定的文件");
        }
    }


    public static void processCluster(String inFile, String outFile)
            throws Exception {
        //HashSet<String> resultSet = new HashSet<>();
        ArrayList<String> rList = new ArrayList<>();
        File fileIn = new File(inFile);
        File fileOut = new File(outFile);
        fileOut.createNewFile();
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        BufferedWriter bw = new BufferedWriter(osw);

        if (fileIn.isFile() && fileIn.exists()) {
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    fileIn), "GBK");
            BufferedReader br = new BufferedReader(read);
            String line = null;

            while ((line = br.readLine()) != null) {
                //调换位置
                if (line.startsWith("Cluster")) {
                    String sline = line.trim();
                    if (sline.indexOf("/") != -1){
                        sline = sline.substring(0, sline.indexOf("/") - 1).trim();
                    }
                    sline = sline.replaceAll("\\s+", "-");
                    sline = sline.replaceAll("\\t+", "-");
                    String str3 = sline.substring(0, 8);

                    int idx1 = sline.lastIndexOf("-");
                    String str1 = sline.substring(8, idx1);
                    String str2 = sline.substring(idx1 + 1);

                    str3 = str3 + str2 + "-" + str1;
                    rList.add(str3);
                    continue;
                }
                if(!line.isEmpty()&&!line.startsWith("Cluster")){
                    String[] line_c=line.split("\\t",-1);//不忽略尾部的/t
                    if(line_c.length<4) {
                        System.out.println("drop" + line);
                        continue;
                    }
                    String resultTemp=line;
                    for(int i=2;i<4;i++) {
                        String line_cs=line_c[i];
                        if(line_cs.length()==0)
                            continue;
                        line_cs=line_cs.toUpperCase();
                        //System.out.println("debug"+line_cs);
                        line_cs = line_cs.trim();
                        line_cs = line_cs.replaceAll("\\s+", "，");
                        line_cs = line_cs.replaceAll("，，", "，");
                        line_cs = line_cs.replaceAll("，，，", "，");
                        String result = type(line_cs).trim();
                        //统计行业
                        String[] result_split=result.split("\\|");
                        for(String str:result_split){
                            if(i==2){
                                if (count2.containsKey(str))
                                    count2.put(str,count2.get(str)+1);
                                else
                                    count2.put(str,1);
                            }
                            else if(i==3){
                                if (count3.containsKey(str))
                                    count3.put(str,count3.get(str)+1);
                                else
                                    count3.put(str,1);
                            }
                        }
                        //统计行业
                        resultTemp += "\t" + result;
                    }
                    rList.add(resultTemp);
                    continue;
                }
                if (line.isEmpty())
                    continue;
                rList.add(line);
            }

            for(String e : rList)
                bw.write(e + "\n");

            br.close();
            bw.close();
        }
        else {
            System.out.println("找不到指定的文件");
        }
    }

    private static String type(String line) {
        StringBuffer sb = new StringBuffer();
        Iterator<Entry<String, String>> iter = patternMap.entrySet().iterator();
        while(iter.hasNext()){
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String regex = entry.getValue();

            if (isKeyType(regex,line)){
                if(sb.indexOf(key) == -1)
                    sb.append(key + "|");
            }
        }
        String type= NoPattern;
        if (sb.length() != 0)
            type = sb.substring(0,sb.length() - 1);
        return type ;// return type + "\t" + line; 修改
    }

    private static boolean isKeyType(String regex, String line) {
        String[] regexs = regex.split("@");
        for(String s : regexs){
            if(s.contains(".*")){
                String[] al = s.split("\\.\\*");
                boolean flag = true;
                for(String e: al) {
					/*if(!line.contains(e)) {
						flag = false;
						break;
					}*/
                    //System.out.println("debug"+e);
                    if (!Pattern.compile(e).matcher(line).find()) {
                        flag = false;
                        break;
                    }
                }
                if(flag) {
                    //System.out.println("debug"+s);
                    return true;
                }
            }else{
                if(Pattern.compile(s).matcher(line).find()) {
                    //System.out.println("debug"+s);
                    return true;
                }
            }
        }
        return false;
    }
    public static void main(String[] args) throws Exception
    {
        loadPattern("dat/问题类别模式.txt");
        processCluster("dat/prase.in","dat/prase_out.txt");

        File fileOut = new File("dat/贷款人所在行业.txt");
        fileOut.createNewFile();
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        BufferedWriter bw = new BufferedWriter(osw);
        //BufferedWriter bw=new BufferedWriter(new FileWriter("dat/贷款人所在行业.txt"));

        System.out.println("贷款人所在行业");
        bw.write("//贷款人所在行业\n");
        Iterator<Entry<String, Integer>> iter = count2.entrySet().iterator();
        while(iter.hasNext()){
            Entry<String,Integer> entry=iter.next();
            System.out.println(entry.getKey()+"\t"+entry.getValue());
            bw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        bw.close();

        fileOut = new File("dat/贷款流向行业.txt");
        fileOut.createNewFile();
        osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        bw = new BufferedWriter(osw);

        bw.write("//贷款流向行业\n");
        System.out.println("贷款流向行业");
        iter=count3.entrySet().iterator();
        while(iter.hasNext()){
            Entry<String,Integer> entry=iter.next();
            System.out.println(entry.getKey()+"\t"+entry.getValue());
            bw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        bw.close();
    }
}

