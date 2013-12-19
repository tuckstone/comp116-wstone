package glasswareserviceimpl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import glasswaredata.FaceBook;
import glasswaredata.Twitter;
import glasswaredata.User;
import glasswareservice.UserService;

public class TwitterServiceImpl implements UserService {

	private Twitter user;
	
	public User getInfo(String name){
		this.user = new Twitter(name);
		//WHATEVER PRIVATE PROCESSING YOU NEED
		return this.user;
	}
	
	public String getHtml(String name) {
        try {
        	String encodedname = URLEncoder.encode(name,"UTF-8");
        	String url = "https://twitter.com/search?q=" + encodedname + "&src=typd";
			return Scraper.executeGet(url);
		} catch (UnsupportedEncodingException e) {
			System.out.println("could not encode parameters");
			e.printStackTrace();
	        return null;
		}
    }
	
	public Twitter htmlparser(String html){
		Document doc = Jsoup.parse(html);		
		Elements the_elements = doc.getElementsByClass("js-tweet-text");
		Element first_result = the_elements.first();
		
		Twitter user = new Twitter("no FaceBook account found");
		user.setAbout(first_result.text());		
		return user;
	}
	
}
