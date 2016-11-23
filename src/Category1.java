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


public class Category1 {

	public static HashMap<String,String> patternMap = new HashMap<>();
	
	public static void main(String[] args) throws Exception {
		loadPattern("dat/�������ģʽ.txt");
		processCluster("dat/stock_cluster_result.txt", "dat/stock_cluster_category.txt");
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
					int index = line.indexOf(" ");
					String key = line.substring(0, index);  //�ո�ǰ������Ϊ���
					String value = line.substring(index+1).trim(); //�ո��������Ϊ���ֵ
					value = value.replaceAll("\\+", "");  //ȥ���Ӻ�
					value = value.replaceAll("\\*", ".*"); //��*�滻Ϊ.*

                    //����Դ��� W1|W2*W3|W4������
                    String[] vauleArray=value.split("\\s+");
                    StringBuffer sb=new StringBuffer();
                    for (int i=0;i<vauleArray.length;i++){
                        String[] phaseArrary=vauleArray[i].split(".\\*");
                        for (int j=0;j<phaseArrary.length;j++) {
                            String s = phaseArrary[j];

                            if (s.contains("|"))
                                s = "(" + s + ")";
                            if (j != phaseArrary.length - 1)
                                s += ".*";
                            System.out.println(s);
                            sb.append(s);
                        }
                        if(i!=vauleArray.length-1)
                            sb.append("|");
                    }
                    if (sb.length()>0) {
                        value = sb.toString();
                        System.out.println(value);
                    }
                    if(patternMap.containsKey(key))
                        value = patternMap.get(key)+ "|" + value;
                    patternMap.put(key, value);
                }
            }
			br.close();
			System.out.print(patternMap);
		}
		else {
			System.out.println("�Ҳ���ָ�����ļ�");
		}
	}

	public static void processCluster(String inFile, String outFile)
			throws Exception {
		HashSet<String> resultSet = new HashSet<>();
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
				if(!line.isEmpty()&&!line.startsWith("Cluster")){
					String result = type(line).trim();
					//if(!resultSet.contains(result)){
						rList.add(result);
					//	resultSet.add(result);
					//}
					continue;
				}
				rList.add(line);
			}
			
			for(String e : rList)
				bw.write(e + "\n");
		/*	for(String e : rList)
				if(e.startsWith("�����") && e.length() > 20){
					bw.write(e + "\n");
				}*/
			
			br.close();
			bw.close();
		}
		else {
			System.out.println("�Ҳ���ָ�����ļ�");
		}
	}
	
	private static String type(String line) {
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<String, String>> iter = patternMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String regex = entry.getValue();
			if (Pattern.compile(regex).matcher(line).find()){
				if(sb.indexOf(key) == -1)
					sb.append(key + "|");
			}
		}
		String type= "�����";	
		if (sb.length() != 0)
			type = sb.substring(0,sb.length() - 1);	
		return type + "\t" + line;
	}
	
}
