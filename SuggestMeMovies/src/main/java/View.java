import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

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
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.vdurmont.emoji.EmojiParser;


public class View implements Observer{
	private ResourceBundle messages = ResourceBundle.getBundle("locales.LabelsBundle",new Locale("en", "US"));;
	private static String[][] genreCommands = {{"/sbg action"},{"/sbg adventure"},{"/sbg animation"},{"/sbg biography"},{"/sbg comedy"},{"/sbg comedy"},{"/sbg documentary"},{"/sbg drama"},{"/sbg family"},{"/sbg fantasy"},{"/sbg film_noir"},{"/sbg history"},{"/sbg horror"},{"/sbg music"},{"/sbg musical"},{"/sbg mystery"},{"/sbg news"},{"/sbg romance"},{"/sbg sci_fi"},{"/sbg short"},{"/sbg sport"},{"/sbg thriller"},{"/sbg war"},{"/sbg western"}};
	private static String[][] decadeCommands = {{"/sby 1900"},{"/sby 1910"},{"/sby 1920"},{"/sby 1930"},{"/sby 1940"},{"/sby 1950"},{"/sby 1960"},{"/sby 1970"},{"/sby 1980"},{"/sby 1990"},{"/sby 2000"},{"/sby 2010"}};
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
		sendResponse = bot.execute(new SendMessage(chatId,messages.getString("genremsg")).parseMode(ParseMode.HTML).replyMarkup(replyKeyboard));	
	}
	
	public void searchByDecade(Long chatId) {
		ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(decadeCommands).selective(true).oneTimeKeyboard(true);
		sendResponse = bot.execute(new SendMessage(chatId,messages.getString("decademsg")).parseMode(ParseMode.HTML).replyMarkup(replyKeyboard));
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
		sendResponse = bot.execute(new SendMessage(chatId,messages.getString("ratingmsg")).parseMode(ParseMode.HTML).replyMarkup(ratingKbd));
	}
	
	public void searchByCast(Long chatId) {
		sendResponse = bot.execute(new SendMessage(chatId,messages.getString("castmsg")).parseMode(ParseMode.HTML).replyMarkup(new ForceReply()));		
	}
	
	public void searchByDirector(Long chatId) {
		sendResponse = bot.execute(new SendMessage(chatId,messages.getString("directormsg")).parseMode(ParseMode.HTML).replyMarkup(new ForceReply()));		
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
	
	public void showFavorite(Long chatId, String movieId) {
		List<Favorite> favorites = model.getFavorites(chatId);

		for (Favorite fav : favorites) {
		    if (fav.getMovieId().equals(movieId)) {
		    	String msgBody = "<b>"+fav.getMovieTitle()+" ("+fav.getMovieYear()+")</b>\n"+EmojiParser.parseToUnicode(":star:")+" <b>"+fav.getMovieRating()+"</b>/10 on <a href=\""+fav.getImdbUrl()+"\">IMDB</a>\n\n<b>"+EmojiParser.parseToUnicode(":page_with_curl:")+" Synopsis:</b> "+fav.getMovieSummary();
		    	InlineKeyboardMarkup favoriteActions = new InlineKeyboardMarkup(
			        new InlineKeyboardButton[][]{{
			        	new InlineKeyboardButton(EmojiParser.parseToUnicode(":x:")+" "+messages.getString("delfav")).callbackData("/deleteFavorite_"+fav.getMovieId()),
			        	new InlineKeyboardButton(EmojiParser.parseToUnicode(":arrow_forward:")+" "+messages.getString("watchtrailer")).url(fav.getMovieTrailer())
			        },{
			        	new InlineKeyboardButton(EmojiParser.parseToUnicode(":arrow_left:")+" "+messages.getString("back")).callbackData("showFavorites")
			        }}
			    );    	
		   
		    	sendResponse = bot.execute(new SendMessage(chatId,"<a href=\"" + fav.getPosterUrl() +" \">&#8205;</a>"+msgBody).parseMode(ParseMode.HTML).replyMarkup(favoriteActions));
		    	break;
		    }
		}
	}
	
	public void showFavoritesList(Long chatId) {
		List<Favorite> favorites = model.getFavorites(chatId);
		if(favorites.size() > 0) {
			InlineKeyboardButton[][] favKbdBtns = new InlineKeyboardButton[favorites.size()][1];
		    for (int i = 0; i < favorites.size(); i++){
		    	favKbdBtns[i][0] =  new InlineKeyboardButton(favorites.get(i).getMovieTitle()).callbackData("/showMovie_"+favorites.get(i).getMovieId());
		    }
		    InlineKeyboardMarkup favKbd = new InlineKeyboardMarkup(favKbdBtns);
		    sendResponse = bot.execute(new SendMessage(chatId,EmojiParser.parseToUnicode(":star:")+messages.getString("favlist")).parseMode(ParseMode.HTML).replyMarkup(favKbd));
		}else {
			sendResponse = bot.execute(new SendMessage(chatId,messages.getString("nofavmsg")).parseMode(ParseMode.HTML));
		}	
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
			sendResponse = bot.execute(new SendMessage(chatId,messages.getString("cmmneeded")+" <b>"+cmd+"</b> "+messages.getString("fbyvalue")).parseMode(ParseMode.HTML));
				
		//Start
		}else if(cmd.equals("/start")){
			sendResponse = bot.execute(new SendMessage(chatId,messages.getString("startmsg")).parseMode(ParseMode.HTML));
			
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
		
		} else if(cmd.equals("/favorites")) {
			showFavoritesList(chatId);
			
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
	                new InlineKeyboardButton(messages.getString("bydirector")).callbackData("sbd"),
	                new InlineKeyboardButton(messages.getString("byrating")).callbackData("sbr"),
	                new InlineKeyboardButton(messages.getString("bygenre")).callbackData("sbg")
	        	},{
	                new InlineKeyboardButton(messages.getString("bycast")).callbackData("sbc"),
	                new InlineKeyboardButton(messages.getString("bydecade")).callbackData("sby"),
	                new InlineKeyboardButton(messages.getString("anything")).callbackData("san")
	        }});
			sendResponse = bot.execute(new SendMessage(chatId,messages.getString("suggestionmsg")).parseMode(ParseMode.HTML).replyMarkup(suggestionsKeyboard));
		//help	
		}else if(cmd.equals("/help")){
			InlineKeyboardMarkup helpKeyboard = new InlineKeyboardMarkup(
	        new InlineKeyboardButton[][]{{
	                new InlineKeyboardButton(EmojiParser.parseToUnicode(":mag:")+messages.getString("searchmoviemsg")).switchInlineQueryCurrentChat(""),
	        	},{
	                new InlineKeyboardButton(EmojiParser.parseToUnicode(":hash:")+messages.getString("cmdlist")).callbackData("start")
	        }});
			sendResponse = bot.execute(new SendMessage(chatId,messages.getString("botdesc")).replyMarkup(helpKeyboard));
		
		//default response
		}else {
			sendResponse = bot.execute(new SendMessage(chatId,EmojiParser.parseToUnicode(":confused:")+messages.getString("cmminvalid")));			
		}	
	}
	
	public void callbackQueryHandler(Update u) {
		CallbackQuery q = u.callbackQuery();
		Long chatId = q.message().chat().id();
		int messageId = q.message().messageId();
		String data = q.data();
		EditMessageText editMessageText = null;
		DeleteMessage deleteMessage = null;
		
		if (q != null) {
			if(data.equals("sbg")) {
				searchByGenre(chatId);
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.equals("sbr")) {
				searchByRating(chatId);
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.indexOf("/sbr") >= 0) {
				callSearchSuggestionController(u, "/sbr", data.split("/sbr")[1].toString(), true);
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.indexOf("sby") >= 0) {
				searchByDecade(chatId);
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.equals("sbd")) {
				searchByDirector(chatId);
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.equals("sbc")) {
				searchByCast(chatId);
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.equals("/addFavorite")) {
				model.addFavorite(chatId);
				editMessageText = new EditMessageText(chatId,messageId, messages.getString("addingfav")).parseMode(ParseMode.HTML).disableWebPagePreview(true);
				
			}else if(data.equals("san")) {
				callSearchSuggestionController(u, "", "", false);
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.indexOf("/showMovie_") >= 0) {
				showFavorite(chatId,data.split("/showMovie_")[1].toString());
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.indexOf("/deleteFavorite_") >= 0) {
				model.removeFavorite(chatId,data.split("/deleteFavorite_")[1].toString());    				   
				editMessageText = new EditMessageText(chatId,messageId, messages.getString("removingfav")).parseMode(ParseMode.HTML).disableWebPagePreview(true);
				
			}else if(data.equals("start")) {
				sendResponse = bot.execute(new SendMessage(chatId,messages.getString("startmsg")).parseMode(ParseMode.HTML));
				
			}else if(data.equals("closeAction")) {
				deleteMessage = new DeleteMessage(chatId, messageId);
				
			}else if(data.equals("showFavorites")) {
				showFavoritesList(chatId);
				deleteMessage = new DeleteMessage(chatId, messageId);
			}
			
			if(deleteMessage != null) {
				bot.execute(deleteMessage);
			}else {
				if(editMessageText != null) {
					bot.execute(editMessageText);
				}
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
								
							}else if(update.message().text().indexOf(messages.getString("result")) < 0){
								sendResponse = bot.execute(new SendMessage(update.message().chat().id(),messages.getString("typestart")));
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
	public void update(long chatId, String responseData, String responseImage, String responseItem, List<Map<String, String>> qRes){
		
		//adiciona novo user se preciso
		if(model.isUserAvailable(chatId)) {
			model.addUser(new User(), chatId);
		}
		
		//call a button after movie suggestion asking for add to favorites
		if(responseItem.equals("favorite")) {
			InlineKeyboardMarkup favoriteButton = new InlineKeyboardMarkup(
				new InlineKeyboardButton[]{
					new InlineKeyboardButton(messages.getString("yesmsg")).callbackData("/addFavorite"),
					new InlineKeyboardButton(messages.getString("nomsg")).callbackData("closeAction")
				}
			);
			sendResponse = bot.execute(new SendMessage(chatId,"Add to favorites?").replyMarkup(favoriteButton));
		}
		
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
			    msgBody = "<a href=\"" + qRes.get(i).get("poster") +" \">&#8205;</a>"+"<b>"+messages.getString("result")+":</b>\n\n"+"<b>"+qRes.get(i).get("title")+" "+qRes.get(i).get("year")+"</b>\n<i>"+qRes.get(i).get("runtime")+"</i> "+EmojiParser.parseToUnicode(":star:")+" <b>"+qRes.get(i).get("rating")+"</b>/10 on <a href=\""+qRes.get(i).get("imdbUrl")+"\">IMDB</a>\n\n<b>"+EmojiParser.parseToUnicode(":performing_arts:")+" "+messages.getString("genre")+": </b>"+qRes.get(i).get("genre")+"\n\n<b>"+EmojiParser.parseToUnicode(":movie_camera:")+" "+messages.getString("director")+":</b> "+qRes.get(i).get("director")+"\n\n<b>"+EmojiParser.parseToUnicode(":man:")+" "+messages.getString("cast")+":</b>"+qRes.get(i).get("cast")+"\n\n<b>"+EmojiParser.parseToUnicode(":page_with_curl:")+" "+messages.getString("synopsis")+":</b> "+qRes.get(i).get("synopsis");
			    results[i] = new InlineQueryResultArticle(
            		Integer.toString(i),qRes.get(i).get("title"), new InputTextMessageContent(msgBody).parseMode(ParseMode.HTML))
		    		.thumbUrl(qRes.get(i).get("poster"))
		    		.thumbHeight(120).thumbWidth(40)
		    		.description(qRes.get(i).get("synopsis"))
		    		.replyMarkup(
	    				 new InlineKeyboardMarkup(
					        new InlineKeyboardButton[]{
					        	new InlineKeyboardButton(EmojiParser.parseToUnicode(":vhs:")+messages.getString("trailerdetails"))
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