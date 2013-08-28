package com.renren.search.analyzer.tc;

import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import com.renren.search.analyzer.standard.AresStandardTokenizer;
import com.renren.search.analyzer.tc.util.WordSeg;
import com.tencent.research.nlp.SWIGTYPE_p_void;
import com.tencent.research.nlp.TCWordSeg;

//本分词程序对String大小写，一律转换为大写
public class AresTCAnalyzer extends Analyzer {
	private static final Logger logger = Logger.getLogger(AresTCAnalyzer.class);
	private int mode;
	private SWIGTYPE_p_void handle = null;

	public AresTCAnalyzer() {
		// path是词库路径，需要在这里设置
		WordSeg.init();
		this.mode =  TCWordSeg.TC_POS | TCWordSeg.TC_S2D
				| TCWordSeg.TC_U2L | TCWordSeg.TC_T2S | TCWordSeg.TC_ENGU
				| TCWordSeg.TC_CN;
		handle = TCWordSeg.TCCreateSegHandle(this.mode);
	}

	public AresTCAnalyzer(int mode) {
		WordSeg.init();
		this.mode = mode;
	}

	// 创建一个TokeStream，能够对reader提供的所有文本进行分词。返回的TokenStream对象就用来递归处理所有的词汇单元
	@Override
	public TokenStream tokenStream(String fieldName, Reader in) {
		return new AresTCTokenizer(in, handle);
	}

	// 创建一个TokenStream，为同一个线程重复使用，节省时间
	// 该方法是一个可选方法
	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader in)
			throws IOException {
		AresTCTokenizer tokenizer = (AresTCTokenizer) getPreviousTokenStream();
		if (tokenizer == null) {
			tokenizer = new AresTCTokenizer(in, handle);
			setPreviousTokenStream(tokenizer);
		} else
			tokenizer.reset(in);
		return tokenizer;
	}

	// 释放analyzer占用的资源
	@Override
	public void close() {
		super.close();
		//WordSeg.destroy();
		if(handle != null) {
			TCWordSeg.TCCloseSegHandle(handle);
		}
		TCWordSeg.TCUnInitSeg();
	}
}
