package glasswareserviceimpl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import glasswaredata.FaceBook;
import glasswaredata.User;
import glasswareservice.UserService;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class FaceBookServiceImpl implements UserService {

	private FaceBook user;
	
	public User getInfo(String name){
		//WHATEVER PRIVATE PROCESSING YOU NEED
		String html = getHtml(name);
		//do something to populate stuff from html to actual object
		this.user = htmlparser(html);
		return this.user;
	}
	
	public String getHtml(String name) {
        try {
        	String encodedname = URLEncoder.encode(name,"UTF-8");
        	String url = "http://www.facebook.com/search.php?q=" + encodedname + "&amp;_fb_noscript=1";
			return Scraper.executeGet(url);
		} catch (UnsupportedEncodingException e) {
			System.out.println("could not encode parameters");
			e.printStackTrace();
	        return null;
		}
    }
	
	public FaceBook htmlparser(String html){
		Document doc = Jsoup.parse(html);		
		Elements the_elements = doc.getElementsByClass("pls");
		Element first_result = the_elements.first();
		FaceBook user = new FaceBook("no FaceBook account found");
		
		if (first_result != null){
			String[] tokens = first_result.text().split("[ ]+");
			
			if(tokens.length > 1) 
				user.setName(tokens[0] + " " + tokens[1]);
			
			String about = "";
			for(int i = 2; i < tokens.length; i++)
				about += tokens[i] + " ";
			user.setAbout(about);
			
			Element image = doc.select("img").first();
			String url = image.absUrl("src");
			user.setImg_src(url);
		}
		
		return user;
	}
}
