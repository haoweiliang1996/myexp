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

/**
 * �Ǻ���˳��
 * @author Administrator
 *
 */
public class Category2 {

	public static HashMap<String,String> patternMap = new HashMap<>();
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		loadPattern("dat/�������ģʽ.txt");
		processCluster("dat/stock_cluster_result.txt", "dat/stock_cluster_unorder.txt");
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
					String key = line.substring(0, index);
					String value = line.substring(index+1).trim();
			
					value = value.replaceAll("\\+", "");
					value = value.replaceAll("\\*", ".*");
					//����Դ��� W1|W2*W3|W4������
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
							//��ͬ��ģʽ��ļ������@
							sb.append("@");
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
					//line.replaceAll("\\t+", "��");
					line = line.trim();
					line = line.replaceAll("\\s+", "��");
					line = line.replaceAll("����", "��");
					line = line.replaceAll("������", "��");
					String result = type(line).trim();
					result = "\t" + result;
					//if(!resultSet.contains(result)){
						rList.add(result);
						//resultSet.add(result);
					//}
					continue;
				}
				if (line.isEmpty()) 
					continue;
				rList.add(line);
			}
			
			for(String e : rList)
				bw.write(e + "\n");
			/*for(String e : rList)
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
			
			if (isKeyType(regex,line)){
				if(sb.indexOf(key) == -1)
					sb.append(key + "|");
			}
		}
		String type= "�����";	
		if (sb.length() != 0)
			type = sb.substring(0,sb.length() - 1);	
		return type + "\t" + line;
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
				if(flag)
					return true;
			}else{
				if(line.contains(s))
					return true;
			}
		}
		return false;
	}
	
}
