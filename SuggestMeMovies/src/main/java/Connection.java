import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.pengrad.telegrambot.model.Update;

public class Connection {
	
	private Model model;
	private String imdbDomain = "http://www.imdb.com";
	private String imdbSearch = "/search/title?adult=include&title_type=feature,tv_movie,documentary,short&title=";
	private String suggestDomain = "http://www.suggestmemovie.com";
	private String tryAgainMsg = "Unable to request data now, try again later!";
	private String noImg = "https://s-media-cache-ak0.pinimg.com/236x/f3/5a/d9/f35ad9427be01af5955e6a6ce803f5dc--artist-list-top-artists.jpg";
	
	//constructor
	public Connection(Model m){
		this.model = m; 
	}
	
	//SEARCH ON IMDB FOR SPECIFIC MOVIE
	public void searchImdb(Update update,String s) {			
		Document doc = null;
		int itemsLimit = 20;
		List<Map<String, String>> queryResults = new ArrayList<Map<String, String>>();
		String url = imdbDomain+imdbSearch+s.replaceAll(" ", "+");
		
		//site connection
		try {
			doc = Jsoup.connect(url).header("Accept-Language", "en").post();
		} catch (IOException e) {
			model.notifyObservers(update.message().chat().id(), tryAgainMsg,"",null);
		}
		
		//scrapping
		if(doc.selectFirst(".lister-item .lister-item-content") != null) {
			Elements elements = doc.select("div.lister-item");
			Element e = null;
			Map<String, String> data = null;
			int eSize = elements.size();
			if(eSize < itemsLimit) {
				itemsLimit = eSize; 
			}
			//iterate over elements
			for(int i = 0;i<itemsLimit;i++) {
				e = elements.get(i);
				data = new HashMap<String, String>();
				
				//title
				try{
					data.put("title",e.select("div.lister-item-content h3.lister-item-header > a").text());
				}catch(Exception ex){
					data.put("title","");
				}
				
				//imdbUrl
				try{
					data.put("imdbUrl",e.select("div.lister-item-image a").attr("href"));
				}catch(Exception ex){
					data.put("imdbUrl",imdbDomain);
				}
				
				//year
				try{
					data.put("year", e.select("div.lister-item-content h3.lister-item-header span.lister-item-year").text());
				}catch(Exception ex){
					data.put("year","");
				}
				
				//rating
				try {
					data.put("rating", e.select(".ratings-imdb-rating").text());
				}catch(Exception ex){
					data.put("rating","0");
				}
				
				//genre
				try {
					data.put("genre", e.select("p.text-muted span.genre").text());
				}catch(Exception ex){
					data.put("genre","Unknown");
				}
				
				//runtime
				try{
					data.put("runtime", e.select("p.text-muted span.runtime").text());
				}catch(Exception ex){
					data.put("runtime","");
				}
				
				//synopsis
				try {
					data.put("synopsis", e.select("div.lister-item-content p").get(1).text());
					if(data.get("synopsis").equals("Add a Plot")){
						data.put("synopsis", "");
					}
				}catch(Exception ex){
					data.put("synopsis","Unknown");
				}

				//director and cast
				try {
					String cast = e.select("div.lister-item-content p").get(2).text();
					if(cast.indexOf("|") != -1){
						data.put("director", cast.split("\\|")[0].replaceAll("Directors: ", "").replaceAll("Director: ", ""));
						data.put("cast", cast.split("\\|")[1].replaceAll("Stars:", "").replaceAll("Star:", ""));
					}else{
						if(cast.indexOf("Star") != -1) {
							data.put("cast", cast.replaceAll("Stars: ", "").replaceAll("Star: ", ""));
						}else if(cast.indexOf("Director") != -1){
							data.put("director", cast.replaceAll("Directors: ", "").replaceAll("Director: ", ""));							
						}
					}
				}catch(Exception ex){
					data.put("cast","Unknown");
					data.put("director","Unknown");
				}
				
				//poster
				try {
					data.put("poster", e.select(".loadlate").attr("loadlate").replaceAll("UX67_CR0,0,67,98_AL", "UX220_CR0,0,220,322_AL"));
				}catch(Exception ex){
					data.put("poster",noImg);
				}
			
				queryResults.add(data);
			}
			//show results to user
			model.notifyObservers(Long.valueOf(update.inlineQuery().id()), "","",queryResults);	
		}
	}
		
