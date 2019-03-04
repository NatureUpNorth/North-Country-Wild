
// parent class for a panel in the tab
public class TabItem {
	
	private String desc;
	private String hint;
	private String returnValue;
	
	public TabItem(String desc, String hint, String rv) {
		this.desc = desc;
		this.hint = hint;
		this.returnValue = rv;
	}
	
	public String getDesc() {
		return desc;
	}
	public String getHint() {
		return hint;
	}
	public String getReturnValue() {
		return returnValue;
	}
	
}
