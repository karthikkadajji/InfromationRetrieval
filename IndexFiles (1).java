package IR;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.jsoup.Jsoup;

public class IndexFiles {
	
	/** Index all files under a directory. */
	public static void indexDocs(final IndexWriter writer, Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
        	  String s = getFileExtension(file);
        	  if (s.equals("txt"))
        	  {	  	    		
      			indexDoc_txt(writer, file);
        	  }
        	  else if ( s.equals("html")) {
        		  indexDoc_html(writer,file);
        	  }
        	  else {
        		  System.out.println("File with extension " +s +"won't be indexed");
        	  }
  	    	
          } catch (IOException ignore) {
            // don't index files that can't be read.
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
     System.out.println("Please enter valid document directory");
     System.exit(0);
    }
  }

  /** Indexes a single document */
  static void indexDoc_txt(IndexWriter writer, Path file) throws IOException {
    try (InputStream stream = Files.newInputStream(file)) {
      Document doc = new Document();
      Field pathField = new StringField("path", file.toString(), Field.Store.YES);
      Field pathField1 = new StringField("Filename", file.getFileName().toString(), Field.Store.YES);
      doc.add(pathField);
      doc.add(pathField1);
      
    
      CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
		
      Analyzer analyser = new StandardAnalyzer();
      TokenStream tokenstream = new StandardTokenizer();
      tokenstream = analyser.tokenStream("contents", new FileReader(file.toString()));
      tokenstream = new StopFilter(tokenstream, stopWords);
      tokenstream = new PorterStemFilter(tokenstream);
      analyser.close();
      doc.add(new TextField("contents", tokenstream));
			
		
      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
        // New index, so we just add the document (no old document can be there):
//        System.out.println("adding " + file.getFileName().toString());
        writer.addDocument(doc);

        System.out.println(doc.get("Filename"));
      } else {
        writer.updateDocument(new Term("path", file.toString()), doc);
        writer.updateDocument(new Term("filename",file.getFileName().toString()),doc);
      }
    }
    
  }
 static void indexDoc_html(IndexWriter writer, Path file) throws IOException {
	 try (InputStream stream = Files.newInputStream(file)) {
//	      Create a document object for storing indexing fields
	  Document doc = new Document();
	  Field pathField = new StringField("path", file.toString(), Field.Store.YES);
	  
	  File input1 = new File(file.toString());
	  org.jsoup.nodes.Document doc1 = Jsoup.parse(input1, "UTF-8", "");
	  String text = doc1.body().text();
	  String title = doc1.title();
	  Field pathField1 = new StringField("Title", title, Field.Store.YES);
	  StringBuilder html_search = new StringBuilder();
	  html_search.append(text);
	  html_search.append(title);
      doc.add(pathField);
      doc.add(pathField1);
      CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
      Analyzer analyser = new StandardAnalyzer();
      TokenStream tokenstream = new StandardTokenizer();
      tokenstream = analyser.tokenStream("contents", html_search.toString());
      tokenstream = new StopFilter(tokenstream, stopWords);
      tokenstream = new PorterStemFilter(tokenstream);
      analyser.close();
      doc.add(new TextField("contents", tokenstream));
      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
        // New index, so we just add the document (no old document can be there)
        writer.addDocument(doc);

        System.out.println(doc.get("Title"));
      } else {
//        Existing index (an old copy of this document may have been indexed) so 
//        we use updateDocument instead to replace the old one matching the exact 
//        path, if present:
//    	System.out.println("updating " + file.getFileName().toString());
        writer.updateDocument(new Term("path", file.toString()), doc);
        writer.updateDocument(new Term("filename",file.getFileName().toString()),doc);
      }
    }
    
  }

//Function to get extension of the file  
 public static String getFileExtension(Path f){
	String name_of_file = f.getFileName().toString();
	if(name_of_file.lastIndexOf(".") != -1 && name_of_file.lastIndexOf(".") != 0)
        return (name_of_file.substring(name_of_file.lastIndexOf(".")+1));
    else return "";
	}
}






































































































