package guru.thewowza.example.dynamic;

public class RenditionItem
{
	private String name = "";
	private String type = "both";
	private String language = "";
	private String group = "";
	
	public RenditionItem()
	{
		
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public void setLanguage(String lang)
	{
		this.language = lang;
	}
	
	public String getLanguage()
	{
		return this.language;
	}
	
	public void setGroup(String group)
	{
		this.group = group;
	}
	
	public String getGroup()
	{
		return this.group;
	}
}
