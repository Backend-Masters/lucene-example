package com.khjeon.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Lucene Index & Search Example
 *
 * 이상한나라의 엘리스 Book을 index & searching 함
 *
 * Alice's Adventures in Wonderland by Lewis Carroll : https://www.gutenberg.org/ebooks/11
 *
 * reference : http://www.lucenetutorial.com/lucene-in-5-minutes.html
 */
public class SimpleDemo {

	public static void main(String[] args) throws Exception {

		InputStream is = SimpleDemo.class.getClassLoader().getResourceAsStream("11-0.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		List<String> list = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			list.add(line);
		}
		reader.close();
		System.out.println("Lines : " + list.size());


		/**
		 *   1. Indexing
		 *   Directory : 색인은 색인 절차를 거쳐, 어딘가에 저장해야 함. Directory가 색인 저장을 담당
		 *   Analyzer : 루씬 자체는 일반 텍스트만을 색인함. 분석하도록 지정한 모든 텍스트는 Analyzer(분석기)를 거침
		 *     - 즉 텍스트의 단어를 루씬에서 활용할수 있는 토큰으로 분리
		 *   Document : 색인에 추가할 데이터를 저장하는 단위.
		 *   Field : 색인에 추가하려는 내용은 Document 인스턴스에 Field 형대로 추가
		 *     - TextField vs StringField
		 *       + TextField : contents, article 등의 문맥이 있는 글, 형태분석 필요값
		 *       + StringField : 그 자체로서의 값(ID, email, url등)
		 */

		Directory directory = new RAMDirectory();
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(standardAnalyzer);
		IndexWriter writer = new IndexWriter(directory, config);

		for(int i=0; i<list.size(); i++) {
			Document doc = new Document();
			doc.add(new TextField("text", list.get(i), Field.Store.YES));
			doc.add(new StringField("line", String.valueOf(i), Field.Store.YES));
			writer.addDocument(doc);
		}
		writer.close();

		search("pretty", standardAnalyzer, DirectoryReader.open(directory));
		search("beautiful", standardAnalyzer, DirectoryReader.open(directory));
		search("she", standardAnalyzer, DirectoryReader.open(directory));
	}

	public static void search(String queryStr, Analyzer analyzer, DirectoryReader directoryReader) throws Exception {
		Query q = (new QueryParser("text", analyzer)).parse(queryStr);

		IndexSearcher searcher = new IndexSearcher(directoryReader);

		TopDocs topDocs = searcher.search(q, 10);
		System.out.println("Query : " + queryStr + " / TOTAL HITS : " + topDocs.totalHits);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;

		for(int i=0; i<scoreDocs.length; i++) {
			int docId = scoreDocs[i].doc;
			Document d = searcher.doc(docId);
			String outString = String.format("Found Doc - line : %s , text : %s", d.get("line"), d.get("text"));
			System.out.println(outString);
		}
	}
}
