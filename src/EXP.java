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
import java.security.Key;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;
import java.util.stream.*;
import java.util.function.*;

public class EXP {
    public static HashMap<String,String> patternMap = new HashMap<>();
    public static HashMap<String,Integer> count2=new HashMap<String,Integer>();
    public static HashMap<String,Integer> count3=new HashMap<String,Integer>();
    public static HashMap<String,Vector<String>> pattern_to_deny_pattern=new HashMap<>();
    public static Vector<String> keyList=new Vector<>();
    public static String NoPattern="";
    public static myTreeKount treeCount;

    //��������˸�ǰ�����������ҵ�class�����и�class
    public static class myTreeKount{
        private static class flag{
            public int ROOT=-2; //pre[i]Ϊ-2ʱ���ڵ�������
            public int UN_PRASE=-1;
        }
        private flag FLAG=new flag();
        private int pre[];
        private HashMap<String,Integer> key_to_keyid=new HashMap<>();
        private int countHeadTab(String s){
            int kount=0;
            for(char c:s.toCharArray()){
                if(c=='\t')
                    kount++;
                else
                    break;
            }
            return kount;
        }

        private void buildTree(int keyId,int keyIdOfFather){
            if(keyId>=keyList.size()){
                System.out.println("error "+"����ʱkey����Խ�� keyId: "+keyId);
                return;
            }

            pre[keyId]=keyIdOfFather;
            for(int i=keyId+1;i<keyList.size();i++) {
                if(countHeadTab(keyList.get(keyId))>=countHeadTab(keyList.get(i))) {
                    return;
                }
                if(pre[i]==FLAG.UN_PRASE)
                    buildTree(i, keyId);
            }
        }

        public myTreeKount(String strFile) throws Exception
        {
            File file=new File(strFile);
            if(file.isFile()&&file.exists()){
                InputStreamReader read = new InputStreamReader(new FileInputStream(
                        file), "GBK");
                BufferedReader br = new BufferedReader(read);
                String line = null;
                while ((line = br.readLine()) != null) {
                    int splitIndex=line.lastIndexOf('\t');
                    if(splitIndex==-1) {
                        if(!line.equals(NoPattern)){
                            System.out.println("info "+"���е�class��Ч");
                            continue;
                        }
                    }
                    String key;
                    if(splitIndex==-1)
                        key=line;
                    else
                        key=line.substring(0,splitIndex);
                    key_to_keyid.put(key.trim(),keyList.size());
                    keyList.add(key);
                }
            }
            else
                System.out.println("info "+"���е�class��Ч");
            pre=new int[keyList.size()];
            for(int i=0;i<pre.length;i++)
                pre[i]=FLAG.UN_PRASE;//���ڵ�
            for(int i=0;i<pre.length;i++)
                if(pre[i]==FLAG.UN_PRASE) {
                    buildTree(i,FLAG.ROOT);
                }
        }

