package com.renren.search.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import com.renren.search.util.WordSegDict;

public class ThreadSafeAresAnalyzer extends Analyzer{
	private static ThreadLocal<AresAnalyzer> analyzer = new ThreadLocal<AresAnalyzer>(){
		protected synchronized AresAnalyzer initialValue(){
			return new AresAnalyzer();
		}
	};
	
	public ThreadSafeAresAnalyzer(){
		WordSegDict.getInstance();
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader in) {
		return analyzer.get().tokenStream(fieldName, in);
	}

	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader in)
			throws IOException {
		return analyzer.get().reusableTokenStream(fieldName, in);
	}
	
	@Override
	public void close(){
		analyzer.get().close();
		analyzer.remove();
	}
	
}
