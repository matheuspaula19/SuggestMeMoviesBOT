import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.vdurmont.emoji.EmojiParser;
import com.pengrad.telegrambot.model.Update;

public class Model implements Subject{
	
	private List<Observer> observers = new LinkedList<Observer>();
	private static Model uniqueInstance;
	private static List<String> genresAllowed = Arrays.asList("action","adventure","animation","biography","comedy","comedy","documentary","drama","family","fantasy","film_noir","history","horror","music","musical","mystery","news","romance","sci_fi","short","sport","thriller","war","western");
	private static String[] commonPrefixes = {"You typed only numbers!",
								"Please type a valid category!",
								"Invalid rating number!",
								"Please type a number!",
								"Please type a year above 1900!",
								"No movie Found!"};
	private Model(){}
	
	
	public static String getToken() {
		return "508952083:AAHlAuJm1gLOnjKmqvN29NlUsC92MQLDnIo";
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
	public void notifyObservers(long chatId, String responseData, String responseImage, List<Map<String, String>> queryResult){
		for(Observer observer:observers){
			observer.update(chatId, responseData, responseImage, queryResult);
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
		String msgHeader = " Maybe you'll like this one";
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
					this.notifyObservers(chatId, commonPrefixes[0],"",null);
					wrongValue = true;
				}
				
			//by genre
			}else if(filterType.equals("/sbg")) {
				String g = filterVal.replaceAll("-", "_").toLowerCase();
				if(genresAllowed.contains(g)) {
					list = c.getRandomMovie(update, true,g,"","0","0","","");
					msgHeader = msgHeader + " in the <b>" + filterVal + "</b> category";
				}else {
					this.notifyObservers(chatId, commonPrefixes[1],"",null);
					wrongValue = true;
				}
			//by rating
			}else if(filterType.equals("/sbr")) {
				if(filterVal.matches("[0-9]*\\.?[0-9]*")) {
					float r = Float.parseFloat(filterVal);
					if(r <= 0 || r > 95) {
						this.notifyObservers(chatId, commonPrefixes[2],"",null);
						wrongValue = true;
					}else {
						if((float)r<(float)10) {r = r*10;}
						int v = (int)r;
						list = c.getRandomMovie(update, true,"","","0",String.valueOf(v),"","");
						msgHeader = msgHeader + " with <b>"+ ((float)r/(float)10) +"</b> or above on IMDB";
					}
				}else {
					this.notifyObservers(chatId, commonPrefixes[3],"",null);
					wrongValue = true;
				}
				
			//by decade	
			}else if(filterType.equals("/sby")) {
				if(filterVal.matches("-?\\d+")) {
					/*gets decade year*/
					int y = (Integer.parseInt(filterVal)/10)*10;
					if(y>1899) {
						list = c.getRandomMovie(update, true,"","","0","0",String.valueOf(y),String.valueOf(y+9));
						msgHeader = msgHeader + " in the <b>"+y+"'s</b> period";
					}else {
						this.notifyObservers(chatId, commonPrefixes[4],"",null);
						wrongValue = true;
					}
				}else {
					this.notifyObservers(chatId, commonPrefixes[3],"",null);
					wrongValue = true;
				}
			}
			
		//any movie
		}else {
			list = c.getRandomMovie(update, false,"","","","","","");
		}
		//merge data and send to user through message
		if(list != null) {
		    String msgBody = EmojiParser.parseToUnicode(":wink:")+msgHeader+":\n\n<b>"+list.get("title")+" ("+list.get("year")+")</b>\n"+EmojiParser.parseToUnicode(":star:")+" <b>"+list.get("rating")+"</b>/10 on <a href=\""+list.get("imdbUrl")+"\">IMDB</a>\n\n<b>"+EmojiParser.parseToUnicode(":performing_arts:")+" Genre: </b>"+list.get("genre")+"\n\n<b>"+EmojiParser.parseToUnicode(":movie_camera:")+" Director:</b> "+list.get("director")+"\n\n<b>"+EmojiParser.parseToUnicode(":man:")+" Cast:</b> "+list.get("cast")+"\n\n<b>"+EmojiParser.parseToUnicode(":page_with_curl:")+" Synopsis:</b> "+list.get("synopsis");
			this.notifyObservers(chatId, msgBody,list.get("poster"),null);
			this.notifyObservers(chatId, EmojiParser.parseToUnicode(":vhs:")+" <b>Trailer:</b>\n@vid "+youtubeLink(list.get("trailer")),"",null);
		}else if(wrongValue == false){
			this.notifyObservers(chatId, commonPrefixes[5],"",null);
		}
	}	
}