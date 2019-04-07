package IR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

public class SearchFiles {

	public static void perform_search(String indexPath, String q, String rankingModel) throws IOException, ParseException {

		String[] fields = {"Title", "contents"};

		final int maxHits = 10; //is total number that needs to be displayed
//		Initialization of reader to read from index directory
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
//		Initialization of searcher
		IndexSearcher searcher = new IndexSearcher(reader);

//		Initialize ranking model for search
		if (rankingModel.equalsIgnoreCase("OK")) {
			BM25Similarity similarity = new BM25Similarity();
			searcher.setSimilarity(similarity);
			
		} else if(rankingModel.equalsIgnoreCase("VS")) {
			ClassicSimilarity sim = new ClassicSimilarity();
			searcher.setSimilarity(sim);
		}
		
		else { 
			System.out.println("Please enter valid Ranking Model");
			System.exit(0);
		}

		Analyzer analyzer = new StandardAnalyzer();
//		MultifieldQueryParser to search in multiple fields which is contents and title
		MultiFieldQueryParser mfparser = new MultiFieldQueryParser(fields, analyzer);

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
//		Applying stemming to the query
		PorterStemmer p = new PorterStemmer();
		StringBuilder q1 = new StringBuilder();
		StringTokenizer tokens = new StringTokenizer(q);
	    int len = tokens.countTokens();
	    for( int i=0; i<len;i++) {
		    	p.setCurrent(tokens.nextToken());
		    	p.stem();
	    	q1.append(p.getCurrent()+" ");
	    	
	    }
		Query query = mfparser.parse(q1.toString());
		
		System.out.println("\nSearching For: " + q + "\n");
//		Calling the method for search
		doSearch(in, searcher, query, maxHits);
//		Finally close the reader
		reader.close();

	}

	public static void doSearch(BufferedReader in, IndexSearcher searcher, Query query,
			int maxHits) throws IOException {
			TopDocs results = searcher.search(query, maxHits);
			ScoreDoc[] hits = results.scoreDocs;
			int total_number_of_hits = Math.toIntExact(results.totalHits);
			
//			Getting minimum value between total number of hits and maximum hits which is 10
			int minimum_matched_documents = Math.min(maxHits, total_number_of_hits);
			
//			Iteration for display of results
			for (int i = 0; i < minimum_matched_documents; i++) {
				String format_file = null;
				String title;
				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("path");
				if(path.lastIndexOf(".") != -1 && path.lastIndexOf(".") != 0)
			         format_file =  (path.substring(path.lastIndexOf(".")+1));
//				Check if file is txt and then display filename
				if(format_file.equals("txt")) {
					 title = doc.get("Filename");
					
				}
//				If it is html then display title of the document
				else
					title = doc.get("Title");
				double score = 	hits[i].score;

				if (path != null) {
					System.out.println((i+1) + ". " + title);

					// prints the path of the document
					System.out.println("   Path: " + path);

					// prints document score
					System.out.println("   Score: " + score + "\n");
					
				}
				else {
					System.out.println("No path exists for " + (i+1));
				}
		}
	}	
}
