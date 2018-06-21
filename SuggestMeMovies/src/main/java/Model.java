import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import com.vdurmont.emoji.EmojiParser;
import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;
import com.pengrad.telegrambot.model.Update;

public class Model implements Subject{
	
	private ResourceBundle messages = ResourceBundle.getBundle("locales.LabelsBundle",new Locale("en", "US"));;	
	private List<Observer> observers = new LinkedList<Observer>();
	private static Model uniqueInstance;
	private static Favorite lastMovie;
	private static List<String> genresAllowed = Arrays.asList("action","adventure","animation","biography","comedy","comedy","documentary","drama","family","fantasy","film_noir","history","horror","music","musical","mystery","news","romance","sci_fi","short","sport","thriller","war","western");	
	
	//bd de favoritos e usuarios
	ObjectContainer users = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "bd/users.db4o");
	
	
	private Model(){}
	
	public List<Favorite> getFavorites(long chatId) {
		Query query = users.query();
		query.constrain(User.class);
	    ObjectSet<User> allUsers = query.execute();
	    List<Favorite> favorites = new ArrayList<Favorite>();
	    for(User user:allUsers){
	    	if(user.getChatid() == chatId) {
    			favorites = user.getFavorites();
	    	}
	    }
	    return favorites;
	}
	
	public void removeFavorite(long chatId, String movieId) {
		Query query = users.query();
		query.constrain(User.class);
	    ObjectSet<User> allUsers = query.execute();
	    
	    for(User user:allUsers){
	    	if(user.getChatid() == chatId) {
	    		for (Favorite  favorite:user.getFavorites()) {
	    			if(favorite.getMovieId().equals(movieId)) {
	    				String tmp = favorite.getMovieTitle();
	    				user.getFavorites().remove(favorite);
		    			users.store(user.getFavorites());
		    			users.commit();
		    			
		    			this.notifyObservers(chatId, tmp+" "+messages.getString("sucessremove"),"","",null);
		    			break;
	    			}
	    		}
	    		break;
	    	}
	    }
	}
	public boolean addFavorite(long chatid) {
		
		Query query = users.query();
		query.constrain(User.class);
	    ObjectSet<User> allUsers = query.execute();
	    boolean addedFlag = false;
	    
	    for(User user:allUsers){
	    	if(user.getChatid() == chatid) {
	    		
	    		for (Favorite  favorite:user.getFavorites()) {
	    			if(favorite.getMovieId().equals(lastMovie.getMovieId())) {
	    				addedFlag = true;
	    				break;
	    			}
	    		}
	    		//add the last searched movie to favorites list
	    		if(addedFlag == false) {
	    			user.getFavorites().add(lastMovie);
	    			users.store(user.getFavorites());
	    			users.commit();
	    			
	    			this.notifyObservers(chatid, lastMovie.getMovieTitle()+" "+messages.getString("successfavadd"),"","",null);
	    		}else {
	    			this.notifyObservers(chatid, messages.getString("movieexists"),"","",null);
	    		}
	    		break;
	    	}
	    }
		
	    return false;
	}
	
	//adiciona usuario
	public boolean addUser(User user, long chatid){	
		if(isUserAvailable(user.getChatid())){
			user.setChatid(chatid);
			users.store(user);
			users.commit();
			return true;
		}
		return false;
	}
	
	public boolean isUserAvailable(long chatid){
		Query query = users.query();
		query.constrain(User.class);
	    ObjectSet<User> allUsers = query.execute();
	    
	    for(User user:allUsers){
	    	if(user.getChatid() == chatid) return false;
	    }
	    
	    return true;
	}
	
	public static String getToken() {
		return "508952083:AAE_eV8Wts_vOpUr-AdVetDl3auZxKUHHAo";
	}
	
	public static Model getInstance(){
		if(uniqueInstance == null){
			uniqueInstance = new Model();
		}
		return uniqueInstance;
	}
	
	//REGISTER OBSERVER
	public void registerObserver(Observer observer){
		observers.add(observer);
		
	}
	
	//NOTIFY ALL OBSERVERS
	public void notifyObservers(long chatId, String responseData, String responseImage, String responseItem, List<Map<String, String>> queryResult){
		for(Observer observer:observers){
			observer.update(chatId, responseData, responseImage, responseItem, queryResult);
		}
	}
	
	//CONVERT YOUTBE URL TO SHORTEN VERSION
	public String youtubeLink(String l){
		//https://youtu.be/0EHGiXxO_-I
	    l = l.replaceAll("youtube.com","youtu.be").replaceAll("embed/","").replaceAll("//www.","");
	    return l.substring(0, l.indexOf("?"));
	}
		
	//GET SUGGESTIONS AND SEND TO THE USER
	public void searchSuggestions(Update update, Boolean filtred, String filterType, String filterVal) {
		Long chatId = null;
		Boolean wrongValue = false;
		Map<String, String> list = null;
		String msgHeader = messages.getString("maybeyoulike");
		//opens a new connection to get the suggestion
		Connection c = new Connection(this);
		
		if(update.message() != null) {
			chatId = update.message().chat().id();
		}else {
			chatId = update.callbackQuery().message().chat().id();
		}		
		if(filtred == true) {
			
			//by director or cast
			if(filterType.equals("/sbc") || filterType.equals("/sbd")) {
				if(!filterVal.matches("[0-9]+")) {
					list = c.getRandomMovie(update, true, "", filterVal, "0", "0", "", "");
					
					if(filterType.equals("/sbd")) {
						msgHeader = msgHeader + " by <b>"+filterVal+"</b>";
					}else {
						msgHeader = msgHeader + " with <b>"+filterVal+"</b>";
					}
				}else {
					this.notifyObservers(chatId, messages.getString("onlynumbersmsg"),"","",null);
					wrongValue = true;
				}
				
			//by genre
			}else if(filterType.equals("/sbg")) {
				String g = filterVal.replaceAll("-", "_").toLowerCase();
				if(genresAllowed.contains(g)) {
					list = c.getRandomMovie(update, true,g,"","0","0","","");
					msgHeader = msgHeader + " "+messages.getString("inthe")+" <b>" + filterVal + "</b> "+messages.getString("category");
				}else {
					this.notifyObservers(chatId, messages.getString("typevalidcat"),"","",null);
					wrongValue = true;
				}
			//by rating
			}else if(filterType.equals("/sbr")) {
				if(filterVal.matches("[0-9]*\\.?[0-9]*")) {
					float r = Float.parseFloat(filterVal);
					if(r <= 0 || r > 95) {
						this.notifyObservers(chatId, messages.getString("typevalidrating"),"","",null);
						wrongValue = true;
					}else {
						if((float)r<(float)10) {r = r*10;}
						int v = (int)r;
						list = c.getRandomMovie(update, true,"","","0",String.valueOf(v),"","");
						msgHeader = msgHeader + messages.getString("with") + " <b>"+ ((float)r/(float)10) +"</b> "+messages.getString("oraboveimdb");
					}
				}else {
					this.notifyObservers(chatId, messages.getString("typeanumber"),"","",null);
					wrongValue = true;
				}
				
			//by decade	
			}else if(filterType.equals("/sby")) {
				if(filterVal.matches("-?\\d+")) {
					/*gets decade year*/
					int y = (Integer.parseInt(filterVal)/10)*10;
					if(y>1899) {
						list = c.getRandomMovie(update, true,"","","0","0",String.valueOf(y),String.valueOf(y+9));
						msgHeader = msgHeader + " "+ messages.getString("inthe")+" <b>"+y+"'s</b> "+messages.getString("period");
					}else {
						this.notifyObservers(chatId, messages.getString("typeabove1900"),"","",null);
						wrongValue = true;
					}
				}else {
					this.notifyObservers(chatId, messages.getString("typeanumber"),"","",null);
					wrongValue = true;
				}
			}
			
		//any movie
		}else {
			list = c.getRandomMovie(update, false,"","","","","","");
		}
		//merge data and send to user through message
		if(list != null) {
			
			lastMovie = new Favorite(list.get("id"),
									list.get("title"),
									list.get("rating"),
									Integer.valueOf(list.get("year")),
									list.get("imdbUrl"),
									list.get("poster"),
									list.get("trailer"),
									list.get("synopsis"));
			
		    String msgBody = EmojiParser.parseToUnicode(":wink:")+msgHeader+":\n\n<b>"+list.get("title")+" ("+list.get("year")+")</b>\n"+EmojiParser.parseToUnicode(":star:")+" <b>"+list.get("rating")+"</b>/10 on <a href=\""+list.get("imdbUrl")+"\">IMDB</a>\n\n<b>"+EmojiParser.parseToUnicode(":performing_arts:")+" "+messages.getString("genre")+": </b>"+list.get("genre")+"\n\n<b>"+EmojiParser.parseToUnicode(":movie_camera:")+" "+messages.getString("director")+":</b> "+list.get("director")+"\n\n<b>"+EmojiParser.parseToUnicode(":man:")+" "+messages.getString("cast")+":</b> "+list.get("cast")+"\n\n<b>"+EmojiParser.parseToUnicode(":page_with_curl:")+" "+messages.getString("synopsis")+":</b> "+list.get("synopsis");
			this.notifyObservers(chatId, msgBody,list.get("poster"),"",null);
			this.notifyObservers(chatId, EmojiParser.parseToUnicode(":vhs:")+" <b>"+messages.getString("trailer")+":</b>\n@vid "+youtubeLink(list.get("trailer")),"","",null);
			this.notifyObservers(chatId, "","","favorite",null);
		}else if(wrongValue == false){
			this.notifyObservers(chatId, messages.getString("nomoviefound"),"","",null);
		}
	}	
}