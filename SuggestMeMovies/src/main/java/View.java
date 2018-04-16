import java.util.List;
import java.util.Map;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.model.request.InputTextMessageContent;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.vdurmont.emoji.EmojiParser;


public class View implements Observer{
	private static String startMessage = "You can control me by sending these commands:\n\n<b>Suggestions:</b>\n/suggestMe - Returns a suggestion based on director, cast, rating, genre or any movie without filters. (Choice by buttons)\n\n<b>Suggestions sub-commands:</b>\n/suggestByGenre - Returns a suggestion based on genre (comedy, animation, drama, etc.)\n/suggestByDecade - Returns a suggestion based on a decade period (Ex: 1970 - 1979)\n/suggestByRating - Returns a suggestion based on IMDB Score\n/suggestByDirector - Returns a suggestion based on a director\n/suggestByCast - Returns a suggestion based on a cast member\n/suggestAnything - Returns a suggestion without specific filters\n\n\n<b>Suggestions | Specific commands:</b>\n\n<b>Suggestions by Genre:</b>\n/sbg <b>genre</b> - Returns a suggestion based on genre\n(Ex: /sbg animation)\n\n<b>Suggestions by Cast Member:</b>\n/sbc <b>cast member name</b> - Returns a suggestion based on a cast member\n(Ex: /sbc Marlon Brando)\n\n<b>Suggestions by Director:</b>\n/sbd <b>director's name</b> - Returns a suggestion based on a director\n(Ex: /sbd Stanley Kubrick)\n\n<b>Suggestions by IMDB Score:</b>\n/sbr <b>IDMB Score (6 to 9.5)</b> - Returns a suggestion based on the score\n(Ex: /sbr 6 - will return a movie rated in 6 or above)\n\n<b>Suggestions by Decade:</b>\n/sby <b>year</b> - Returns a suggestion based on a decade period\n(Ex: /sby 1970 - will return a movie released between 1970 and 1979)\n\nYou can also search for specific movies:\n\n<b>Search:</b>\nTo do so just type <b>@SuggestMeMovies_bot</b> <i>your movie name</i> and a list with 10 results will appear";
	private static String[][] genreCommands = {{"/sbg action"},{"/sbg adventure"},{"/sbg animation"},{"/sbg biography"},{"/sbg comedy"},{"/sbg comedy"},{"/sbg documentary"},{"/sbg drama"},{"/sbg family"},{"/sbg fantasy"},{"/sbg film_noir"},{"/sbg history"},{"/sbg horror"},{"/sbg music"},{"/sbg musical"},{"/sbg mystery"},{"/sbg news"},{"/sbg romance"},{"/sbg sci_fi"},{"/sbg short"},{"/sbg sport"},{"/sbg thriller"},{"/sbg war"},{"/sbg western"}};
	private static String[][] decadeCommands = {{"/sby 1900"},{"/sby 1910"},{"/sby 1920"},{"/sby 1930"},{"/sby 1940"},{"/sby 1950"},{"/sby 1960"},{"/sby 1970"},{"/sby 1980"},{"/sby 1990"},{"/sby 2000"},{"/sby 2010"}};
	private static String[] commonPrefixes = {
			"To filter by genre, select one of the list below:",
			"To filter by rating, select one of the list below or type:\n\n<b>/sbr number</b>\n\nEx: /sbr 7\n\nAre valid numbers between 6 to 9.5!",
			"To filter by cast, type <b>/sbc</b> followed by name\n\nEx: <b>/sbc Jennifer Aniston</b>",
			"To filter by director, type <b>/sbd</b> followed by name\n\nEx: <b>/sbd Stanley Kubrick</b>",
			"You must send the command",
			"followed by the value!",
			"Choose the suggestion filter that you want:",
			" Command not recognized!",
			"Send /start to see the full list of commands available!",
			"This bot can help you find a great movie suggestion to watch or a quick search using the inline feature. To see the full list of commands available press the \"/\" commands button or type \"/start\". To search for a specific movie type the bot name \"@SuggestMeMovies_bot\" followed by the search!",
			"To filter by decade, select one of the list below or type /sby <b>year</b>\n\nEx: /sby 1990"
	};
	
	private Model model;
	public View(Model m){
		this.model = m;
	}
	