	//GET A RANDOM MOVIE SUGGESTION (BY FILTER OR NOT)
	public Map<String, String> getRandomMovie(Update update, Boolean filtered, String genreFilter,String person,String imdbusers,String imdbrating,String yearStart,String yearEnd) {		
		Document doc = null;
		Long chatId = null;
		if(update.message() != null) {
			chatId = update.message().chat().id();
		}else if(update.callbackQuery() != null){
			chatId = update.callbackQuery().message().chat().id();
		}
		Map<String, String> data = new HashMap<String, String>();
		Map<String, String> postData = new HashMap<String, String>();
		postData.put("mood_change", "1");
		postData.put("mood_category", genreFilter);
		postData.put("mood_imdb_users", imdbusers);
		postData.put("mood_year1", yearStart);
		postData.put("mood_year2", yearEnd);
		postData.put("mood_imdb_rating", imdbrating);
		postData.put("mood_extra1", person);
		
		//site connection
		if(filtered == true) {
			try {
				doc = Jsoup.connect(suggestDomain)
						.userAgent("Mozilla/5.0")
	                    .timeout(10 * 1000)
	                    .method(Method.POST)
	                    .data(postData)	
						.post();
			} catch (IOException e) {
				model.notifyObservers(chatId, tryAgainMsg,"",null);
			}
		}else {
			try {
				doc = Jsoup.connect(suggestDomain).get();
			} catch (IOException e) {
				model.notifyObservers(chatId, tryAgainMsg,"",null);
			}
		}
		
		//scrapping
		if(doc.selectFirst("h1") != null) {
			//title and year
			try {
				String t = doc.selectFirst("h1").html().toString();
				Matcher m1 = Pattern.compile("\\s\\([^\\d]*(\\d+)[^\\\\d]*\\)").matcher(t);
				if(m1.find()) {
					data.put("year", m1.group(1).toString());
				}
				data.put("title", t.split("\\(")[0].toString());
			}catch(Exception ex) {
				data.put("year","");
				data.put("title","Unknown");
			}
			//poster
			try{
				data.put("poster", suggestDomain+doc.select(".movie-tell img").attr("src").toString());
			}catch(Exception ex){
				data.put("poster", noImg);
			}
						
			//rating
			try{
				data.put("rating", doc.selectFirst(".meter-text-movie div > span").html().toString());
			}catch(Exception ex){
				data.put("rating", "0");
			}
			
			//imdbUrl
			try {
				data.put("imdbUrl", doc.select(".boxcontainer2 .movie-tell div").get(4).selectFirst("a[href]").attr("href").toString());
			}catch(Exception ex){
				data.put("imdbUrl", imdbDomain);
			}
			
			//genre
			try{
				String g = doc.select(".boxcontainer2 .movie-tell div").get(1).text().replaceAll(" ,",", ").split(":")[1].toString();
				data.put("genre", g.substring(0, g.lastIndexOf(",")));
			}catch(Exception ex) {
				data.put("genre", "Unknown");
			}
			
			//director
			try{
				String d = doc.select(".boxcontainer2 .movie-tell div").get(2).text().replaceAll(" ,",", ").split(":")[1].toString();
				data.put("director",  d.substring(0, d.lastIndexOf(",")));
			}catch(Exception ex){
				data.put("director", "Unknown");
			}
			
			//cast
			try{
				String c = doc.select(".boxcontainer2 .movie-tell div").get(3).text().replaceAll(" ,",", ").split(":")[1].toString();
				data.put("cast", c.substring(0, c.lastIndexOf(",")));
			}catch(Exception ex){
				data.put("cast", "Unknown");
			}
			
			//synopsis
			try{
				data.put("synopsis", doc.select(".boxcontainer2 .movie-tell div.content").html().toString());
			}catch(Exception ex){
				data.put("synopsis", "Unknown");
			}
			
			//trailer
			try{
				data.put("trailer", doc.select("div.video iframe").attr("src").toString());
			}catch(Exception ex){
				data.put("trailer", "");
			}
			return data;
		}else {
			return null;
		}
	}
}