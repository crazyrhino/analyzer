package com.renren.search.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import com.renren.search.util.WordSeg;
import com.renren.search.util.WordSegDict;

//本分词程序对String大小写，一律转换为大写
public class AresAnalyzer extends Analyzer {
	private WordSeg wordseg = new WordSeg();

	public AresAnalyzer() {
		// path是词库路径，需要在这里设置
		WordSegDict wsdict = WordSegDict.getInstance();
		wordseg.init(wsdict);
	}

	public AresAnalyzer(String path) {
		WordSegDict wsdict = WordSegDict.getInstance(path);
		wordseg.init(wsdict);
	}

	public AresAnalyzer(WordSegDict wsdict) {
		wordseg.init(wsdict);
		// path是词库路径，需要在这里设置
	}

	// 创建一个TokeStream，能够对reader提供的所有文本进行分词。返回的TokenStream对象就用来递归处理所有的词汇单元
	@Override
	public TokenStream tokenStream(String fieldName, Reader in) {
		return new AresTokenizer(in, wordseg);
		// return new AresTokenizer(in, null);
	}

	// 创建一个TokenStream，为同一个线程重复使用，节省时间
	// 该方法是一个可选方法
	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader in)
			throws IOException {
		AresTokenizer tokenizer = (AresTokenizer) getPreviousTokenStream();
		if (tokenizer == null) {
			tokenizer = new AresTokenizer(in, wordseg);
			setPreviousTokenStream(tokenizer);
		} else
			tokenizer.reset(in);
		return tokenizer;
	}

	// 释放analyzer占用的资源
	@Override
	public void close() {
		super.close();
		wordseg.destroy();
	}
}
