import java.util.ArrayList;
import java.util.List;

public class User {
	private long chatid;
	private List<Favorite> favorites = new ArrayList<Favorite>();

	public List<Favorite> getFavorites() {
		return favorites;
	}
	public void setFavorites(List<Favorite> favorites) {
		this.favorites = favorites;
	}
	public long getChatid() {
		return chatid;
	}
	public void setChatid(long chatid) {
		this.chatid = chatid;
	}
}
