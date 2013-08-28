package com.renren.search.analyzer.standard;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class AresStandardAnalyzer extends Analyzer {

	public AresStandardAnalyzer() {
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader in) {
		return new AresStandardTokenizer(in);
	}

	// 创建一个TokenStream，为同一个线程重复使用，节省时间
	// 该方法是一个可选方法
	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader in)
			throws IOException {
		AresStandardTokenizer tokenizer = (AresStandardTokenizer) getPreviousTokenStream();
		if (tokenizer == null) {
			tokenizer = new AresStandardTokenizer(in);
			setPreviousTokenStream(tokenizer);
		} else
			tokenizer.reset(in);
		return tokenizer;
	}

	public static void main(String[] args) throws IOException {
		String standard = "【杏】<img src='http://a.xnimg.cn/imgpro/emotions/tie/2.gif?ver=1' alt='谄笑'><img src='http://a.xnimg.cn/imgpro/emotions/tie/2.gif?ver=1' alt='谄笑'> 1、日前，中共中央决定：周永康同志不再担任中央政法委书记职务；孟建柱同志兼任中央政法委书记。 2、美国总统奥巴马11月19日对缅甸展开了历史性访问，使其成为首位在位期间出访该国的美国总统。3、昨日国家统计局公布的10月份70个大中城市住宅销售价格变动情况，环比下降的城市已经高达34个。";

		AresStandardAnalyzer analyz = new AresStandardAnalyzer();

		TokenStream stream = analyz.reusableTokenStream("title",
				new StringReader(standard));

		TermAttribute termAtt1 = stream.addAttribute(TermAttribute.class);
		OffsetAttribute offsetAtt1 = stream.addAttribute(OffsetAttribute.class);
		PositionIncrementAttribute position1 = stream
				.addAttribute(PositionIncrementAttribute.class);
		TypeAttribute typeAttr1 = stream.addAttribute(TypeAttribute.class);
		stream.reset();

		for (boolean next = stream.incrementToken(); next; next = stream
				.incrementToken()) {
			System.out.println("offset=<" + offsetAtt1.startOffset() + ","
					+ offsetAtt1.endOffset() + ">\tterm=$$" + termAtt1.term()
					+ "$$");
		}

	}

}