	TelegramBot bot = TelegramBotAdapter.build(Model.getToken());
	
	//Object that receives messages
	GetUpdatesResponse updatesResponse;
	//Object that send responses
	SendResponse sendResponse;
	//Object that manage chat actions like "typing action"
	BaseResponse baseResponse;
	int queuesIndex=0;
	ControllerSearch controllerSearch; //Strategy Pattern -- connection View -> Controller
	boolean searchBehaviour = false;
	
	public void setControllerSearch(ControllerSearch controllerSearch){ //Strategy Pattern
		this.controllerSearch = controllerSearch;
	}
	
	public void searchByGenre(Long chatId) {
		ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(genreCommands).selective(true).oneTimeKeyboard(true);
		sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[0]).replyToMessageId(1).parseMode(ParseMode.HTML).replyMarkup(replyKeyboard));	
	}
	
	public void searchByDecade(Long chatId) {
		ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(decadeCommands).selective(true).oneTimeKeyboard(true);
		sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[10]).replyToMessageId(1).parseMode(ParseMode.HTML).replyMarkup(replyKeyboard));	
	}
	
	public void searchByRating(Long chatId) {
		InlineKeyboardMarkup ratingKbd = new InlineKeyboardMarkup(
		        new InlineKeyboardButton[][]{{
		                new InlineKeyboardButton("9.5").callbackData("/sbr95"),
		                new InlineKeyboardButton("9").callbackData("/sbr90"),
		                new InlineKeyboardButton("8.5").callbackData("/sbr85"),
		                new InlineKeyboardButton("8").callbackData("/sbr80")
		        },{
		                new InlineKeyboardButton("7.5").callbackData("/sbr75"),
		                new InlineKeyboardButton("7").callbackData("/sbr70"),
		                new InlineKeyboardButton("6.5").callbackData("/sbr65"),
		                new InlineKeyboardButton("6").callbackData("/sbr60")
		        }});
		sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[1]).replyToMessageId(1).parseMode(ParseMode.HTML).replyMarkup(ratingKbd));
	}
	
	public void searchByCast(Long chatId) {
		sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[2]).replyToMessageId(1).parseMode(ParseMode.HTML).replyMarkup(new ForceReply()));		
	}
	
	public void searchByDirector(Long chatId) {
		sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[3]).replyToMessageId(1).parseMode(ParseMode.HTML).replyMarkup(new ForceReply()));		
	}
	
	//call suggestion search (filtred or not filtred)
	public void callSearchSuggestionController(Update update, String cmd, String cmdValue, Boolean filtred) {
		if(filtred) {
			setControllerSearch(new ControllerSearchSuggestions(model, this, true, cmd, cmdValue));
		}else {
			setControllerSearch(new ControllerSearchSuggestions(model, this, false, "", ""));
		}
		this.callController(update);
	}
	
	//Handle user commands
	public void getUserCommands(Update update){
		//user msg
		Long chatId = update.message().chat().id();
		String msg = update.message().text();
		String cmdVal = "";
		String cmd = "";
		
		//Split the command from the value
		String[] cmdList = msg.split(" ",2);
		cmd = cmdList[0].toLowerCase();
		if(cmdList.length>1) {
			cmdVal = cmdList[1];
		}

		System.out.println(">Mensagem:"+msg+"\nComando:"+cmd+"\n"+"valor do comando:"+cmdVal+"\n");
		
		//suggestions by...
		//cast
		if((cmd.equals("/sbc")
				|| cmd.equals("/sbg")
				|| cmd.equals("/sbr")
				|| cmd.equals("/sby")
				|| cmd.equals("/sbd")) && !cmdVal.isEmpty()) {
			//"true" means that the suggestion will be filtred
			callSearchSuggestionController(update, cmd, cmdVal, true);
			
		//no values msg
		}else if((cmd.equals("/sbc") || cmd.equals("/sbd")) && cmdVal.isEmpty()) {
			sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[4]+" <b>"+cmd+"</b> "+commonPrefixes[5]).parseMode(ParseMode.HTML));
			
		//Start
		}else if(cmd.equals("/start")){
			sendResponse = bot.execute(new SendMessage(chatId,startMessage).parseMode(ParseMode.HTML));
			
		//anything
		}else if(cmd.equals("/suggestanything") || cmd.equals("/suggestmeanything")){
			//"false" means that the suggestion will not be filtred
			callSearchSuggestionController(update, "", "", false);
			
		//Suggest by Genre
		}else if((cmd.equals("/suggestbygenre") || cmd.equals("/sbg")) && cmdVal.isEmpty()){
			searchByGenre(chatId);
			
		//Suggest by Rating
		}else if((cmd.equals("/suggestbyrating") || cmd.equals("/sbr")) && cmdVal.isEmpty()){
			searchByRating(chatId);
			
		//Suggest by Cast			
		}else if(cmd.equals("/suggestbycast")){
			searchByCast(chatId);
			
		//Suggest Anything
		}else if(cmd.equals("/suggestbydirector")){
			searchByDirector(chatId);
			
		//Suggest by Decade		
		}else if(cmd.equals("/suggestbydecade") || cmd.equals("/sby")){
			searchByDecade(chatId);
			
		//Suggest Me		
		}else if(cmd.equals("/suggestme") || cmd.equals("/suggestmemovies")){
			InlineKeyboardMarkup suggestionsKeyboard = new InlineKeyboardMarkup(
	        new InlineKeyboardButton[][]{{
	                new InlineKeyboardButton("By Director").callbackData("sbd"),
	                new InlineKeyboardButton("By Rating").callbackData("sbr"),
	                new InlineKeyboardButton("By Genre").callbackData("sbg")
	        	},{
	                new InlineKeyboardButton("By Cast").callbackData("sbc"),
	                new InlineKeyboardButton("By Decade").callbackData("sby"),
	                new InlineKeyboardButton("Anything").callbackData("san")
	        }});
			sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[6]).replyToMessageId(1).parseMode(ParseMode.HTML).replyMarkup(suggestionsKeyboard));
		//help	
		}else if(cmd.equals("/help")){
			InlineKeyboardMarkup helpKeyboard = new InlineKeyboardMarkup(
	        new InlineKeyboardButton[][]{{
	                new InlineKeyboardButton(EmojiParser.parseToUnicode(":mag:")+" Search for a Movie").switchInlineQueryCurrentChat(""),
	        	},{
	                new InlineKeyboardButton(EmojiParser.parseToUnicode(":hash:")+" Commands list").callbackData("start")
	        }});
			sendResponse = bot.execute(new SendMessage(chatId,commonPrefixes[9]).replyMarkup(helpKeyboard));
		
		//default response
		}else {
			sendResponse = bot.execute(new SendMessage(chatId,EmojiParser.parseToUnicode(":confused:")+commonPrefixes[7]));			
		}	
	}
	
	public void callbackQueryHandler(Update u) {
		CallbackQuery q = u.callbackQuery();
		Long chatId = q.message().chat().id();
		String data = q.data();
		
		if (q != null) {
			if(data.equals("sbg")) {
				searchByGenre(chatId);
				
			}else if(data.equals("sbr")) {
				searchByRating(chatId);
				
			}else if(data.indexOf("/sbr") >= 0) {
				callSearchSuggestionController(u, "/sbr", data.split("/sbr")[1].toString(), true);
				
			}else if(data.indexOf("sby") >= 0) {
				searchByDecade(chatId);
				
			}else if(data.equals("sbd")) {
				searchByDirector(chatId);
				
			}else if(data.equals("sbc")) {
				searchByCast(chatId);
				
			}else if(data.equals("san")) {
				callSearchSuggestionController(u, "", "", false);
				
			}else if(data.equals("start")) {
				sendResponse = bot.execute(new SendMessage(chatId,startMessage).parseMode(ParseMode.HTML));
				
			}
		}
	}
	
	public void receiveUsersMessages() {
		//infinity loop
		while (true){
			try {
				//taking the Queue of Messages
				updatesResponse =  bot.execute(new GetUpdates().limit(100).offset(queuesIndex));
									
				//Queue of messages
				List<Update> updates = updatesResponse.updates();		
				if(updates != null) {
				//taking each message in the Queue
					for (Update update : updates) {
					
						//updating queue's index
						queuesIndex = update.updateId()+1;
	
						//Messages
						if(update.message() != null) {
							if(this.searchBehaviour==true){
								this.callController(update);
								
							}else if(update.message().text().substring(0, 1).equals("/")) {
								getUserCommands(update);
								
							}else if(update.message().text().indexOf("Result:") < 0){
								sendResponse = bot.execute(new SendMessage(update.message().chat().id(),commonPrefixes[8]));
							}
						}else {
							//Inline Buttons
							if(update.callbackQuery() != null) {
								callbackQueryHandler(update);
								
							}else {					
								//Inline search
								if(update.inlineQuery() != null) {
									InlineQuery inlineQuery = update.inlineQuery();
									setControllerSearch(new ControllerSearchIMDB(model, this, inlineQuery.query()));
									this.callController(update);
								}
							}
						}
					}
				}
					
			}catch(Exception e) {
				System.out.println(e.getStackTrace());
			}
		}		
	}
	
	public void callController(Update update){
		this.controllerSearch.search(update);
	}
	
	//update actions
	public void update(long chatId, String responseData, String responseImage,List<Map<String, String>> qRes){
		//message with image
		if(!responseImage.isEmpty()) {
			responseData = "<a href=\"" + responseImage +" \">&#8205;</a>"+responseData;
			sendResponse = bot.execute(new SendMessage(chatId, responseData).parseMode(ParseMode.HTML));
		
		//message only
		}else if(!responseData.isEmpty()) {
			sendResponse = bot.execute(new SendMessage(chatId, responseData).parseMode(ParseMode.HTML));
		
		//inline query results	
		}else if(qRes != null) {
			String msgBody = "";
			@SuppressWarnings("rawtypes")
			InlineQueryResult[] results = new InlineQueryResult[qRes.size()];			
			for(int i = 0;i < qRes.size();i++) {
			    msgBody = "<a href=\"" + qRes.get(i).get("poster") +" \">&#8205;</a>"+"<b>Result:</b>\n\n"+"<b>"+qRes.get(i).get("title")+" "+qRes.get(i).get("year")+"</b>\n<i>"+qRes.get(i).get("runtime")+"</i> "+EmojiParser.parseToUnicode(":star:")+" <b>"+qRes.get(i).get("rating")+"</b>/10 on <a href=\""+qRes.get(i).get("imdbUrl")+"\">IMDB</a>\n\n<b>"+EmojiParser.parseToUnicode(":performing_arts:")+" Genre: </b>"+qRes.get(i).get("genre")+"\n\n<b>"+EmojiParser.parseToUnicode(":movie_camera:")+" Director:</b> "+qRes.get(i).get("director")+"\n\n<b>"+EmojiParser.parseToUnicode(":man:")+" Cast:</b>"+qRes.get(i).get("cast")+"\n\n<b>"+EmojiParser.parseToUnicode(":page_with_curl:")+" Synopsis:</b> "+qRes.get(i).get("synopsis");
			    results[i] = new InlineQueryResultArticle(
            		Integer.toString(i),qRes.get(i).get("title"), new InputTextMessageContent(msgBody).parseMode(ParseMode.HTML))
		    		.thumbUrl(qRes.get(i).get("poster"))
		    		.thumbHeight(120).thumbWidth(40)
		    		.description(qRes.get(i).get("synopsis"))
		    		.replyMarkup(
	    				 new InlineKeyboardMarkup(
					        new InlineKeyboardButton[]{
					        	new InlineKeyboardButton(EmojiParser.parseToUnicode(":vhs:")+" Trailer + Details")
					        	.url("http://imdb.com"+qRes.get(i).get("imdbUrl"))
					     })
		    		);
			}
			bot.execute(new AnswerInlineQuery(String.valueOf(chatId), results).cacheTime(1).isPersonal(false).nextOffset(""));
		}
		this.searchBehaviour = false;
	}
	
	//send "typing message" to user
	public void sendTypingMessage(Update update){
		Long chatId = null;
		if(update.message() != null) {
			chatId = update.message().chat().id();
		}else if(update.callbackQuery() != null){
			chatId = update.callbackQuery().message().chat().id();
		}
		if(chatId != null) {
			baseResponse = bot.execute(new SendChatAction(chatId, ChatAction.typing.name()));
		}
	}
}