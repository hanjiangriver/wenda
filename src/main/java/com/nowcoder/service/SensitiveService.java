package com.nowcoder.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 张汉江 on 2017/8/18
 */
@Service
public class SensitiveService implements InitializingBean {

    private  static  final Logger logger= LoggerFactory.getLogger(SensitiveService.class);
    /**
     * 默认敏感词替换符
     */
    private static final String DEFAULT_REPLACEMENT = "敏感词";
    /*
    implements trinode
     */
    private class  TrieNode{
        //关键词是否终结
       private boolean end=false;
        //存放 下面的子节点
        private Map<Character,TrieNode>subNodes=new HashMap<>();
        //添加子节点
        public  void addSubNode(Character key,TrieNode node){
            subNodes.put(key,node);
        }
        //获得子节点
        public TrieNode getSubNode(Character key){
            return subNodes.get(key);
        }
        boolean isKeywordEnd() {
            return end;
        }
        public void setKeywordEnd(boolean end){
           this.end=end;
        }
        public int getSubNodeCount() {
            return subNodes.size();
        }
    }

    private  TrieNode rootNode=new TrieNode();//根节点

    /**
     * 判断是否是一个符号
     */
    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    //添加节点
    public void addWord(String str){
        TrieNode tempNode=rootNode;
        for(int i=0;i<str.length();i++){
            Character c=str.charAt(i);
            if(isSymbol(c)){//过滤空格
                continue;
            }
            TrieNode node=tempNode.getSubNode(c);
            if(node==null){
                node=new TrieNode();
                tempNode.addSubNode(c,node);
            }
            tempNode=node;
            if(i==str.length()-1){    //结束
                tempNode.setKeywordEnd(true);
            }
        }

    }
    /*
      过滤敏感词
     */
    public String filter(String str){
        if (StringUtils.isBlank(str)) {
            return str;
        }
        String replacement = DEFAULT_REPLACEMENT;
        StringBuilder sb=new StringBuilder();
        int begin=0;  //开始位置
        int postion=0;
        TrieNode tempNode=rootNode;//根节点
        while(postion<str.length()){
            Character c=str.charAt(postion);
            // 空格直接跳过
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    sb.append(c);
                    ++begin;
                }
                ++postion;
                continue;
            }
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null){//不是敏感词
                sb.append(str.charAt(begin));
                postion=begin+1;
                begin=postion;
                tempNode=rootNode;//回到根节点重新开始
            }else if(tempNode.isKeywordEnd()){//是最后一个节点 替换
                sb.append(replacement);
                tempNode=rootNode;
                postion=postion+1;
                begin=postion;
            }else {
                postion++;
            }
        }
        sb.append(str.substring(begin));

        return sb.toString();


    }
    @Override
    public void afterPropertiesSet() throws Exception {
      //  rootNode=new TrieNode();
        BufferedReader br=null;
        try {
            InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader isr=new InputStreamReader(is);
             br=new BufferedReader(isr);
            String str;
            while ((str=br.readLine())!=null){
                str=str.trim();
                addWord(str);
            }
        }catch (Exception e){
            logger.error("读取敏感词失败"+e.getMessage());
        }finally {
            br.close();
        }
    }
    public static void main(String[] argv) {
        SensitiveService s = new SensitiveService();
        s.addWord("色情");
        s.addWord("好色");
        System.out.print(s.filter("你好*色**情XX"));

    }
}
