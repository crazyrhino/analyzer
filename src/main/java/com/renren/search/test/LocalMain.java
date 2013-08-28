package com.renren.search.test;
import java.io.File;
import java.io.IOException;

import java.io.StringReader;


import org.apache.lucene.analysis.Analyzer;
import org.apache.log4j.*;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.renren.search.analyzer.AresAnalyzer;
import com.renren.search.analyzer.ThreadSafeAresAnalyzer;
import com.renren.search.util.WordSeg;
import com.renren.search.util.WordSegDict;



public class LocalMain {
	
	private static class TestClient extends Thread{
		private Analyzer analyzer;
		private String res;
		public TestClient(Analyzer analyzer, String res){
			this.analyzer = analyzer;
			this.res=res;
		}
		public void run() {  
			TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(res));
			TermAttribute term = tokenStream.addAttribute(TermAttribute.class);
			OffsetAttribute offset = tokenStream.addAttribute(OffsetAttribute.class);
			TypeAttribute type = tokenStream.addAttribute(TypeAttribute.class);
			PositionIncrementAttribute posIncr = tokenStream.addAttribute(PositionIncrementAttribute.class);
			try {
				while(tokenStream.incrementToken()) {	
					System.out.println("[" + term.term() + "] " + ": " + offset.startOffset() + "->" + offset.endOffset() + ": " + type.type() + ": " + posIncr.getPositionIncrement());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	

	public static void main(String[] args) throws IOException, ParseException {
		String path = "/home/xiaobing/dev/SearchEngine_branch/AresAnalyzer/32bit";
		System.setProperty(WordSegDict.SystemPropertyKey, path);
		System.out.println("path: " + path);
		System.out.println(File.separator);
		//WordSegDict wsdict = WordSegDict.getInstance(path);
        //wsdict.load(path + "/");
        //wsdict.init(path + "/data/");
       
		String res = "香港电影中 的	十大武侠金曲  @56网搞笑  kuaileDE推荐 我我的 我我的我我的我我的我我的我我的我我的我我的我我我的的我的我的我的我的我的";
		String res2= "搜索引擎系统使用icegrid+icebox 管理broker 和 searcher， ice registry 提供location service, ice node 实际运行Broker/Search Service,通过使用icegrid admin 来部署和管理";
		System.out.println(res.length());
//		String res = "我我的 我我的我我的我我的我我的我我的我我的我我的我我我的的我的我的我的我的我的我的我的的";
		//res = res.replaceAll("\\s+", " ");
		Analyzer analyzer = new ThreadSafeAresAnalyzer();
		QueryParser qp=new QueryParser(Version.LUCENE_30,"test",analyzer);
		Query q = qp.parse(res2);
		System.out.println(q);
		/*TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(res));
		TermAttribute term = tokenStream.addAttribute(TermAttribute.class);
		OffsetAttribute offset = tokenStream.addAttribute(OffsetAttribute.class);
		TypeAttribute type = tokenStream.addAttribute(TypeAttribute.class);
		PositionIncrementAttribute posIncr = tokenStream.addAttribute(PositionIncrementAttribute.class);
		while(tokenStream.incrementToken()) {	
			System.out.println("[" + term.term() + "] " + ": " + offset.startOffset() + "->" + offset.endOffset() + ": " + type.type() + ": " + posIncr.getPositionIncrement());
		}*/
		//System.out.println("hello lucene");
//		System.out.println(isWhitespace(' '));
//		System.out.println(isWhitespace('　'));
		TestClient t1 = new TestClient(analyzer,res);
		TestClient t2 = new TestClient(analyzer,res);
		TestClient t3 = new TestClient(analyzer,res);
		TestClient t4 = new TestClient(analyzer,res2);
		TestClient t5 = new TestClient(analyzer,res2);
		TestClient t6 = new TestClient(analyzer,res2);
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
	}
	
	 public static boolean isWhitespace(char ch){  
	        if(ch == '　'){  
	            return true;  
	        }  
	        if(Character.isWhitespace(ch)){  
	            return true;  
	        }  
	        return false;  
	    }  
	
}