        public int getFatherKeyId(int keyid){
            return pre[keyid];
        }
        public int getKeyId(String key){
            System.out.println("debug getKeyId:"+key);
            if(!key_to_keyid.containsKey(key))
                return FLAG.ROOT;
            return key_to_keyid.get(key);
        }
        public String getKeyById(int id){
            // System.out.println("debug getKeyById id:"+id);
            if(id>=keyList.size())
                System.out.println("KeyList�±�Խ�磺"+id+" "+keyList.size());
            return keyList.get(id);
        }
    }

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
                        System.out.println("����NoPattern");
                        if (!NoPattern .equals(""))
                            System.out.println("error" + "�����ļ����ж������,����ν��������");
                        NoPattern=line;
                        continue;
                    }

                    int index = line.indexOf("\t");
                    String key = line.substring(0, index);
                    String value = line.substring(index+1).trim();
                    value=value.toUpperCase();
                    value = value.replaceAll("\\+", "");
                    value = value.replaceAll("\\*", ".*");

                    //����Դ��� W1|W2*W3|W4������
                    String[] vauleArray=value.split("\\s+");
                    //System.out.println("debug"+vauleArray.length);
                    StringBuilder sb=new StringBuilder();
                    for (int i=0;i<vauleArray.length;i++){
                        //System.out.print("debug"+vauleArray[i]);

                        //������Ҫȥ����ģʽ������
                        int throwBeginIndex=vauleArray[i].indexOf("-");

                        Vector<String> temp=new Vector<>();
                        if(throwBeginIndex!=-1){
                            String[] throwStrList=vauleArray[i].substring(throwBeginIndex+1).split(",");//to-do
                            vauleArray[i]=vauleArray[i].substring(0,throwBeginIndex);
                            for(String str:throwStrList){
                                if(!str.contains("|"))
                                    temp.add(str);
                                else
                                {
                                    StringBuilder sbuilder=new StringBuilder();
                                    String[] strTempList=str.split("\\.\\*");
                                    for(String strTemp:strTempList){
                                        if(strTemp.contains("|"))
                                            sbuilder.append("("+strTemp+")"+".*");
                                        else
                                            sbuilder.append(strTemp+".*");
                                    }
                                    if(sbuilder.length()>0)
                                        temp.add(sbuilder.delete(sbuilder.length()-2,sbuilder.length()).toString());
                                }
                            }
                        }
                        //����Ҫȥ����ģʽ�����롷

                        if(vauleArray[i].contains("|")) {
                            String[] phaseArrary = vauleArray[i].split("\\.\\*");
                            //System.out.println("debug"+phaseArrary.length);
                            for (String s:phaseArrary) {
                                if (s.contains("|"))
                                    s = "(" + s + ")";
                                //System.out.println("debug"+s);
                                sb.append(s+".*");
                            }
                            sb.delete(sb.length()-2,sb.length());
                        }
                        else
                            sb.append(vauleArray[i]);

                        String nowPrasePattern=sb.substring(sb.lastIndexOf("@")+1);
                        System.out.println("debug nowPrasePattern:"+nowPrasePattern);
                        if(!pattern_to_deny_pattern.containsKey(nowPrasePattern))
                            pattern_to_deny_pattern.put(nowPrasePattern,temp);

                        if(i!=vauleArray.length-1)
                            sb.append("@");                     //��ͬ��ģʽ��ļ������@
                    }
                    if (sb.length()>0) {
                        value = sb.toString();
                        //System.out.println("debug"+value);
                    }
                    //����Դ��� W1|W2*W3|W4������

                    if(patternMap.containsKey(key))
                        value = patternMap.get(key)+ "@" + value;
                    patternMap.put(key, value);
                }
            }
            br.close();
            System.out.println(patternMap);
            System.out.println("debug "+"pattern_to_deny_pattern "+pattern_to_deny_pattern.toString());
        }
        else {
            System.out.println("�Ҳ���ָ�����ļ�");
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
                //����λ��
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
                    String[] line_c=line.split("\\t",-1);//������β����/t
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
                        line_cs = line_cs.replaceAll("\\s+", "��");
                        line_cs = line_cs.replaceAll("����", "��");
                        line_cs = line_cs.replaceAll("������", "��");
                        String result = type(line_cs).trim();

                        //ͳ����ҵ
                        if(result.length()==0) {
                            System.out.println("debug + line_cs: " + line_cs + " result:" + result);
                            System.out.println("��Ϊĳ����� ɾ���������ҵ���class");
                            result="������";
                        }
                        String[] result_split=result.split("\\|");
                        HashSet<String> keyIdSet=new HashSet<>();
                        for(String str:result_split){
                            final int FLAG_ROOT=-2;
                            for(int id=treeCount.getKeyId(str);id!=FLAG_ROOT;id=treeCount.getFatherKeyId(id)){
                                keyIdSet.add(treeCount.getKeyById(id));
                            }
                        }
                        for(String str:keyIdSet) {
                            if (i == 2) {
                                if (count2.containsKey(str))
                                    count2.put(str, count2.get(str) + 1);
                                else
                                    count2.put(str, 1);
                            } else if (i == 3) {
                                if (count3.containsKey(str))
                                    count3.put(str, count3.get(str) + 1);
                                else
                                    count3.put(str, 1);
                            }
                        }
                        //ͳ����ҵ

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
            System.out.println("�Ҳ���ָ�����ļ�");
        }
    }

    //�ж�һ��sentence��class��
    private static String type(String line) {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> iter = patternMap.entrySet().iterator();

        while(iter.hasNext()){
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String regex = entry.getValue();

            String matchedPattern=isKeyType(regex,line);
            if(matchedPattern.length()==0)
                continue;
            matchedPattern+="\t"+key;
            System.out.println("debug "+"matchedPattern "+matchedPattern);
            sb.append(matchedPattern + "@");
        }
        String type= NoPattern;
        if (sb.length() != 0) {
            System.out.println("debug typeResult"+sb.substring(0,sb.length()-1));
            //<<����ȥ��ģʽ
            String[] typeResult = sb.substring(0, sb.length() - 1).split("@");//ȥ��ĩβ�ġ�@��
            String[] phaseList=new String[typeResult.length];
            String[] classList=new String[typeResult.length];
            String[] patternList=new String[typeResult.length];
            for(int i=0;i<typeResult.length;i++){
                String[] temp=typeResult[i].split("\\t");
                phaseList[i]=temp[0];
                patternList[i]=temp[1];
                classList[i]=temp[2];
            }
            HashSet<String> denyPatternSet=new HashSet<String>();
            for(String pa:patternList){
                if(!pattern_to_deny_pattern.containsKey(pa))
                {
                    System.out.println("error+ "+pa+"û�г����� pattern_to_deny_pattern");
                    continue;
                }
                for(String denyPattern:pattern_to_deny_pattern.get(pa)){
                    denyPatternSet.add(denyPattern);
                }
            }

            sb=new StringBuilder();

            //��Ҫ�����ҽ��������� to-do ��Ȼ���ܴ��� 		����Ͷ�ʣ�������	Ͷ��*ƻ��*��Ʒ-ƻ��*��Ʒ
            for(int i=0;i<phaseList.length;i++){
                boolean flag1=false;
                for(String s:denyPatternSet){
                    if(s.contains(".*")){
                        String[] al = s.split("\\.\\*");
                        boolean flag=true;
                        StringBuilder sBuilder=new StringBuilder();
                        for(String e: al) {
                            sBuilder.append(e+"|");
                            Matcher pa=Pattern.compile(e).matcher(phaseList[i]);
                            if (!pa.find()) {
                                flag=false;
                                break;
                            }
                        }
                        if(flag)//����
                        {
                            String temp=phaseList[i];
                            if (temp.replaceAll(sBuilder.substring(0,sBuilder.length()-1),"").length()==0)//��������
                             flag1 = true;
                        }
                    }else{
                        Matcher pa=Pattern.compile(s).matcher(phaseList[i]);
                        if(pa.matches()) {
                           flag1=true;
                        }
                    }
                }
                if(!flag1)
                    sb.append(classList[i]+"|");
            }
            if(sb.length()>0)
                type=sb.substring(0,sb.length()-1);
            //����ȥ��ģʽ>>
        }
        return type ;// return type + "\t" + line; �޸�
    }

    //����ֵ��һ��ƥ����һ��classʱ��ƥ�䵽ĳ����ģʽ�õĴ����+'\t'+ƥ���ϵ�pattern
    private static String isKeyType(String regex, String line) {
        String[] regexs = regex.split("@");
        for(String s : regexs){
            //System.out.println("debug s regexs"+s);
            StringBuilder sb=new StringBuilder();
            if(s.contains(".*")){
                String[] al = s.split("\\.\\*");
                boolean flag = true;
                for(String e: al) {
                    //System.out.println("debug"+e);
                    Matcher pa=Pattern.compile(e).matcher(line);
                    if (!pa.find()) {
                        flag = false;
                        break;
                    }
                    else
                        sb.append(line.substring(pa.start(),pa.end()));
                }
                if(flag) {
                    //System.out.println("debug"+s);
                    sb.append("\t"+s);  //ƥ�䵽ĳ����ģʽ�õĴ����+'\t'+ƥ���ϵ�pattern
                    return sb.toString();
                }
            }else{
                Matcher pa=Pattern.compile(s).matcher(line);
                if(pa.find()) {
                    //System.out.println("debug"+s);
                    return line.substring(pa.start(),pa.end())+"\t"+s;
                }
            }
        }
        return "";
    }
    public static void main(String[] args) throws Exception
    {
        //just for test
        //Vector<String> vTemp=new Vector<>();vTemp.add("����.*��ֲ");
        //pattern_to_deny_pattern.put("��ֲ.*Ͷ��",vTemp);
        //just for test

        loadPattern("dat/�������ģʽ.txt");
        treeCount=new myTreeKount("dat/�������ģʽ.txt");
        processCluster("dat/prase.in","dat/prase_out.txt");

        File fileOut = new File("dat/������������ҵ.txt");
        fileOut.createNewFile();
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        BufferedWriter bw = new BufferedWriter(osw);
        //BufferedWriter bw=new BufferedWriter(new FileWriter("dat/������������ҵ.txt"));

        System.out.println("������������ҵ");
        bw.write("//������������ҵ\n");
        for(String str:keyList) {
            if (count2.containsKey(str)) {
                bw.write(str + "\t" + count2.get(str) + "\n");
            }
        }
        bw.close();

        fileOut = new File("dat/����������ҵ.txt");
        fileOut.createNewFile();
        osw = new OutputStreamWriter(new FileOutputStream(fileOut));
        bw = new BufferedWriter(osw);

        bw.write("//����������ҵ\n");
        System.out.println("����������ҵ");
            for(String str:keyList) {
                if (count3.containsKey(str)) {
                    bw.write(str + "\t" + count3.get(str) + "\n");
                }
            }
        bw.close();
    }
}

