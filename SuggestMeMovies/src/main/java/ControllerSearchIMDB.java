import com.pengrad.telegrambot.model.Update;

public class ControllerSearchIMDB implements ControllerSearch{
	
	private Model model;
	private String search;
	private Connection c;
	
	public ControllerSearchIMDB(Model m, View v, String s){
		this.model = m; //connection Controller -> Model
		this.search = s;
		c = new Connection(model);
	}
	
	public void search(Update update){
		c.searchImdb(update,search);
	}

}
