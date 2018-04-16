import com.pengrad.telegrambot.model.Update;

public class ControllerSearchSuggestions implements ControllerSearch{
	
	
	private Model model;
	private View view;
	private Boolean filtred;
	private String filterType;
	private String filterValue;
	
	public ControllerSearchSuggestions(Model model, View view, Boolean f, String fT, String fV){
		this.model = model; //connection Controller -> Model
		this.view = view; //connection Controller -> View
		this.filtred = f; //suggestion search parameters
		this.filterType = fT;//filter type
		this.filterValue = fV;//filter value
	}
	
	public void search(Update update){
		view.sendTypingMessage(update);
		model.searchSuggestions(update, filtred, filterType, filterValue);
	}

}
