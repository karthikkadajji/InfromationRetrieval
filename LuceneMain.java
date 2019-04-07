package IR;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneMain {
//	Variable declaration for getting values from command line
	String indexPath;
	String docsPath;
	String query;
	String rankingModel;
	
	public static void main(String[] args) throws IOException {
		LuceneMain l = new LuceneMain();
		int length_of_arguement = args.length;
//		Checking if arguments have been provided or not. If not then will exit
		if (length_of_arguement==0) {
			System.out.println("Please provide necessary arguments");
			System.exit(0);
		}
//		Checking if required number of arguments have been provided or not
		else if(length_of_arguement < 4) {
			System.out.println("Not enough arguments provided. There should be at least 4 arguments.");
			System.exit(0);
			
		}
//		Initialize variables if everything is good
		else {
			l.docsPath = args[0];
			l.indexPath = args[1];
			l.rankingModel = args[2];
			StringBuilder sb = new StringBuilder();
			for (int i=3; i < length_of_arguement; i++)
			{
				l.query = sb.append(args[i]).append(" ").toString();
			}
			l.query = l.query.trim();
		}
//		create bool variable for setting configuration of index writer
	    boolean create = true;
//	    Initialization of document path and indexpath
	    final Path docDir = Paths.get(l.docsPath);
	    Directory dir = FSDirectory.open(Paths.get(l.indexPath));
//	    Create an analyzer for indexer configuration
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
//	    Checking the model value and setting index configuration
	    if(l.rankingModel.equalsIgnoreCase("VS"))
	    	iwc.setSimilarity(new ClassicSimilarity());
	    else if(l.rankingModel.equalsIgnoreCase("OK"))
	    	iwc.setSimilarity(new BM25Similarity());
	    else {
	    	System.out.println("Please enter valid model which is OK or VS");
	    	System.exit(0);
	    }
	    if (create) {
	    	iwc.setOpenMode(OpenMode.CREATE);
	      } 
	    else {
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }
//	    Create an index writer object
	    IndexWriter writer = new IndexWriter(dir, iwc);
//	    Calling static function of IndexFiles.java for indexing
	    IndexFiles.indexDocs(writer, docDir);
		writer.close();
		System.out.println("Indexing has been completed. Now proceeding to search.");
		try {
			SearchFiles.perform_search(l.indexPath, l.query, l.rankingModel);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
			
	}

}


